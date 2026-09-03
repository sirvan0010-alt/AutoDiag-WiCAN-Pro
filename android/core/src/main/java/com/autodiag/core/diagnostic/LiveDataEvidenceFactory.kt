package com.autodiag.core.diagnostic

import com.autodiag.core.obd.LiveDataQuality
import com.autodiag.core.obd.LiveDataSample

/** Converts a decoded Mode 01 sample into the common diagnostic evidence model. */
object LiveDataEvidenceFactory {
    fun fromMode01(
        sample: LiveDataSample,
        verification: EvidenceVerification = EvidenceVerification.UNVERIFIED,
        ecuId: String? = null,
        sourceId: String? = null
    ): DiagnosticEvidence<Double> {
        val availability = when (sample.quality) {
            LiveDataQuality.GOOD -> EvidenceAvailability.AVAILABLE
            LiveDataQuality.UNAVAILABLE -> EvidenceAvailability.UNAVAILABLE
            LiveDataQuality.ERROR -> EvidenceAvailability.ERROR
            LiveDataQuality.INVALID -> EvidenceAvailability.ERROR
        }

        return DiagnosticEvidence(
            key = "obd.mode01.pid.%02X".format(sample.pid),
            value = sample.value,
            unit = sample.unit,
            timestampEpochMs = sample.timestampEpochMs,
            availability = availability,
            verification = verification,
            provenance = EvidenceProvenance(
                source = EvidenceSource.OBD_MODE_01,
                sourceId = sourceId,
                ecuId = ecuId,
                rawRepresentation = sample.rawHex
            ),
            isDerived = false,
            quality = "${sample.quality.name}:${sample.freshness.name}",
            note = sample.error
        )
    }
}
