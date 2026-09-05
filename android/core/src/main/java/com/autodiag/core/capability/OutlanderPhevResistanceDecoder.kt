package com.autodiag.core.capability

/**
 * Compatibility facade for the current Outlander UI path.
 *
 * Vehicle-specific byte positions and formulas are supplied by diagnostic-data.
 * This class intentionally contains no Outlander-specific byte offsets.
 */
object OutlanderPhevResistanceDecoder {
    fun decode(definition: SignalDecoderDefinition, response: IntArray): Double =
        DataDrivenDecoder.decode(response, definition.decoder)

    fun decodeMeasurement(
        definition: SignalDecoderDefinition,
        response: IntArray,
        timestampEpochMs: Long,
        ecuIdentity: EcuDataIdentity? = null,
        rawRequest: String? = definition.request,
        rawResponse: String? = null,
        verification: OutlanderMeasurementVerification = OutlanderMeasurementVerification.UNVERIFIED
    ): OutlanderResistanceMeasurement {
        val kind = when (definition.signalId) {
            "battery.isolation_resistance" -> OutlanderResistanceKind.HV_ISOLATION_RESISTANCE
            "battery.internal_resistance.max" -> OutlanderResistanceKind.INTERNAL_RESISTANCE_MAX_UNVERIFIED
            "battery.internal_resistance.min" -> OutlanderResistanceKind.INTERNAL_RESISTANCE_MIN_UNVERIFIED
            else -> throw IllegalArgumentException("Unsupported Outlander resistance signal: ${definition.signalId}")
        }
        return OutlanderResistanceMeasurement(
            kind = kind,
            value = decode(definition, response),
            timestampEpochMs = timestampEpochMs,
            ecuIdentity = ecuIdentity,
            rawRequest = rawRequest,
            rawResponse = rawResponse,
            verification = verification
        )
    }
}
