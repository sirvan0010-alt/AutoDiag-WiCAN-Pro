package com.autodiag.core.transport

/** Lightweight link-health counters shared by WiCAN transports and the UI. */
data class TransportMetrics(
    val rxChunks: Long = 0,
    val rxBytes: Long = 0,
    val txChunks: Long = 0,
    val txBytes: Long = 0,
    val reconnects: Long = 0,
    val lastRxAtMs: Long? = null,
    val lastTxAtMs: Long? = null,
    val connectedAtMs: Long? = null
) {
    val idleRxMs: Long?
        get() = lastRxAtMs?.let { (System.currentTimeMillis() - it).coerceAtLeast(0L) }
}
