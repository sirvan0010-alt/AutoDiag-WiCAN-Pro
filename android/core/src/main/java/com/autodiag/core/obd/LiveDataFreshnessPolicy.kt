package com.autodiag.core.obd

/** Converts sample age into a UI-safe freshness state. */
data class LiveDataFreshnessPolicy(
    val freshAfterMs: Long = 1_500L,
    val staleAfterMs: Long = 4_000L
) {
    init {
        require(freshAfterMs >= 0) { "freshAfterMs must be >= 0" }
        require(staleAfterMs >= freshAfterMs) { "staleAfterMs must be >= freshAfterMs" }
    }

    fun evaluate(sampleTimestampMs: Long, nowMs: Long): LiveDataFreshness {
        val age = (nowMs - sampleTimestampMs).coerceAtLeast(0L)
        return when {
            age <= freshAfterMs -> LiveDataFreshness.FRESH
            age <= staleAfterMs -> LiveDataFreshness.AGING
            else -> LiveDataFreshness.STALE
        }
    }
}
