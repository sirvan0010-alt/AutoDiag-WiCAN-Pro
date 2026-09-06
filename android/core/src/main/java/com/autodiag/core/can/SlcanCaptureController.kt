package com.autodiag.core.can

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Owns the lifecycle bridge between the live SLCAN frame stream and the bounded
 * capture recorder. This keeps capture explicitly opt-in and produces an immutable
 * session when stopped.
 */
class SlcanCaptureController(
    private val stream: SlcanCanFrameStream,
    private val capture: CanCapture = CanCapture(),
    private val scope: CoroutineScope
) {
    private var recordingJob: Job? = null

    val isCapturing: Boolean get() = capture.isCapturing

    fun start(nowNanos: Long? = null) {
        check(recordingJob == null || recordingJob?.isActive != true) {
            "SLCAN capture is already running"
        }
        if (nowNanos == null) capture.start() else capture.start(nowNanos)
        recordingJob = scope.launch {
            stream.frames.collect { frame ->
                capture.record(frame)
            }
        }
    }

    fun snapshot(): CanCaptureSession? = capture.snapshot()

    fun stop(): CanCaptureSession? {
        recordingJob?.cancel()
        recordingJob = null
        return capture.stop()
    }
}
