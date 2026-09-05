package com.autodiag.core.capability

/**
 * Compatibility facade for the current Outlander UI path.
 *
 * The actual byte decoding primitive is generic and lives in DataDrivenDecoder;
 * vehicle-specific decoder definitions are maintained as provenance data.
 */
object OutlanderPhevResistanceDecoder {
    private val isolation = DataDecoderSpec(
        kind = DataDecoderSpec.Kind.UNSIGNED_U16_BE,
        start = 78,
        end = 79,
        scale = 1.0,
        unit = "kΩ"
    )

    private val internalMaximum = DataDecoderSpec(
        kind = DataDecoderSpec.Kind.UNSIGNED_U8,
        start = 38,
        scale = 0.1,
        unit = "MΩ"
    )

    private val internalMinimum = DataDecoderSpec(
        kind = DataDecoderSpec.Kind.UNSIGNED_U8,
        start = 39,
        scale = 0.1,
        unit = "MΩ"
    )

    /** Watchdog Lz3/a, 21 01: unsigned 16-bit BE, tokens 78..79, kΩ. */
    fun decodeIsolationResistance(response: IntArray): Double =
        DataDrivenDecoder.decode(response, isolation)

    /** Watchdog Le4/a, 21 01: unsigned byte 38, source-labelled MΩ. */
    fun decodeUnverifiedInternalResistanceMaximum(response: IntArray): Double =
        DataDrivenDecoder.decode(response, internalMaximum)

    /** Watchdog Le4/a, 21 01: unsigned byte 39, source-labelled MΩ. */
    fun decodeUnverifiedInternalResistanceMinimum(response: IntArray): Double =
        DataDrivenDecoder.decode(response, internalMinimum)

    fun decodeIsolationMeasurement(
        response: IntArray,
        timestampEpochMs: Long,
        ecuIdentity: EcuDataIdentity? = null,
        rawRequest: String? = "21 01",
        rawResponse: String? = null
    ): OutlanderResistanceMeasurement = OutlanderResistanceMeasurement(
        kind = OutlanderResistanceKind.HV_ISOLATION_RESISTANCE,
        value = decodeIsolationResistance(response),
        timestampEpochMs = timestampEpochMs,
        ecuIdentity = ecuIdentity,
        rawRequest = rawRequest,
        rawResponse = rawResponse,
        verification = OutlanderMeasurementVerification.PARTIALLY_VERIFIED
    )
}
