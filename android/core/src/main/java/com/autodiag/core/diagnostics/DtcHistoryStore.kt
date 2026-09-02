package com.autodiag.core.diagnostics

import com.autodiag.core.obd.DiagnosticTroubleCode
import com.autodiag.core.obd.DtcMemory

/** Why a tracked DTC transitioned from ACTIVE to RESOLVED. */
enum class DtcResolutionReason {
    NOT_RESOLVED,
    ABSENT_ON_RESCAN,
    CLEARED_BY_USER
}

enum class DtcHistoryStatus { ACTIVE, RESOLVED }

/**
 * One tracked (ECU, memory, code) lineage.
 * Resolved records remain in the store so first/last/resolution timestamps are preserved.
 */
data class DtcHistoryRecord(
    val ecuAddress: Int?,
    val memory: DtcMemory,
    val code: String,
    val firstSeenAt: Long,
    val lastSeenAt: Long,
    val status: DtcHistoryStatus,
    val resolvedAt: Long? = null,
    val resolutionReason: DtcResolutionReason = DtcResolutionReason.NOT_RESOLVED,
    val timesObserved: Int = 1,
    val reoccurrenceCount: Int = 0
) {
    private fun key() = Triple(ecuAddress, memory, code)

    override fun equals(other: Any?): Boolean = other is DtcHistoryRecord &&
        key() == other.key() &&
        firstSeenAt == other.firstSeenAt &&
        lastSeenAt == other.lastSeenAt &&
        status == other.status &&
        resolvedAt == other.resolvedAt &&
        resolutionReason == other.resolutionReason &&
        timesObserved == other.timesObserved &&
        reoccurrenceCount == other.reoccurrenceCount

    override fun hashCode(): Int = 31 * key().hashCode() + firstSeenAt.hashCode()
}

/**
 * Pure, Android/transport-neutral DTC history aggregation.
 *
 * The caller must only pass a complete scan for the selected (ECU, memory) scope.
 * A filtered/partial scan must not be ingested because absence would be interpreted
 * as resolution. Persistence (for example Room) should wrap this store rather than
 * duplicate its state-transition rules.
 */
class DtcHistoryStore {
    private val records = LinkedHashMap<Triple<Int?, DtcMemory, String>, DtcHistoryRecord>()

    fun ingestScan(
        ecuAddress: Int?,
        memory: DtcMemory,
        currentCodes: List<DiagnosticTroubleCode>,
        scanTimestamp: Long
    ) {
        val currentCodeSet = currentCodes.map { it.code }.toSet()

        for (dtc in currentCodes.distinctBy { it.code }) {
            val key = Triple(ecuAddress, memory, dtc.code)
            val existing = records[key]
            records[key] = when {
                existing == null -> DtcHistoryRecord(
                    ecuAddress = ecuAddress,
                    memory = memory,
                    code = dtc.code,
                    firstSeenAt = scanTimestamp,
                    lastSeenAt = scanTimestamp,
                    status = DtcHistoryStatus.ACTIVE
                )
                existing.status == DtcHistoryStatus.ACTIVE -> existing.copy(
                    lastSeenAt = scanTimestamp,
                    timesObserved = existing.timesObserved + 1
                )
                else -> existing.copy(
                    lastSeenAt = scanTimestamp,
                    status = DtcHistoryStatus.ACTIVE,
                    resolvedAt = null,
                    resolutionReason = DtcResolutionReason.NOT_RESOLVED,
                    timesObserved = existing.timesObserved + 1,
                    reoccurrenceCount = existing.reoccurrenceCount + 1
                )
            }
        }

        // Only resolve records belonging to this exact scan scope.
        val snapshot = records.toList()
        for ((key, record) in snapshot) {
            val (recEcu, recMemory, recCode) = key
            if (recEcu != ecuAddress || recMemory != memory) continue
            if (record.status != DtcHistoryStatus.ACTIVE) continue
            if (recCode in currentCodeSet) continue
            records[key] = record.copy(
                status = DtcHistoryStatus.RESOLVED,
                resolvedAt = scanTimestamp,
                resolutionReason = DtcResolutionReason.ABSENT_ON_RESCAN
            )
        }
    }

    /**
     * Records a confirmed Mode 04 / UDS 0x14 clear for one ECU + memory.
     * It must be called only after the transport/protocol layer confirms success.
     */
    fun recordExplicitClear(ecuAddress: Int?, memory: DtcMemory, clearedAt: Long) {
        val snapshot = records.toList()
        for ((key, record) in snapshot) {
            val (recEcu, recMemory, _) = key
            if (recEcu != ecuAddress || recMemory != memory) continue
            if (record.status != DtcHistoryStatus.ACTIVE) continue
            records[key] = record.copy(
                status = DtcHistoryStatus.RESOLVED,
                resolvedAt = clearedAt,
                resolutionReason = DtcResolutionReason.CLEARED_BY_USER
            )
        }
    }

    /** Full history, including resolved records. */
    fun all(): List<DtcHistoryRecord> = records.values.sortedByDescending { it.firstSeenAt }

    fun active(): List<DtcHistoryRecord> = records.values
        .filter { it.status == DtcHistoryStatus.ACTIVE }
        .sortedByDescending { it.firstSeenAt }

    fun resolved(): List<DtcHistoryRecord> = records.values
        .filter { it.status == DtcHistoryStatus.RESOLVED }
        .sortedByDescending { it.resolvedAt ?: 0L }

    fun forEcu(ecuAddress: Int?): List<DtcHistoryRecord> = records.values
        .filter { it.ecuAddress == ecuAddress }
        .sortedByDescending { it.firstSeenAt }
}
