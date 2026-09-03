package com.autodiag.core.can

/** One captured CAN frame with a monotonic timestamp relative to the capture start. */
data class CanCaptureRecord(
    val timestampNanos: Long,
    val frame: CanFrame
)

/** Immutable snapshot of a capture session. */
data class CanCaptureSession(
    val startedAtNanos: Long,
    val records: List<CanCaptureRecord>,
    val droppedRecords: Long = 0
) {
    val durationNanos: Long?
        get() = records.lastOrNull()?.timestampNanos

    val frameCount: Int get() = records.size
}

/**
 * Bounded in-memory CAN recorder. It never grows without limit: oldest records are
 * discarded once [maxRecords] is reached and the discard count is preserved.
 */
class CanCapture(
    private val maxRecords: Int = DEFAULT_MAX_RECORDS,
    private val clockNanos: () -> Long = System::nanoTime
) {
    private val records = ArrayDeque<CanCaptureRecord>()
    private var startedAtNanos: Long? = null
    private var droppedRecords: Long = 0

    init { require(maxRecords > 0) { "maxRecords must be > 0" } }

    val isCapturing: Boolean get() = startedAtNanos != null

    fun start(nowNanos: Long = clockNanos()) {
        records.clear()
        droppedRecords = 0
        startedAtNanos = nowNanos
    }

    fun record(frame: CanFrame, nowNanos: Long = clockNanos()): Boolean {
        val start = startedAtNanos ?: return false
        val relative = (nowNanos - start).coerceAtLeast(0L)
        if (records.size >= maxRecords) {
            records.removeFirst()
            droppedRecords++
        }
        records.addLast(CanCaptureRecord(relative, frame.copy(timestampNanos = relative)))
        return true
    }

    fun stop(): CanCaptureSession? {
        val start = startedAtNanos ?: return null
        startedAtNanos = null
        return CanCaptureSession(start, records.toList(), droppedRecords)
    }

    fun snapshot(): CanCaptureSession? = startedAtNanos?.let {
        CanCaptureSession(it, records.toList(), droppedRecords)
    }

    fun clear() {
        records.clear()
        droppedRecords = 0
    }

    companion object { const val DEFAULT_MAX_RECORDS = 50_000 }
}
