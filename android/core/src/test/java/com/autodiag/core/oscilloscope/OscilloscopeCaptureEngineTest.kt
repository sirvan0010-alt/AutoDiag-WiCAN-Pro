package com.autodiag.core.oscilloscope

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OscilloscopeCaptureEngineTest {
    @Test
    fun ringBufferKeepsNewestSamples() {
        val buffer = OscilloscopeRingBuffer(3)
        buffer.add(OscilloscopeSample(0, 0.0))
        buffer.add(OscilloscopeSample(1, 1.0))
        buffer.add(OscilloscopeSample(2, 2.0))
        buffer.add(OscilloscopeSample(3, 3.0))

        assertEquals(listOf(1L, 2L, 3L), buffer.toList().map { it.timestampNanos })
        assertEquals(listOf(2L, 3L), buffer.takeLast(2).map { it.timestampNanos })
    }

    @Test
    fun triggeredCaptureContainsPreAndPostTriggerSamples() {
        val config = OscilloscopeCaptureConfig(
            channel = 0,
            sampleRateHz = 10_000,
            preTriggerSamples = 2,
            postTriggerSamples = 3,
            trigger = OscilloscopeTrigger(2.0, OscilloscopeTriggerSlope.RISING)
        )
        val engine = OscilloscopeCaptureEngine(config)
        var result: OscilloscopeTriggeredCapture? = null

        listOf(0.0, 1.0, 1.2, 3.0, 4.0, 5.0, 6.0).forEachIndexed { index, voltage ->
            result = result ?: engine.add(OscilloscopeSample(index.toLong(), voltage))
        }

        val completed = assertNotNull(result)
        assertTrue(engine.isTriggered())
        assertTrue(engine.isComplete())
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), completed.capture.samples.map { it.timestampNanos })
        assertEquals(2, completed.triggerSampleIndex)
    }

    @Test
    fun triggerDoesNotFireBeforeCrossing() {
        val config = OscilloscopeCaptureConfig(
            channel = 1,
            sampleRateHz = 1_000,
            preTriggerSamples = 2,
            postTriggerSamples = 2,
            trigger = OscilloscopeTrigger(2.0, OscilloscopeTriggerSlope.RISING)
        )
        val engine = OscilloscopeCaptureEngine(config)
        assertFalse(engine.isTriggered())
        engine.add(OscilloscopeSample(0, 0.5))
        engine.add(OscilloscopeSample(1, 1.0))
        engine.add(OscilloscopeSample(2, 1.5))
        assertFalse(engine.isTriggered())
    }
}
