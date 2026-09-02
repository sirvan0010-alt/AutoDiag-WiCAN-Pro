package com.autodiag.core.oscilloscope

import com.autodiag.core.can.CanFrame
import com.autodiag.core.evidence.DiagnosticEvent

/** Converts the shared diagnostic event stream into oscilloscope overlay markers. */
object OscilloscopeDiagnosticEventMapper {
    fun map(event: DiagnosticEvent): OscilloscopeCorrelationEvent? = when (event) {
        is DiagnosticEvent.FrameReceived -> OscilloscopeCorrelationEvent(
            timestampNanos = event.timestampNanos,
            type = OscilloscopeCorrelationEvent.Type.CAN_FRAME,
            label = "CAN 0x${event.frame.id.toString(16).uppercase()}",
            details = event.frame.hex(),
        )
        is DiagnosticEvent.DiagnosticResponse -> OscilloscopeCorrelationEvent(
            timestampNanos = event.timestampNanos,
            type = if (event.isNegative) OscilloscopeCorrelationEvent.Type.DTC
            else OscilloscopeCorrelationEvent.Type.UDS_RESPONSE,
            label = event.summary,
            details = event.rawResponse,
        )
        is DiagnosticEvent.CommunicationError -> OscilloscopeCorrelationEvent(
            timestampNanos = event.timestampNanos,
            type = OscilloscopeCorrelationEvent.Type.COMMUNICATION,
            label = "Communication error",
            details = event.message,
        )
        is DiagnosticEvent.ConnectionChanged -> OscilloscopeCorrelationEvent(
            timestampNanos = event.timestampNanos,
            type = OscilloscopeCorrelationEvent.Type.COMMUNICATION,
            label = "Connection ${event.connected}",
            details = event.transport,
        )
        else -> null
    }
}
