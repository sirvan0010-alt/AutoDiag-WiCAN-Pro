package com.autodiag.core.diagnostic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class DiagnosticEvidenceJsonTest {
    @Test
    fun roundTripPreservesEvidenceAndProvenance() {
        val original = DiagnosticEvidence(
            key = "obd.mode01.pid.0C",
            value = 1726.0,
            unit = "rpm",
            timestampEpochMs = 123456789L,
            availability = EvidenceAvailability.AVAILABLE,
            verification = EvidenceVerification.UNVERIFIED,
            provenance = EvidenceProvenance(
                source = EvidenceSource.OBD_MODE_01,
                sourceId = "elm327",
                ecuId = "7E8",
                vehicleProfile = "synthetic-test",
                rawRepresentation = "41 0C 1A F8"
            ),
            isDerived = false,
            quality = "VALID:FRESH",
            note = null
        )

        val bytes = ByteArrayOutputStream()
        DiagnosticEvidenceJson.write(listOf(original), bytes)
        val json = bytes.toString(Charsets.UTF_8.name())

        assertTrue(json.contains(DiagnosticEvidenceJson.FORMAT))
        val restored = DiagnosticEvidenceJson.read(ByteArrayInputStream(bytes.toByteArray()))

        assertEquals(1, restored.size)
        assertEquals(original.key, restored[0].key)
        assertEquals(original.value, restored[0].value)
        assertEquals(original.unit, restored[0].unit)
        assertEquals(original.timestampEpochMs, restored[0].timestampEpochMs)
        assertEquals(original.availability, restored[0].availability)
        assertEquals(original.verification, restored[0].verification)
        assertEquals(original.provenance, restored[0].provenance)
        assertEquals(original.isDerived, restored[0].isDerived)
        assertEquals(original.quality, restored[0].quality)
        assertEquals(original.note, restored[0].note)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnknownArtifactFormat() {
        DiagnosticEvidenceJson.read("{\"format\":\"unknown\",\"evidence\":[]}")
    }
}
