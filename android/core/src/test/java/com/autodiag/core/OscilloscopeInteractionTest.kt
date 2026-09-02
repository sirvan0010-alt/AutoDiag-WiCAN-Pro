package com.autodiag.core

import com.autodiag.core.oscilloscope.OscilloscopeInteractionState
import com.autodiag.core.oscilloscope.OscilloscopeViewConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OscilloscopeInteractionTest {
    private val base = OscilloscopeInteractionState(
        OscilloscopeViewConfig(
            timeDivNanos = 1_000L,
            voltsDiv = 1.0,
            verticalOffsetVolts = 0.0,
            horizontalOffsetNanos = 0L,
        )
    )

    @Test
    fun zoomChangesBothScales() {
        val zoomed = base.zoom(0.5, 2.0)
        assertEquals(500L, zoomed.config.timeDivNanos)
        assertEquals(2.0, zoomed.config.voltsDiv)
    }

    @Test
    fun panAndFreezeAreIndependent() {
        val moved = base.pan(250L, -0.5).toggleFreeze()
        assertEquals(250L, moved.config.horizontalOffsetNanos)
        assertEquals(-0.5, moved.config.verticalOffsetVolts)
        assertTrue(moved.frozen)
    }
}
