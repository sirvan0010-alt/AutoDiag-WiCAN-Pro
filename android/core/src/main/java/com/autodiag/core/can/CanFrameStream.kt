package com.autodiag.core.can

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Hot CAN frame stream shared by transports, diagnostics and the live monitor. */
class CanFrameStream(
    extraBufferCapacity: Int = 256
) {
    private val _frames = MutableSharedFlow<CanFrame>(
        replay = 0,
        extraBufferCapacity = extraBufferCapacity
    )

    val frames: Flow<CanFrame> = _frames.asSharedFlow()

    fun publish(frame: CanFrame): Boolean = _frames.tryEmit(frame)
}
