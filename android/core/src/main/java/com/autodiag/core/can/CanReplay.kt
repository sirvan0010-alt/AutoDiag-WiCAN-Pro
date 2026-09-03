package com.autodiag.core.can

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Replays a captured session using its original relative timing. */
object CanReplay {
    fun flow(
        session: CanCaptureSession,
        speed: Double = 1.0
    ): Flow<CanFrame> = flow {
        require(speed > 0.0) { "speed must be > 0" }
        var previousNanos = 0L
        for (record in session.records) {
            val deltaNanos = (record.timestampNanos - previousNanos).coerceAtLeast(0L)
            val delayMillis = (deltaNanos / 1_000_000.0 / speed).toLong()
            if (delayMillis > 0) delay(delayMillis)
            emit(record.frame.copy(timestampNanos = record.timestampNanos))
            previousNanos = record.timestampNanos
        }
    }
}
