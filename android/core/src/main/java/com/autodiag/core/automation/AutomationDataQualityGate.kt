package com.autodiag.core.automation

/** Read-only data-quality gate for freshness and signal availability. */
data class DataQualityResult(
    val accepted: Boolean,
    val reason: String,
    val staleSignalIds: List<String> = emptyList(),
    val missingSignalIds: List<String> = emptyList()
)

class AutomationDataQualityGate(private val maxAgeMs: Long = 30_000L) {
    init { require(maxAgeMs >= 0L) { "maxAgeMs must be non-negative" } }

    fun validate(requiredSignalIds: Set<String>, signals: List<SemanticSignal>, nowMs: Long): DataQualityResult {
        val byId = signals.associateBy { it.id }
        val missing = requiredSignalIds.filter { id ->
            val signal = byId[id]
            signal == null || signal.value == null
        }
        val stale = requiredSignalIds.mapNotNull { id ->
            val signal = byId[id] ?: return@mapNotNull null
            if (signal.value == null || signal.timestampMs <= 0L) return@mapNotNull id
            val age = nowMs - signal.timestampMs
            if (age < 0L || age > maxAgeMs) id else null
        }
        return when {
            missing.isNotEmpty() -> DataQualityResult(false, "REQUIRED_SIGNAL_UNAVAILABLE", stale, missing)
            stale.isNotEmpty() -> DataQualityResult(false, "STALE_OR_INVALID_SIGNAL", stale)
            else -> DataQualityResult(true, "DATA_QUALITY_OK")
        }
    }
}
