package com.autodiag.core.capability

/**
 * Three resistance-related values found in the analysed Outlander PHEV source.
 *
 * HV isolation has a source-derived meaning and unit. The two "internal
 * resistance" fields are deliberately retained for forensic comparison, but
 * their physical meaning is NOT established and they must not be presented as
 * confirmed battery ESR/internal resistance.
 */
enum class OutlanderResistanceKind(
    val signalId: String,
    val displayNameCs: String,
    val unit: String?,
    val meaningStatusCs: String
) {
    HV_ISOLATION_RESISTANCE("battery.isolation_resistance", "Izolační odpor HV systému", "kΩ", "Význam je odvozený ze zdroje; přesná topologie měření není potvrzena."),
    INTERNAL_RESISTANCE_MAX_UNVERIFIED("battery.internal_resistance_max_unverified", "Odpor – hodnota MAX (neověřený význam)", "MΩ", "Zdroj ji označuje jako internal resistance, ale nevíme, co fyzicky znamená. Nejde o potvrzený ESR baterie."),
    INTERNAL_RESISTANCE_MIN_UNVERIFIED("battery.internal_resistance_min_unverified", "Odpor – hodnota MIN (neověřený význam)", "MΩ", "Zdroj ji označuje jako internal resistance, ale nevíme, co fyzicky znamená. Nejde o potvrzený ESR baterie.")
}

enum class OutlanderMeasurementVerification { UNVERIFIED, PARTIALLY_VERIFIED, VERIFIED }

data class OutlanderResistanceMeasurement(
    val kind: OutlanderResistanceKind,
    val value: Double,
    val timestampEpochMs: Long,
    val ecuIdentity: EcuDataIdentity? = null,
    val rawRequest: String? = null,
    val rawResponse: String? = null,
    val verification: OutlanderMeasurementVerification = OutlanderMeasurementVerification.UNVERIFIED
) { val unit: String? get() = kind.unit }

data class OutlanderResistanceSessionStats(
    val kind: OutlanderResistanceKind,
    val current: Double? = null,
    val minimum: Double? = null,
    val maximum: Double? = null,
    val sampleCount: Int = 0,
    val lastTimestampEpochMs: Long? = null,
    val verification: OutlanderMeasurementVerification = OutlanderMeasurementVerification.UNVERIFIED
) {
    val unit: String? get() = kind.unit
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

data class OutlanderResistanceSample(
    val timestampEpochMs: Long,
    val value: Double,
    val verification: OutlanderMeasurementVerification
)

/** Bounded in-memory history for responsive live graphs. */
class OutlanderResistanceHistory(private val maxSamples: Int = 2_000) {
    private val samples = ArrayDeque<OutlanderResistanceSample>()
    fun add(sample: OutlanderResistanceSample) { samples.addLast(sample); while (samples.size > maxSamples) samples.removeFirst() }
    fun snapshot(): List<OutlanderResistanceSample> = samples.toList()
    fun clear() = samples.clear()
}

data class OutlanderLiveSamplingSettings(
    /** Requested logging/polling period. Actual ECU response rate may be slower. */
    val intervalMs: Long = 1_000L
) {
    companion object { val OPTIONS_MS = listOf(100L, 250L, 500L, 1_000L, 2_000L, 5_000L) }
}
