package com.autodiag.core.oscilloscope

/** Derived measurements from a sampled waveform. Null means the capture is insufficient. */
data class OscilloscopeMeasurements(
    val minVolts: Double?,
    val maxVolts: Double?,
    val peakToPeakVolts: Double?,
    val meanVolts: Double?,
    val rmsVolts: Double?,
    val frequencyHz: Double?,
    val periodSeconds: Double?,
    val dutyCyclePercent: Double?
)

object OscilloscopeMeasurementEngine {
    fun calculate(capture: OscilloscopeCapture): OscilloscopeMeasurements {
        val samples = capture.samples
        val min = samples.minOfOrNull { it.voltage }
        val max = samples.maxOfOrNull { it.voltage }
        val mean = samples.takeIf { it.isNotEmpty() }?.map { it.voltage }?.average()
        val rms = samples.takeIf { it.isNotEmpty() }
            ?.let { values -> kotlin.math.sqrt(values.sumOf { it.voltage * it.voltage } / values.size) }

        val risingEdges = findRisingCrossings(samples, capture.trigger?.levelVolts ?: mean)
        val frequency = if (risingEdges.size >= 2) {
            val intervals = risingEdges.zipWithNext { a, b ->
                (b.timestampNanos - a.timestampNanos) / 1_000_000_000.0
            }.filter { it > 0.0 }
            intervals.takeIf { it.isNotEmpty() }?.let { 1.0 / (it.average()) }
        } else null

        val period = frequency?.let { 1.0 / it }
        val duty = if (capture.trigger != null && samples.size >= 2) {
            calculateDutyCycle(samples, capture.trigger.levelVolts)
        } else null

        return OscilloscopeMeasurements(
            minVolts = min,
            maxVolts = max,
            peakToPeakVolts = if (min != null && max != null) max - min else null,
            meanVolts = mean,
            rmsVolts = rms,
            frequencyHz = frequency,
            periodSeconds = period,
            dutyCyclePercent = duty
        )
    }

    private fun findRisingCrossings(
        samples: List<OscilloscopeSample>,
        level: Double?
    ): List<OscilloscopeSample> {
        if (level == null || samples.size < 2) return emptyList()
        return samples.zipWithNext()
            .filter { (previous, current) -> previous.voltage < level && current.voltage >= level }
            .map { it.second }
    }

    private fun calculateDutyCycle(samples: List<OscilloscopeSample>, level: Double): Double? {
        if (samples.size < 2) return null
        var highNanos = 0L
        var totalNanos = 0L
        samples.zipWithNext().forEach { (a, b) ->
            val delta = b.timestampNanos - a.timestampNanos
            if (delta <= 0) return@forEach
            totalNanos += delta
            if (a.voltage >= level) highNanos += delta
        }
        return if (totalNanos > 0) highNanos * 100.0 / totalNanos else null
    }
}
