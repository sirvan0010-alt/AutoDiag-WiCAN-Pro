package com.autodiag.core.profile

import com.autodiag.core.diagnostic.EvidenceAvailability
import com.autodiag.core.diagnostic.EvidenceVerification
import org.junit.Assert.assertEquals
import org.junit.Test

class VehicleProfileEvidenceTest {
    @Test
    fun equal_top_candidates_remain_ambiguous_and_fail_closed() {
        val profiles = listOf(
            VehicleSignalProfile(VehicleProfileIdentity("a", manufacturer = "VW", model = "Fabia", ecuId = "MED17")),
            VehicleSignalProfile(VehicleProfileIdentity("b", manufacturer = "VW", model = "Fabia", ecuId = "MED17")),
        )
        val selection = VehicleProfileSelector.select(ObservedEcuIdentity("VW", "Fabia", "MED17"), profiles)
        assertEquals(true, selection.ambiguous)
        assertEquals(null, selection.selected)
        val evidence = VehicleProfileEvidenceFactory.fromSelection(selection, 1L)
        assertEquals(EvidenceAvailability.PARTIAL, evidence.availability)
        assertEquals(EvidenceVerification.UNVERIFIED, evidence.verification)
    }
}
