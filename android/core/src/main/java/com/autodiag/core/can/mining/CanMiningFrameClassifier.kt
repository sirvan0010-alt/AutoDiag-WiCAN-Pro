package com.autodiag.core.can.mining

/**
 * Classifies CAN layouts in one capture window as PERIODIC, EVENT or MIXED.
 * Results are descriptive evidence, not proof of a command or signal meaning.
 */
object CanMiningFrameClassifier {
    enum class FrameClass { PERIODIC, EVENT, MIXED }

    data class IdStats(
        val key: CanFrameView.Key,
        val frameCount: Int,
        val medianIntervalMillis: Double,
        val intervalCv: Double,
        val frameClass: FrameClass,
    )

    data class Config(
        val minPeriodicFrames: Int = 5,
        val maxPeriodicMedianMillis: Long = 500,
        val maxPeriodicCv: Double = 0.5,
        val minEventMedianMillis: Long = 500,
        val gapFactor: Double = 5.0,
    ) {
        init {
            require(minPeriodicFrames >= 2) { "minPeriodicFrames must be >= 2" }
            require(maxPeriodicMedianMillis >= 0L) { "maxPeriodicMedianMillis must be non-negative" }
            require(minEventMedianMillis >= 0L) { "minEventMedianMillis must be non-negative" }
            require(maxPeriodicCv >= 0.0 && maxPeriodicCv.isFinite()) { "maxPeriodicCv must be finite and non-negative" }
            require(gapFactor >= 1.0 && gapFactor.isFinite()) { "gapFactor must be finite and >= 1" }
        }
    }

    val defaultConfig = Config()

    /** Input order does not matter; each (ID, flags, DLC) layout is independent. */
    fun classify(
        frames: List<CanFrameView>,
        config: Config = defaultConfig,
    ): Map<CanFrameView.Key, IdStats> =
        frames.groupBy { it.key }.mapValues { (_, group) -> statsFor(group, config) }

    private fun statsFor(group: List<CanFrameView>, config: Config): IdStats {
        val sorted = group.sortedBy { it.timestampMillis }
        val intervals = sorted.zipWithNext { a, b -> b.timestampMillis - a.timestampMillis }
            .filter { it >= 0L }

        if (intervals.isEmpty()) {
            return IdStats(sorted.first().key, sorted.size, Double.NaN, Double.NaN, FrameClass.EVENT)
        }

        val median = intervals.median()
        val kept = intervals.filter { it.toDouble() <= median * config.gapFactor }
        val dispersionBase = if (kept.size >= 2) kept else intervals
        val mean = dispersionBase.average()
        val cv = if (mean > 0.0) {
            val variance = dispersionBase.map { (it - mean) * (it - mean) }.average()
            kotlin.math.sqrt(variance) / mean
        } else {
            // Repeated timestamps are not evidence of a stable periodic stream.
            Double.POSITIVE_INFINITY
        }

        val classification = when {
            sorted.size >= config.minPeriodicFrames &&
                median <= config.maxPeriodicMedianMillis &&
                cv <= config.maxPeriodicCv -> FrameClass.PERIODIC
            median >= config.minEventMedianMillis -> FrameClass.EVENT
            else -> FrameClass.MIXED
        }
        return IdStats(sorted.first().key, sorted.size, median, cv, classification)
    }

    private fun List<Long>.median(): Double {
        val sorted = sorted()
        return if (sorted.size % 2 == 1) sorted[sorted.size / 2].toDouble()
        else (sorted[sorted.size / 2 - 1].toDouble() + sorted[sorted.size / 2].toDouble()) / 2.0
    }
}
