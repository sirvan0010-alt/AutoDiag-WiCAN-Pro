package com.autodiag.core.profile

import com.autodiag.core.diagnostic.DiagnosticEvidence
import com.autodiag.core.diagnostic.DiagnosticEvidenceStore
import com.autodiag.core.diagnostic.EvidenceAvailability
import com.autodiag.core.diagnostic.EvidenceProvenance
import com.autodiag.core.diagnostic.EvidenceSource
import com.autodiag.core.diagnostic.EvidenceVerification

/** Result of conservative profile selection; ambiguity remains explicit. */
data class VehicleProfileSelection(
    val selected: ProfileMatch?,
    val candidates: List<ProfileMatch>,
    val ambiguous: Boolean,
)

object VehicleProfileSelector {
    fun select(observed: ObservedEcuIdentity, profiles: List<VehicleSignalProfile>): VehicleProfileSelection {
        val ranked = VehicleProfileMatcher.rank(observed, profiles)
        val best = ranked.firstOrNull()
        val ambiguous = best != null && ranked.drop(1).firstOrNull()?.score == best.score
        return VehicleProfileSelection(if (ambiguous) null else best, ranked, ambiguous)
    }
}

object VehicleProfileEvidenceFactory {
    fun fromSelection(
        selection: VehicleProfileSelection,
        timestampEpochMs: Long,
        sourceId: String? = null,
    ): DiagnosticEvidence<String> = DiagnosticEvidence(
        key = "vehicle.profile.selection",
        value = selection.selected?.profile?.identity?.id,
        timestampEpochMs = timestampEpochMs,
        availability = when {
            selection.ambiguous -> EvidenceAvailability.PARTIAL
            selection.selected != null -> EvidenceAvailability.AVAILABLE
            else -> EvidenceAvailability.UNKNOWN
        },
        verification = when {
            selection.ambiguous || selection.selected == null -> EvidenceVerification.UNVERIFIED
            else -> EvidenceVerification.PARTIALLY_VERIFIED
        },
        provenance = EvidenceProvenance(
            source = EvidenceSource.VEHICLE_PROFILE,
            sourceId = sourceId,
            vehicleProfile = selection.selected?.profile?.identity?.id,
        ),
        quality = "candidates=${selection.candidates.size};ambiguous=${selection.ambiguous}",
        note = if (selection.ambiguous) "Multiple profiles have equal identity-match score" else null,
    )
}

fun DiagnosticEvidenceStore.appendVehicleProfileSelection(
    selection: VehicleProfileSelection,
    timestampEpochMs: Long,
    sourceId: String? = null,
) {
    append(VehicleProfileEvidenceFactory.fromSelection(selection, timestampEpochMs, sourceId))
}
