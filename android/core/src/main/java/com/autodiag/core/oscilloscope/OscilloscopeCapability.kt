package com.autodiag.core.oscilloscope

/** Hardware capability exposed by an adapter or an additional oscilloscope module. */
enum class OscilloscopeCapability {
    AVAILABLE,
    AVAILABLE_WITH_ADDITIONAL_HARDWARE,
    NOT_SUPPORTED,
    UNKNOWN
}

/** Describes the measurable signal path without claiming unsupported hardware. */
data class OscilloscopeChannelCapability(
    val channelCount: Int,
    val maxVoltage: Double,
    val minVoltage: Double = 0.0,
    val maxSampleRateHz: Long,
    val resolutionBits: Int? = null,
    val supportsAcCoupling: Boolean = false,
    val supportsDcCoupling: Boolean = true
) {
    init {
        require(channelCount > 0)
        require(maxVoltage > minVoltage)
        require(maxSampleRateHz > 0)
        require(resolutionBits == null || resolutionBits > 0)
    }
}
