package com.autodiag.core.capability

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DiagnosticDataProviderTest {
    @Test
    fun empty_provider_is_safe_and_does_not_invent_data() = runBlocking {
        val provider: DiagnosticDataProvider = EmptyDiagnosticDataProvider

        assertNull(provider.findVehicle("TMBTEST12345678901"))
        assertNull(provider.findEcu(EcuDataIdentity(ecuId = "unknown")))
        assertEquals(emptyList(), provider.findSignals(EcuDataIdentity()))
        assertNull(provider.findDtc("P0000"))
    }

    @Test
    fun definitions_preserve_verification_and_provenance() {
        val signal = SignalDataDefinition(
            id = "rpm",
            label = "Engine speed",
            unit = "rpm",
            request = "010C",
            scale = 0.25,
            verification = VerificationState.PARTIALLY_VERIFIED,
            provenance = "diagnostic-data/mode01"
        )

        assertEquals(VerificationState.PARTIALLY_VERIFIED, signal.verification)
        assertEquals("diagnostic-data/mode01", signal.provenance)
    }
}
