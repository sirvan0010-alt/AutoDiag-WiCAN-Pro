package com.autodiag.core.oscilloscope

/** A synchronized set of captures sharing one time reference. */
data class OscilloscopeMultiChannelCapture(
    val captures: List<OscilloscopeCapture>,
    val referenceStartNanos: Long? = captures.minOfOrNull { it.samples.firstOrNull()?.timestampNanos ?: Long.MAX_VALUE }
) {
    init {
        require(captures.isNotEmpty())
        require(captures.map { it.channel }.distinct().size == captures.size)
    }

    /** Returns each channel's start offset from the common reference clock. */
    fun startOffsetsNanos(): Map<Int, Long> = captures.associate { capture ->
        val start = capture.samples.firstOrNull()?.timestampNanos ?: referenceStartNanos ?: 0L
        capture.channel to (start - (referenceStartNanos ?: start))
    }
}

/** Aligns a channel's samples onto the capture reference without resampling. */
object OscilloscopeChannelAlignment {
    fun offsetSamples(
        capture: OscilloscopeCapture,
        referenceStartNanos: Long,
    ): List<OscilloscopeSample> = capture.samples.map { sample ->
        sample.copy(timestampNanos = sample.timestampNanos - referenceStartNanos)
    }
}
