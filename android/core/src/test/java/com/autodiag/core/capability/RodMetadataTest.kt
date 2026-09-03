package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RodMetadataTest {
    @Test
    fun parsesObservedSectionsWithoutInventingPayloadSemantics() {
        val entry = RodMetadataParser.parse(
            "EV_DeckLidContrUnit.rod",
            listOf("[CMP]", "[DTC]", "[MWB]", "[GES]", "[SOT]", "[XPL]")
        )

        assertTrue("DTC" in entry.sections)
        assertTrue(RodFeatureKind.DTC in entry.featureKinds)
        assertTrue(RodFeatureKind.MEASURED_VALUES in entry.featureKinds)
        assertTrue(RodFeatureKind.ADJUSTMENT in entry.featureKinds)
        assertTrue("SOT" in entry.sections)
        assertEquals(VerificationState.PARTIALLY_VERIFIED, entry.verification)
    }

    @Test
    fun databaseEvidenceNeverClaimsLiveAvailability() {
        val entry = RodMetadataParser.parse("EV_DashBoardVISMQB37W_005_SK38P.rod", listOf("DTC", "MWB"))
        val capability = RodCapabilityEvidence.capabilityStatus(entry, RodFeatureKind.DTC)

        assertEquals(CapabilityStatus.PARTIAL, capability.status)
        assertEquals(VerificationState.PARTIALLY_VERIFIED, capability.verification)
    }
}
