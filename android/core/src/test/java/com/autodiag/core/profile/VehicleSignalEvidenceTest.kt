package com.autodiag.core.profile

import com.autodiag.core.diagnostic.DiagnosticEvidenceStore
import com.autodiag.core.diagnostic.EvidenceSource
import com.autodiag.core.diagnostic.EvidenceVerification
import org.junit.Assert.assertEquals
import org.junit.Test

class VehicleSignalEvidenceTest {
    @Test
    fun preserves_profile_provenance_and_raw_bytes() {
        val definition = VehicleSignalDefinition(
            name = "SOC",
            shortName = "soc",
            request = "22F00F",
            unit = "%",
            scale = 0.5,
            verification = EvidenceVerification.VERIFIED,
            sourceProfile = "toyota-prius-phv",
        )
        val sample = VehicleSignalDecoder.decode(definition, byteArrayOf(50)).getOrThrow()
        val store = DiagnosticEvidenceStore()
        store.appendVehicleSignal(sample, 1234L, ecuId = "BMS")

        val evidence = store.snapshot().single()
        assertEquals(25.0, evidence.value)
        assertEquals(EvidenceSource.VEHICLE_PROFILE, evidence.provenance.source)
        assertEquals("toyota-prius-phv", evidence.provenance.vehicleProfile)
        assertEquals("BMS", evidence.provenance.ecuId)
        assertEquals("32", evidence.provenance.rawRepresentation)
        assertEquals(EvidenceVerification.VERIFIED, evidence.verification)
    }
}
