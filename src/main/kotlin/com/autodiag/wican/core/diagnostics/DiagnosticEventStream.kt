package com.autodiag.wican.core.diagnostics

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Process-wide diagnostic event stream for transport, CAN and protocol layers.
 * Buffering is bounded so telemetry cannot grow memory without limit.
 */
class DiagnosticEventStream(
    extraBufferCapacity: Int = 256
) {
    private val events = MutableSharedFlow<DiagnosticEvent>(
        replay = 0,
        extraBufferCapacity = extraBufferCapacity.coerceAtLeast(1)
    )

    val flow: SharedFlow<DiagnosticEvent> = events.asSharedFlow()

    fun tryEmit(event: DiagnosticEvent): Boolean = events.tryEmit(event)
}
