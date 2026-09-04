package com.autodiag.core.capability

/**
 * Source-derived decoders from the analysed PHEV Watchdog APK.
 *
 * The indexes intentionally remain explicit. The Watchdog parser includes a
 * three-character CAN header token in its response array when present, so
 * these offsets must only be applied to the same normalized response layout.
 */
object OutlanderPhevResistanceDecoder {
    /** Watchdog z3/a, command 21 01: bytes 78..79, unsigned 16-bit, kΩ. */
    fun decodeIsolationResistance(response: IntArray): Double {
        require(response.size > 79) { "Outlander isolation response is too short" }
        return ((response[78] and 0xFF) * 256 + (response[79] and 0xFF)).toDouble()
    }

    /**
     * Watchdog e4/a, command 21 01: byte 38, source-labelled MΩ.
     * Retained for forensic/source comparison only; NOT a confirmed battery ESR.
     */
    fun decodeUnverifiedInternalResistanceMaximum(response: IntArray): Double {
        require(response.size > 38) { "Outlander battery response is too short" }
        return (response[38] and 0xFF) / 10.0
    }

    /** Watchdog e4/a, command 21 01: byte 39, source-labelled MΩ; meaning unverified. */
    fun decodeUnverifiedInternalResistanceMinimum(response: IntArray): Double {
        require(response.size > 39) { "Outlander battery response is too short" }
        return (response[39] and 0xFF) / 10.0
    }

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
