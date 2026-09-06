package com.autodiag.core

import com.autodiag.core.capability.OutlanderMeasurementVerification
import com.autodiag.core.capability.OutlanderPhevEvidenceSample
import com.autodiag.core.capability.OutlanderPhevEvidenceSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OutlanderPhevEvidenceSessionTest {
    @Test fun jsonRoundTripPreservesEvidenceAndVerification() {
        val session = OutlanderPhevEvidenceSession(
            sessionId = "test-session",
            startedAtEpochMs = 1234L,
        )
        session.append(
            OutlanderPhevEvidenceSample(
                timestampEpochMs = 1235L,
                request = "21 01",
                response = "61 01 ...",
                adapterStatus = "OK",
                parsedByteCount = 80,
                isolationResistanceKOhm = 3200,
                internalResistanceMaxMOhm = 1.2,
                internalResistanceMinMOhm = 0.8,
                verification = OutlanderMeasurementVerification.PARTIALLY_VERIFIED,
            )
        )

        val restored = OutlanderPhevEvidenceSession.fromJson(session.toJson())
        val sample = restored.snapshot().single()

        assertEquals("test-session", restored.sessionId)
        assertEquals(1234L, restored.startedAtEpochMs)
        assertEquals("21 01", sample.request)
        assertEquals("61 01 ...", sample.response)
        assertEquals(80, sample.parsedByteCount)
        assertEquals(3200, sample.isolationResistanceKOhm)
        assertEquals(1.2, sample.internalResistanceMaxMOhm!!, 0.0001)
        assertEquals(0.8, sample.internalResistanceMinMOhm!!, 0.0001)
        assertEquals(OutlanderMeasurementVerification.PARTIALLY_VERIFIED, sample.verification)
    }

    @Test fun missingSamplesProducesEmptySession() {
        val restored = OutlanderPhevEvidenceSession.fromJson(
            "{\"sessionId\":\"empty\",\"startedAtEpochMs\":42}"
        )
        assertTrue(restored.snapshot().isEmpty())
    }

    @Test fun missingOptionalValuesRemainNull() {
        val restored = OutlanderPhevEvidenceSession.fromJson(
            "{\"sessionId\":\"minimal\",\"startedAtEpochMs\":42,\"samples\":[{" +
                "\"timestampEpochMs\":43,\"request\":\"21 01\",\"response\":\"\",\"adapterStatus\":\"NO_DATA\"}]}"
        )
        val sample = restored.snapshot().single()
        assertNull(sample.response)
        assertNull(sample.parsedByteCount)
        assertEquals(OutlanderMeasurementVerification.UNVERIFIED, sample.verification)
    }
}
