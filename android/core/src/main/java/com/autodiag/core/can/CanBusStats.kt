package com.autodiag.core.can

/** Lightweight counters for diagnostics and transport-health reporting. */
data class CanBusStats(
    val receivedFrames: Long = 0,
    val receivedBytes: Long = 0,
    val droppedFrames: Long = 0,
    val errorFrames: Long = 0,
    val firstTimestampNanos: Long? = null,
    val lastTimestampNanos: Long? = null
) {
    fun onFrame(frame: CanFrame): CanBusStats = copy(
        receivedFrames = receivedFrames + 1,
        receivedBytes = receivedBytes + frame.dataLength,
        firstTimestampNanos = firstTimestampNanos ?: frame.timestampNanos,
        lastTimestampNanos = frame.timestampNanos ?: lastTimestampNanos
    )

    fun onDrop(): CanBusStats = copy(droppedFrames = droppedFrames + 1)
    fun onError(): CanBusStats = copy(errorFrames = errorFrames + 1)

    fun frameRateHz(): Double? {
        val first = firstTimestampNanos ?: return null
        val last = lastTimestampNanos ?: return null
        val seconds = (last - first) / 1_000_000_000.0
        return if (seconds > 0.0) receivedFrames / seconds else null
    }
}
