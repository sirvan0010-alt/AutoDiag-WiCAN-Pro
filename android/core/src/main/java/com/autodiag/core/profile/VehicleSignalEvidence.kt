package com.autodiag.core.profile

import com.autodiag.core.diagnostic.DiagnosticEvidence
import com.autodiag.core.diagnostic.DiagnosticEvidenceStore
import com.autodiag.core.diagnostic.EvidenceAvailability
import com.autodiag.core.diagnostic.EvidenceProvenance
import com.autodiag.core.diagnostic.EvidenceSource

/** Converts a vehicle signal sample into the common evidence model. */
object VehicleSignalEvidenceFactory {
    fun fromSample(
        sample: VehicleSignalSample,
        timestampEpochMs: Long,
        vehicleProfile: String? = sample.definition.sourceProfile,
        ecuId: String? = null,
        sourceId: String? = null,
    ): DiagnosticEvidence<Double> = DiagnosticEvidence(
        key = "vehicle.signal.${sample.definition.shortName}",
        value = sample.value,
        unit = sample.definition.unit,
        timestampEpochMs = timestampEpochMs,
        availability = EvidenceAvailability.AVAILABLE,
        verification = sample.definition.verification,
        provenance = EvidenceProvenance(
            source = EvidenceSource.VEHICLE_PROFILE,
            sourceId = sourceId ?: sample.definition.request,
            ecuId = ecuId,
            vehicleProfile = vehicleProfile,
            rawRepresentation = sample.rawBytes.joinToString(" ") { "%02X".format(it.toInt() and 0xFF) },
        ),
        quality = "raw=${sample.rawValue}",
    )
}

fun DiagnosticEvidenceStore.appendVehicleSignal(
    sample: VehicleSignalSample,
    timestampEpochMs: Long,
    vehicleProfile: String? = sample.definition.sourceProfile,
    ecuId: String? = null,
    sourceId: String? = null,
) {
    append(VehicleSignalEvidenceFactory.fromSample(sample, timestampEpochMs, vehicleProfile, ecuId, sourceId))
}
