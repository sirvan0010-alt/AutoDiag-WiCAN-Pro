package com.autodiag.core.diagnostic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class DiagnosticEvidenceTest {
    @Test
    fun preservesAvailabilityVerificationAndProvenance() {
        val evidence = DiagnosticEvidence(
            key = "engine.rpm",
            value = 1726.0,
            unit = "ot/min",
            timestampEpochMs = 10_000L,
            availability = EvidenceAvailability.AVAILABLE,
            verification = EvidenceVerification.VERIFIED,
            provenance = EvidenceProvenance(
                source = EvidenceSource.OBD_MODE_01,
                ecuId = "engine",
                rawRepresentation = "41 0C 1A F8"
            )
        )

        assertEquals(1726.0, evidence.value)
        assertEquals(EvidenceAvailability.AVAILABLE, evidence.availability)
        assertEquals(EvidenceVerification.VERIFIED, evidence.verification)
        assertEquals("41 0C 1A F8", evidence.provenance.rawRepresentation)
        assertFalse(evidence.isDerived)
    }

    @Test
    fun unknownIsNotVerified() {
        val evidence = DiagnosticEvidence(
            key = "battery.cell.01",
            value = null,
            timestampEpochMs = 10_000L,
            availability = EvidenceAvailability.UNKNOWN,
            verification = EvidenceVerification.UNVERIFIED,
            provenance = EvidenceProvenance(EvidenceSource.VEHICLE_PROFILE)
        )

        assertEquals(EvidenceAvailability.UNKNOWN, evidence.availability)
        assertEquals(EvidenceVerification.UNVERIFIED, evidence.verification)
    }
}
