package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OutlanderPhevEvidenceGateTest {
    private val signal = SignalDataDefinition(
        id = "battery.soc",
        label = "SOC",
        unit = "%",
        request = "22ABCD",
        verification = VerificationState.UNVERIFIED,
        provenance = "PHEV Watchdog APK analysis"
    )

    private val exchange = OutlanderPhevEvidenceGate.ObservedExchange(
        request = "22 AB CD",
        response = "62 AB CD 01 02",
        source = "vehicle-capture-test",
        timestampEpochMs = 1L
    )

    @Test
    fun matchingRequestWithResponseBecomesPartialEvidence() {
        val result = OutlanderPhevEvidenceGate.evaluate(signal, exchange)

        assertEquals(OutlanderPhevEvidenceGate.EvidenceStatus.PARTIAL, result.status)
        assertNotNull(result.signal)
        assertEquals(VerificationState.PARTIALLY_VERIFIED, result.signal!!.verification)
    }

    @Test
    fun mismatchingRequestIsRejected() {
        val result = OutlanderPhevEvidenceGate.evaluate(
            signal,
            exchange.copy(request = "22ABCE")
        )

        assertEquals(OutlanderPhevEvidenceGate.EvidenceStatus.REJECTED, result.status)
        assertNull(result.signal)
    }

    @Test
    fun missingRequestCannotBePromoted() {
        val result = OutlanderPhevEvidenceGate.evaluate(
            signal.copy(request = null),
            exchange
        )

        assertEquals(OutlanderPhevEvidenceGate.EvidenceStatus.REJECTED, result.status)
        assertNull(result.signal)
    }

    @Test
    fun emptyResponseCannotBeUsedAsEvidence() {
        val result = OutlanderPhevEvidenceGate.evaluate(
            signal,
            exchange.copy(response = "")
        )

        assertEquals(OutlanderPhevEvidenceGate.EvidenceStatus.REJECTED, result.status)
        assertNull(result.signal)
    }
}
