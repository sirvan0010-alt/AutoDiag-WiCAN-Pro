package com.autodiag.core.diagnostic

import org.junit.Assert.assertEquals
import org.junit.Test

class DiagnosticEvidenceStoreTest {
    @Test
    fun appendPreservesEvidenceHistory() {
        val store = DiagnosticEvidenceStore()
        val first = DiagnosticEvidence(
            key = "obd.mode01.pid.0C",
            value = 1726.0,
            unit = "rpm",
            timestampEpochMs = 1000L,
            availability = EvidenceAvailability.AVAILABLE,
            verification = EvidenceVerification.UNVERIFIED,
            provenance = EvidenceProvenance(EvidenceSource.OBD_MODE_01, rawRepresentation = "1A F8")
        )
        val second = first.copy(value = 1800.0, timestampEpochMs = 1500L)

        store.append(first)
        store.append(second)

        assertEquals(listOf(first, second), store.snapshot())
    }

    @Test
    fun snapshotIsIndependentFromInternalList() {
        val store = DiagnosticEvidenceStore()
        val evidence = DiagnosticEvidence(
            key = "obd.mode01.pid.0D",
            value = 50.0,
            unit = "km/h",
            timestampEpochMs = 2000L,
            availability = EvidenceAvailability.AVAILABLE,
            verification = EvidenceVerification.UNVERIFIED,
            provenance = EvidenceProvenance(EvidenceSource.OBD_MODE_01)
        )

        val snapshotBefore = store.snapshot()
        store.append(evidence)

        assertEquals(emptyList<DiagnosticEvidence<*>>(), snapshotBefore)
        assertEquals(1, store.snapshot().size)
    }
}
