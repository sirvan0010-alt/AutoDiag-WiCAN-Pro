package com.autodiag.core.oscilloscope

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OscilloscopeViewModelTest {
    @Test
    fun visibleWindowUsesTimePerDivision() {
        val config = OscilloscopeViewConfig(timeDivNanos = 2_000_000L, gridDivisionsX = 10)
        assertEquals(20_000_000L, config.visibleDurationNanos())
        assertEquals(8.0, config.copy(voltsDiv = 0.5).visibleAmplitudeVolts())
    }

    @Test
    fun cursorMeasurementCalculatesTimeVoltageAndFrequency() {
        val first = OscilloscopeCursor(1_000_000L, 1.0)
        val second = OscilloscopeCursor(2_000_000L, 3.5)
        val measurement = OscilloscopeCursorMath.measure(first, second)

        assertEquals(1_000_000L, measurement.deltaTimeNanos)
        assertEquals(2.5, measurement.deltaVoltage)
        assertEquals(1_000.0, measurement.frequencyHz)
    }

    @Test
    fun identicalCursorsHaveNoFrequency() {
        val cursor = OscilloscopeCursor(10L, 2.0)
        assertNull(OscilloscopeCursorMath.measure(cursor, cursor).frequencyHz)
    }
}
