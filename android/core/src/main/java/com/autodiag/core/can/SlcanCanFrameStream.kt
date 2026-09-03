package com.autodiag.core.can

import com.autodiag.core.transport.WiCanTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/** Bridges WiCAN SLCAN byte chunks into a hot stream of decoded classic CAN frames. */
class SlcanCanFrameStream(
    private val transport: WiCanTransport,
    scope: CoroutineScope
) {
    private val decoder = SlcanCodec.StreamDecoder()
    private val _frames = MutableSharedFlow<CanFrame>(extraBufferCapacity = 512)
    val frames: SharedFlow<CanFrame> = _frames.asSharedFlow()

    private val job: Job = scope.launch {
        transport.observeIncoming().collect { chunk ->
            val timestamp = System.nanoTime()
            decoder.accept(chunk, timestamp).forEach { frame ->
                _frames.emit(frame)
            }
        }
    }

    fun stop() {
        job.cancel()
        decoder.reset()
    }
}
