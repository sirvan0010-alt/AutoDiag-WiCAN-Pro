package com.autodiag.core

import com.autodiag.core.oscilloscope.OscilloscopeCapture
import com.autodiag.core.oscilloscope.OscilloscopeMultiChannelCapture
import com.autodiag.core.oscilloscope.OscilloscopeSample
import kotlin.test.Test
import kotlin.test.assertEquals

class OscilloscopeMultiChannelTest {
    private fun capture(channel: Int, start: Long) = OscilloscopeCapture(
        channel = channel,
        samples = listOf(OscilloscopeSample(start, 0.0), OscilloscopeSample(start + 100L, 1.0)),
        sampleRateHz = 10_000L,
    )

    @Test
    fun exposesCommonStartOffsets() {
        val grouped = OscilloscopeMultiChannelCapture(listOf(capture(0, 1_000L), capture(1, 1_250L)))
        assertEquals(mapOf(0 to 0L, 1 to 250L), grouped.startOffsetsNanos())
    }
}
