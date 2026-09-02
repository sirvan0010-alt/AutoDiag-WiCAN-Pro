package com.autodiag.core.oscilloscope

/** Immutable viewport interaction state for touch/mouse waveform navigation. */
data class OscilloscopeInteractionState(
    val config: OscilloscopeViewConfig,
    val frozen: Boolean = false,
) {
    init {
        require(config.timeDivNanos > 0L)
        require(config.voltsDiv > 0.0)
    }

    fun zoom(timeFactor: Double, voltageFactor: Double): OscilloscopeInteractionState {
        require(timeFactor > 0.0)
        require(voltageFactor > 0.0)
        return copy(
            config = config.copy(
                timeDivNanos = (config.timeDivNanos * timeFactor).toLong().coerceAtLeast(1L),
                voltsDiv = (config.voltsDiv * voltageFactor).coerceAtLeast(Double.MIN_VALUE),
            )
        )
    }

    fun pan(timeDeltaNanos: Long, voltageDelta: Double): OscilloscopeInteractionState = copy(
        config = config.copy(
            horizontalOffsetNanos = config.horizontalOffsetNanos + timeDeltaNanos,
            verticalOffsetVolts = config.verticalOffsetVolts + voltageDelta,
        )
    )

    fun toggleFreeze(): OscilloscopeInteractionState = copy(frozen = !frozen)
}
