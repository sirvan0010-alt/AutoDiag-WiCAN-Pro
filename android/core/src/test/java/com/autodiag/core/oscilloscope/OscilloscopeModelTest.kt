package com.autodiag.core.oscilloscope

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OscilloscopeModelTest {
    @Test
    fun captureCalculatesBasicMeasurements() {
        val capture = OscilloscopeCapture(
            channel = 0,
            sampleRateHz = 10_000,
            samples = listOf(
                OscilloscopeSample(0, 1.0),
                OscilloscopeSample(100_000, 3.0),
                OscilloscopeSample(200_000, 2.0)
            )
        )

        assertEquals(200_000L, capture.durationNanos)
        assertEquals(1.0, capture.minVolts)
        assertEquals(3.0, capture.maxVolts)
        assertEquals(2.0, capture.peakToPeakVolts)
        assertEquals(2.0, capture.meanVolts)
    }

    @Test
    fun triggerDetectsRisingAndFallingEdges() {
        val rising = OscilloscopeTrigger(2.0, OscilloscopeTriggerSlope.RISING)
        val falling = OscilloscopeTrigger(2.0, OscilloscopeTriggerSlope.FALLING)
        val low = OscilloscopeSample(0, 1.0)
        val high = OscilloscopeSample(1, 3.0)

        assertTrue(OscilloscopeTriggerDetector.crossed(low, high, rising))
        assertTrue(OscilloscopeTriggerDetector.crossed(high, low, falling))
        assertFalse(OscilloscopeTriggerDetector.crossed(high, low, rising))
    }
}
