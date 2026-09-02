package com.autodiag.core

import com.autodiag.core.oscilloscope.OscilloscopeCapture
import com.autodiag.core.oscilloscope.OscilloscopeCsv
import com.autodiag.core.oscilloscope.OscilloscopeReplay
import com.autodiag.core.oscilloscope.OscilloscopeSample
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OscilloscopeCsvTest {
    private val capture = OscilloscopeCapture(
        channel = 1,
        samples = listOf(
            OscilloscopeSample(100L, 0.25),
            OscilloscopeSample(200L, -1.5),
        ),
        sampleRateHz = 20_000L,
    )

    @Test
    fun exportsHeaderAndSamples() {
        val csv = OscilloscopeCsv.export(capture)
        assertTrue(csv.startsWith("timestamp_nanos,voltage_volts\n"))
        assertTrue(csv.contains("100,0.25"))
        assertTrue(csv.contains("200,-1.5"))
    }

    @Test
    fun replayRoundTrips() {
        val decoded = OscilloscopeReplay.decode(OscilloscopeReplay.encode(capture))
        assertEquals(capture.channel, decoded.channel)
        assertEquals(capture.sampleRateHz, decoded.sampleRateHz)
        assertEquals(capture.samples, decoded.samples)
    }
}
