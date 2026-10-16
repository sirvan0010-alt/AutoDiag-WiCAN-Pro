package com.autodiag.core.can

import com.autodiag.core.can.mining.CanFrameView
import com.autodiag.core.can.mining.CanMiningCounterFilter
import com.autodiag.core.can.mining.CanMiningFrameClassifier

/** A time interval in the monotonic, capture-relative timestamp domain. Bounds are inclusive. */
data class CanMiningWindow(val startNanos: Long, val endNanos: Long) {
    init {
        require(startNanos >= 0L) { "window start must be non-negative" }
        require(endNanos >= startNanos) { "window end must be >= start" }
    }

    fun contains(timestampNanos: Long): Boolean = timestampNanos in startNanos..endNanos
}

/** Evidence-backed observation for one CAN identifier/layout in two capture windows. */
data class CanMiningCandidate(
    val canId: Long,
    val isExtended: Boolean,
    val isRemote: Boolean,
    val dlc: Int,
    val baselineFrameCount: Int,
    val eventFrameCount: Int,
    val changedBytePositions: List<Int>,
    val eventOnlyPayloads: List<String>,
    val reasons: List<String>,
    val score: Int,
    /** Descriptive timing label from event-window observations; never a semantic verification. */
    val trafficClass: String = "UNKNOWN",
    /** Byte indexes that match a rolling-counter heuristic; may include false positives. */
    val likelyCounterBytePositions: List<Int> = emptyList()
)

data class CanMiningDiffReport(
    val baselineWindow: CanMiningWindow,
    val eventWindow: CanMiningWindow,
    val baselineFrameCount: Int,
    val eventFrameCount: Int,
    val candidates: List<CanMiningCandidate>
)

/**
 * Deterministic, offline-only baseline/event comparison. This class has no transport or TX API.
 * It finds candidates for inspection; it does not identify a signal's meaning or prove causation.
 */
object CanMiningDiff {
    private data class LayoutKey(
        val id: Long, val extended: Boolean, val remote: Boolean, val dlc: Int
    )

    fun analyze(
        session: CanCaptureSession,
        baselineWindow: CanMiningWindow,
        eventWindow: CanMiningWindow
    ): CanMiningDiffReport {
        require(baselineWindow.endNanos < eventWindow.startNanos || eventWindow.endNanos < baselineWindow.startNanos) {
            "baseline and event windows must not overlap"
        }
        val baseline = session.records.filter { baselineWindow.contains(it.timestampNanos) }
        val event = session.records.filter { eventWindow.contains(it.timestampNanos) }
        val baselineGroups = baseline.groupBy { key(it.frame) }
        val eventGroups = event.groupBy { key(it.frame) }
        val eventViews = event.map { record ->
            val frame = record.frame
            CanFrameView(
                timestampMillis = record.timestampNanos / 1_000_000L,
                canId = frame.id,
                extended = frame.isExtended,
                remote = frame.isRemote,
                dlc = frame.dataLength,
                payload = frame.data.copyOf()
            )
        }
        val timingByKey = CanMiningFrameClassifier.classify(eventViews)
        val counterPatterns = setOf(
            CanMiningCounterFilter.BytePattern.COUNTER_NIBBLE_HI,
            CanMiningCounterFilter.BytePattern.COUNTER_NIBBLE_LO,
            CanMiningCounterFilter.BytePattern.COUNTER_BYTE
        )
        val counterPositionsByKey = CanMiningCounterFilter.analyze(eventViews)
            .filter { it.pattern in counterPatterns }
            .groupBy({ it.key }, { it.byteIndex })
            .mapValues { (_, positions) -> positions.distinct().sorted() }
        val candidates = (baselineGroups.keys + eventGroups.keys).distinct().mapNotNull { key ->
            val before = baselineGroups[key].orEmpty()
            val during = eventGroups[key].orEmpty()
            if (during.isEmpty()) return@mapNotNull null

            val beforePayloads = before.map { it.frame.data.toHex() }.toSet()
            val duringPayloads = during.map { it.frame.data.toHex() }.toSet()
            val eventOnlyPayloads = (duringPayloads - beforePayloads).sorted()
            val changedPositions = (0 until key.dlc).filter { position ->
                val beforeValues = before.mapNotNull { it.frame.data.getOrNull(position)?.toInt()?.and(0xFF) }.toSet()
                val eventValues = during.mapNotNull { it.frame.data.getOrNull(position)?.toInt()?.and(0xFF) }.toSet()
                beforeValues != eventValues
            }
            val reasons = buildList {
                if (before.isEmpty()) add("identifier/layout observed only in event window")
                if (eventOnlyPayloads.isNotEmpty()) add("new payload variant(s) observed in event window")
                if (changedPositions.isNotEmpty()) add("observed byte-value set changed at position(s): ${changedPositions.joinToString()}")
            }
            if (reasons.isEmpty()) return@mapNotNull null
            val score = (if (before.isEmpty()) 10 else 0) +
                changedPositions.size * 2 +
                eventOnlyPayloads.size
            val miningKey = CanFrameView.Key(key.id, key.extended, key.remote, key.dlc)
            CanMiningCandidate(
                canId = key.id, isExtended = key.extended, isRemote = key.remote, dlc = key.dlc,
                baselineFrameCount = before.size, eventFrameCount = during.size,
                changedBytePositions = changedPositions, eventOnlyPayloads = eventOnlyPayloads,
                reasons = reasons, score = score,
                trafficClass = timingByKey[miningKey]?.frameClass?.name ?: "UNKNOWN",
                likelyCounterBytePositions = counterPositionsByKey[miningKey].orEmpty()
            )
        }.sortedWith(compareByDescending<CanMiningCandidate> { it.score }.thenBy { it.canId }.thenBy { it.dlc })

        return CanMiningDiffReport(
            baselineWindow = baselineWindow, eventWindow = eventWindow,
            baselineFrameCount = baseline.size, eventFrameCount = event.size, candidates = candidates
        )
    }

    private fun key(frame: CanFrame) = LayoutKey(frame.id, frame.isExtended, frame.isRemote, frame.dataLength)
    private fun ByteArray.toHex(): String = joinToString("") { "%02X".format(it.toInt() and 0xFF) }
}
