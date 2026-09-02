package com.autodiag.core.oscilloscope

/** A sampled point from one analog automotive signal. */
data class OscilloscopeSample(
    val timestampNanos: Long,
    val voltage: Double
)

/** Trigger modes supported by the generic signal-analysis layer. */
enum class OscilloscopeTriggerSlope { RISING, FALLING }

data class OscilloscopeTrigger(
    val levelVolts: Double,
    val slope: OscilloscopeTriggerSlope,
    val hysteresisVolts: Double = 0.0
) {
    init {
        require(hysteresisVolts >= 0.0)
    }
}

/** Time-domain capture independent of a particular hardware transport. */
data class OscilloscopeCapture(
    val channel: Int,
    val samples: List<OscilloscopeSample>,
    val sampleRateHz: Long,
    val trigger: OscilloscopeTrigger? = null
) {
    init {
        require(channel >= 0)
        require(sampleRateHz > 0)
    }

    val durationNanos: Long
        get() = if (samples.size < 2) 0L else samples.last().timestampNanos - samples.first().timestampNanos

    val minVolts: Double?
        get() = samples.minOfOrNull { it.voltage }

    val maxVolts: Double?
        get() = samples.maxOfOrNull { it.voltage }

    val peakToPeakVolts: Double?
        get() = minVolts?.let { min -> maxVolts?.let { max -> max - min } }

    val meanVolts: Double?
        get() = samples.takeIf { it.isNotEmpty() }?.map { it.voltage }?.average()
}

/** Simple edge trigger detector for streaming samples. */
object OscilloscopeTriggerDetector {
    fun crossed(previous: OscilloscopeSample, current: OscilloscopeSample, trigger: OscilloscopeTrigger): Boolean {
        val low = trigger.levelVolts - trigger.hysteresisVolts
        val high = trigger.levelVolts + trigger.hysteresisVolts
        return when (trigger.slope) {
            OscilloscopeTriggerSlope.RISING -> previous.voltage < low && current.voltage >= high
            OscilloscopeTriggerSlope.FALLING -> previous.voltage > high && current.voltage <= low
        }
    }
}
