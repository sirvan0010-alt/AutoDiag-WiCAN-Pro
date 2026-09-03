package com.autodiag.core.obd

/**
 * Tracks ECU responsiveness from diagnostic request results.
 *
 * Transport connectivity alone must not mark an ECU ONLINE. Only a successful
 * diagnostic response calls [onSuccess].
 */
class EcuConnectionMonitor(
    private val nowMs: () -> Long = { System.currentTimeMillis() },
    private val degradedAfterFailures: Int = 3,
    private val offlineAfterMs: Long = 5_000L
) {
    private var lastSuccessfulResponseMs: Long? = null
    private var consecutiveFailures: Int = 0

    init {
        require(degradedAfterFailures > 0) { "degradedAfterFailures must be > 0" }
        require(offlineAfterMs >= 0L) { "offlineAfterMs must be >= 0" }
    }

    fun onSuccess() {
        lastSuccessfulResponseMs = nowMs()
        consecutiveFailures = 0
    }

    fun onFailure() {
        consecutiveFailures++
    }

    fun state(): EcuConnectionState {
        val last = lastSuccessfulResponseMs
            ?: return EcuConnectionState.CONNECTING
        val age = (nowMs() - last).coerceAtLeast(0L)
        return when {
            age > offlineAfterMs -> EcuConnectionState.OFFLINE
            consecutiveFailures >= degradedAfterFailures -> EcuConnectionState.DEGRADED
            else -> EcuConnectionState.ONLINE
        }
    }

    fun reset() {
        lastSuccessfulResponseMs = null
        consecutiveFailures = 0
    }
}
