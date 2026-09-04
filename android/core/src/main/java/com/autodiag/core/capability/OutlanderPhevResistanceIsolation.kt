package com.autodiag.core.capability

/**
 * The two resistance concepts exposed by the Outlander PHEV diagnostic data
 * are intentionally represented separately. They must never be combined into
 * one generic "battery resistance" value.
 */
enum class OutlanderResistanceMeasurement {
    INTERNAL_BATTERY_RESISTANCE,
    HV_ISOLATION_RESISTANCE
}

enum class OutlanderVerification {
    UNVERIFIED,
    PARTIALLY_VERIFIED,
    VERIFIED
}

data class OutlanderResistanceLimit(
    val minimum: Double? = null,
    val maximum: Double? = null,
    val unit: String,
    val source: String,
    val authoritative: Boolean = false
)

data class OutlanderResistanceIsolationSample(
    val measurement: OutlanderResistanceMeasurement,
    /** Value is kept as Double so decimal precision is not lost. */
    val value: Double,
    val unit: String,
    val timestampEpochMs: Long,
    val ecuIdentity: String? = null,
    val rawRequest: String? = null,
    val rawResponse: String? = null,
    val source: String? = null,
    val verification: OutlanderVerification = OutlanderVerification.UNVERIFIED
)

data class OutlanderResistanceIsolationSession(
    val measurement: OutlanderResistanceMeasurement,
    val unit: String,
    val current: Double? = null,
    val sessionMin: Double? = null,
    val sessionMax: Double? = null,
    val sampleCount: Int = 0,
    val lastTimestampEpochMs: Long? = null,
    val ecuIdentity: String? = null,
    val verification: OutlanderVerification = OutlanderVerification.UNVERIFIED,
    val limit: OutlanderResistanceLimit? = null
) {
    /** Manufacturer/service limits are never inferred from observed min/max. */
    fun status(): String = when {
        current == null -> "UNAVAILABLE"
        limit == null || !limit.authoritative -> "MEASURED_LIMIT_UNKNOWN"
        limit.minimum != null && current < limit.minimum -> "CRITICAL"
        limit.maximum != null && current > limit.maximum -> "CRITICAL"
        else -> "WITHIN_VERIFIED_LIMIT"
    }

    companion object {
        fun empty(measurement: OutlanderResistanceMeasurement, unit: String) =
            OutlanderResistanceIsolationSession(
                measurement = measurement,
                unit = unit
            )
    }
}

/** Aggregates a read-only diagnostic stream for one measurement session. */
class OutlanderResistanceIsolationSessionAggregator(
    private val measurement: OutlanderResistanceMeasurement,
    private val unit: String
) {
    private var state = OutlanderResistanceIsolationSession.empty(measurement, unit)

    fun add(sample: OutlanderResistanceIsolationSample): OutlanderResistanceIsolationSession {
        require(sample.measurement == measurement) { "Measurement type mismatch" }
        require(sample.unit == unit) { "Unit mismatch" }
        require(sample.value.isFinite()) { "Resistance value must be finite" }

        val min = state.sessionMin?.let { kotlin.math.min(it, sample.value) } ?: sample.value
        val max = state.sessionMax?.let { kotlin.math.max(it, sample.value) } ?: sample.value
        val verification = when {
            sample.verification == OutlanderVerification.VERIFIED -> OutlanderVerification.VERIFIED
            sample.verification == OutlanderVerification.PARTIALLY_VERIFIED ||
                state.verification == OutlanderVerification.PARTIALLY_VERIFIED -> OutlanderVerification.PARTIALLY_VERIFIED
            else -> state.verification
        }

        state = state.copy(
            current = sample.value,
            sessionMin = min,
            sessionMax = max,
            sampleCount = state.sampleCount + 1,
            lastTimestampEpochMs = sample.timestampEpochMs,
            ecuIdentity = sample.ecuIdentity ?: state.ecuIdentity,
            verification = verification
        )
        return state
    }

    fun current(): OutlanderResistanceIsolationSession = state

    fun reset() {
        state = OutlanderResistanceIsolationSession.empty(measurement, unit)
    }
}
