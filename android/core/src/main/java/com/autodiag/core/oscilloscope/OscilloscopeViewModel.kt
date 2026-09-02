package com.autodiag.core.oscilloscope

/**
 * View configuration for an automotive waveform viewer.
 * The core layer stores physical values; Android UI can map these to pixels.
 */
data class OscilloscopeViewConfig(
    val timeDivNanos: Long = 1_000_000L,
    val voltsDiv: Double = 1.0,
    val verticalOffsetVolts: Double = 0.0,
    val horizontalOffsetNanos: Long = 0L,
    val gridDivisionsX: Int = 10,
    val gridDivisionsY: Int = 8
) {
    init {
        require(timeDivNanos > 0)
        require(voltsDiv > 0.0)
        require(gridDivisionsX > 0)
        require(gridDivisionsY > 0)
    }

    fun visibleDurationNanos(): Long = timeDivNanos * gridDivisionsX
    fun visibleAmplitudeVolts(): Double = voltsDiv * gridDivisionsY
}

/** Cursor used for precise waveform measurements in the viewer. */
data class OscilloscopeCursor(
    val timestampNanos: Long,
    val voltage: Double
)

/** Difference between two measurement cursors. */
data class OscilloscopeCursorMeasurement(
    val deltaTimeNanos: Long,
    val deltaVoltage: Double
) {
    val frequencyHz: Double?
        get() = if (deltaTimeNanos > 0) 1_000_000_000.0 / deltaTimeNanos else null
}

object OscilloscopeCursorMath {
    fun measure(first: OscilloscopeCursor, second: OscilloscopeCursor): OscilloscopeCursorMeasurement =
        OscilloscopeCursorMeasurement(
            deltaTimeNanos = kotlin.math.abs(second.timestampNanos - first.timestampNanos),
            deltaVoltage = second.voltage - first.voltage
        )
}
