package com.autodiag.core.capability

/**
 * Measurement domains deliberately kept separate: battery internal resistance
 * is not the same quantity as HV electrical isolation resistance.
 */
enum class OutlanderResistanceKind(
    val signalId: String,
    val unit: String
) {
    INTERNAL_BATTERY_RESISTANCE("battery.internal_resistance", "MΩ"),
    HV_ISOLATION_RESISTANCE("battery.isolation_resistance", "kΩ")
}

enum class OutlanderMeasurementVerification {
    UNVERIFIED,
    PARTIALLY_VERIFIED,
    VERIFIED
}

data class OutlanderResistanceMeasurement(
    val kind: OutlanderResistanceKind,
    /** Preserve decoder precision; do not round to an integer. */
    val value: Double,
    val timestampEpochMs: Long,
    val ecuIdentity: EcuDataIdentity? = null,
    val rawRequest: String? = null,
    val rawResponse: String? = null,
    val verification: OutlanderMeasurementVerification = OutlanderMeasurementVerification.UNVERIFIED
) {
    val unit: String get() = kind.unit
}

data class OutlanderResistanceSessionStats(
    val kind: OutlanderResistanceKind,
    val current: Double? = null,
    val minimum: Double? = null,
    val maximum: Double? = null,
    val sampleCount: Int = 0,
    val lastTimestampEpochMs: Long? = null,
    val verification: OutlanderMeasurementVerification = OutlanderMeasurementVerification.UNVERIFIED
) {
    val unit: String get() = kind.unit

    fun accept(measurement: OutlanderResistanceMeasurement): OutlanderResistanceSessionStats {
        require(measurement.kind == kind) { "Measurement kind mismatch" }
        return copy(
            current = measurement.value,
            minimum = minimum?.let { minOf(it, measurement.value) } ?: measurement.value,
            maximum = maximum?.let { maxOf(it, measurement.value) } ?: measurement.value,
            sampleCount = sampleCount + 1,
            lastTimestampEpochMs = measurement.timestampEpochMs,
            verification = when {
                measurement.verification == OutlanderMeasurementVerification.VERIFIED -> OutlanderMeasurementVerification.VERIFIED
                verification == OutlanderMeasurementVerification.VERIFIED -> verification
                measurement.verification == OutlanderMeasurementVerification.PARTIALLY_VERIFIED -> OutlanderMeasurementVerification.PARTIALLY_VERIFIED
                else -> verification
            }
        )
    }
}
