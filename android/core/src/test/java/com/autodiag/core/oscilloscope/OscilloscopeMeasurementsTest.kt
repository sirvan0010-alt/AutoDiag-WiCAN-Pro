package com.autodiag.core.oscilloscope

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

class OscilloscopeMeasurementsTest {
    @Test
    fun calculatesRmsAndPwmMeasurements() {
        val samples = listOf(
            OscilloscopeSample(0, 0.0),
            OscilloscopeSample(250_000, 5.0),
            OscilloscopeSample(500_000, 5.0),
            OscilloscopeSample(750_000, 0.0),
            OscilloscopeSample(1_000_000, 0.0),
            OscilloscopeSample(1_250_000, 5.0),
            OscilloscopeSample(1_500_000, 5.0)
        )
        val capture = OscilloscopeCapture(
            channel = 0,
            samples = samples,
            sampleRateHz = 4_000,
            trigger = OscilloscopeTrigger(2.5, OscilloscopeTriggerSlope.RISING)
        )

        val result = OscilloscopeMeasurementEngine.calculate(capture)
        assertEquals(0.0, result.minVolts)
        assertEquals(5.0, result.maxVolts)
        assertEquals(2.5, result.peakToPeakVolts)
        assertEquals(sqrt(75.0 / 7.0), result.rmsVolts!!, 1e-10)
        assertEquals(50.0, result.dutyCyclePercent!!, 1e-10)
    }

    @Test
    fun calculatesFrequencyFromRepeatedRisingEdges() {
        val samples = listOf(
            OscilloscopeSample(0, 0.0),
            OscilloscopeSample(1_000_000, 5.0),
            OscilloscopeSample(2_000_000, 0.0),
            OscilloscopeSample(3_000_000, 5.0),
            OscilloscopeSample(4_000_000, 0.0),
            OscilloscopeSample(5_000_000, 5.0)
        )
        val capture = OscilloscopeCapture(
            channel = 0,
            samples = samples,
            sampleRateHz = 1_000,
            trigger = OscilloscopeTrigger(2.5, OscilloscopeTriggerSlope.RISING)
        )
        val result = OscilloscopeMeasurementEngine.calculate(capture)
        assertEquals(500.0, result.frequencyHz!!, 1e-10)
        assertEquals(0.002, result.periodSeconds!!, 1e-10)
    }
}
