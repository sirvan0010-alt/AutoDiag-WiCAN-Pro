package com.autodiag.core.obd

enum class LiveDataPriority {
    HIGH,
    MEDIUM,
    LOW
}

data class LiveDataPollPlan(
    val pid: Int,
    val priority: LiveDataPriority,
    val intervalMs: Long
)

/** Deterministic first-stage polling policy; adaptive sampling can build on it later. */
object LiveDataPidPolicy {
    fun priority(pid: Int): LiveDataPriority = when (pid) {
        0x04, 0x05, 0x0C, 0x0D, 0x11 -> LiveDataPriority.HIGH
        0x06, 0x07, 0x08, 0x09, 0x0E, 0x0F, 0x10 -> LiveDataPriority.MEDIUM
        else -> LiveDataPriority.LOW
    }

    fun intervalMs(pid: Int): Long = when (priority(pid)) {
        LiveDataPriority.HIGH -> 500L
        LiveDataPriority.MEDIUM -> 1_000L
        LiveDataPriority.LOW -> 2_500L
    }

    fun plan(pid: Int): LiveDataPollPlan =
        LiveDataPollPlan(pid, priority(pid), intervalMs(pid))
}
