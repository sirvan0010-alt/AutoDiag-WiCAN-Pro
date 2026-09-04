package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OutlanderPhevEvidenceSessionTest {
    @Test
    fun roundTripsRawExchangeAndDecodedValues() {
        val session = OutlanderPhevEvidenceSession("test-session", 1000L)
        session.append(
            OutlanderPhevEvidenceSample(
                timestampEpochMs = 1100L,
                request = "21 01",
                response = "7E8 00 01 F4",
                adapterStatus = "POSITIVE",
                parsedByteCount = 4,
                isolationResistanceKOhm = 500,
                internalResistanceMaxMOhm = 1.2,
                internalResistanceMinMOhm = 0.8,
            )
        )

        val restored = OutlanderPhevEvidenceSession.fromJson(session.toJson())
        val sample = restored.snapshot().single()

        assertEquals("test-session", restored.sessionId)
        assertEquals("21 01", sample.request)
        assertEquals("7E8 00 01 F4", sample.response)
        assertEquals(500, sample.isolationResistanceKOhm)
        assertEquals(1.2, sample.internalResistanceMaxMOhm)
        assertEquals(0.8, sample.internalResistanceMinMOhm)
    }

    @Test
    fun exportDeclaresReadOnlyAndNoGuessingPolicy() {
        val json = OutlanderPhevEvidenceSession("s", 1L).toJson()
        assertTrue(json.contains("read_only"))
        assertTrue(json.contains("no_guessed_can_or_ecu_mapping"))
    }
}
