package com.autodiag.core.oscilloscope

/** Capture configuration for a single-shot waveform around a trigger event. */
data class OscilloscopeCaptureConfig(
    val channel: Int,
    val sampleRateHz: Long,
    val preTriggerSamples: Int,
    val postTriggerSamples: Int,
    val trigger: OscilloscopeTrigger? = null
) {
    init {
        require(channel >= 0)
        require(sampleRateHz > 0)
        require(preTriggerSamples >= 0)
        require(postTriggerSamples > 0)
    }
}

/** Result of a completed triggered capture. */
data class OscilloscopeTriggeredCapture(
    val capture: OscilloscopeCapture,
    val triggerSampleIndex: Int?
)

/**
 * Streaming capture engine. Samples are retained in a rolling pre-trigger buffer
 * until the configured edge is detected, then post-trigger samples are collected.
 */
class OscilloscopeCaptureEngine(private val config: OscilloscopeCaptureConfig) {
    private val preBuffer = OscilloscopeRingBuffer(maxOf(1, config.preTriggerSamples))
    private val captured = ArrayList<OscilloscopeSample>(config.preTriggerSamples + config.postTriggerSamples + 1)
    private var previous: OscilloscopeSample? = null
    private var triggered = config.trigger == null
    private var triggerIndex: Int? = if (triggered) config.preTriggerSamples else null
    private var complete = false

    fun reset() {
        preBuffer.clear()
        captured.clear()
        previous = null
        triggered = config.trigger == null
        triggerIndex = if (triggered) config.preTriggerSamples else null
        complete = false
    }

    /** Adds one sample. Returns a completed capture exactly once. */
    fun add(sample: OscilloscopeSample): OscilloscopeTriggeredCapture? {
        if (complete) return null

        if (!triggered) {
            val old = previous
            previous = sample
            if (old != null && config.trigger != null &&
                OscilloscopeTriggerDetector.crossed(old, sample, config.trigger)
            ) {
                triggered = true
                val history = preBuffer.toList().takeLast(config.preTriggerSamples)
                captured.addAll(history)
                captured.add(sample)
                triggerIndex = captured.lastIndex
            } else {
                preBuffer.add(sample)
            }
            return null
        }

        if (captured.isEmpty() || captured.last() != sample) captured.add(sample)
        if (captured.size >= config.preTriggerSamples + config.postTriggerSamples + 1) {
            complete = true
            return OscilloscopeTriggeredCapture(
                capture = OscilloscopeCapture(config.channel, captured.toList(), config.sampleRateHz, config.trigger),
                triggerSampleIndex = triggerIndex
            )
        }
        return null
    }

    fun isTriggered(): Boolean = triggered
    fun isComplete(): Boolean = complete
}
