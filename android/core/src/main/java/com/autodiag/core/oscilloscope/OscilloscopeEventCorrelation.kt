package com.autodiag.core.oscilloscope

/** A timestamped diagnostic event that can be overlaid on an oscilloscope capture. */
data class OscilloscopeCorrelationEvent(
    val timestampNanos: Long,
    val type: Type,
    val label: String,
    val details: String? = null,
) {
    enum class Type {
        CAN_FRAME,
        UDS_REQUEST,
        UDS_RESPONSE,
        DTC,
        COMMUNICATION,
        OTHER,
    }
}

data class OscilloscopeEventMarker(
    val event: OscilloscopeCorrelationEvent,
    val offsetFromCaptureStartNanos: Long,
    val inCapture: Boolean,
)

/**
 * Maps diagnostic timestamps onto an analog capture without changing either
 * source's timestamps. This keeps correlation deterministic for live and replay data.
 */
object OscilloscopeEventCorrelator {
    fun correlate(
        capture: OscilloscopeCapture,
        events: Iterable<OscilloscopeCorrelationEvent>,
    ): List<OscilloscopeEventMarker> {
        val start = capture.samples.firstOrNull()?.timestampNanos ?: return emptyList()
        val end = capture.samples.lastOrNull()?.timestampNanos ?: return emptyList()

        return events
            .sortedBy { it.timestampNanos }
            .map { event ->
                OscilloscopeEventMarker(
                    event = event,
                    offsetFromCaptureStartNanos = event.timestampNanos - start,
                    inCapture = event.timestampNanos in start..end,
                )
            }
    }
}
