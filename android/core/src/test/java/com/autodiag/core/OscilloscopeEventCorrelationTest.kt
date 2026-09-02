package com.autodiag.core

import com.autodiag.core.oscilloscope.OscilloscopeCapture
import com.autodiag.core.oscilloscope.OscilloscopeCorrelationEvent
import com.autodiag.core.oscilloscope.OscilloscopeEventCorrelator
import com.autodiag.core.oscilloscope.OscilloscopeSample
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OscilloscopeEventCorrelationTest {
    private fun capture() = OscilloscopeCapture(
        channel = 0,
        samples = listOf(
            OscilloscopeSample(1_000L, 0.0),
            OscilloscopeSample(2_000L, 1.0),
            OscilloscopeSample(3_000L, 0.0),
        ),
        sampleRateHz = 1_000_000L,
        trigger = null,
    )

    @Test
    fun correlatesAndSortsMarkers() {
        val result = OscilloscopeEventCorrelator.correlate(
            capture(),
            listOf(
                OscilloscopeCorrelationEvent(3_500L, OscilloscopeCorrelationEvent.Type.DTC, "P0300"),
                OscilloscopeCorrelationEvent(1_500L, OscilloscopeCorrelationEvent.Type.CAN_FRAME, "0x123"),
            ),
        )

        assertEquals(listOf(500L, 2_500L), result.map { it.offsetFromCaptureStartNanos })
        assertTrue(result.all { it.inCapture })
    }

    @Test
    fun marksEventsOutsideCapture() {
        val result = OscilloscopeEventCorrelator.correlate(
            capture(),
            listOf(OscilloscopeCorrelationEvent(5_000L, OscilloscopeCorrelationEvent.Type.UDS_RESPONSE, "0x62")),
        )

        assertEquals(1, result.size)
        assertFalse(result.single().inCapture)
        assertEquals(4_000L, result.single().offsetFromCaptureStartNanos)
    }
}
