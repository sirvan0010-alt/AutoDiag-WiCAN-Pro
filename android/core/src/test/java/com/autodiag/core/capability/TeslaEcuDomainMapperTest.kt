package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TeslaEcuDomainMapperTest {
    @Test
    fun mapsOnlyVerifiedExplicitSemanticLabel() {
        val ecu = DiscoveredEcu(ecuId = "ecu-bms", name = "Battery", verified = true)

        val result = TeslaEcuDomainMapper.map(ecu, "battery")

        assertEquals(TeslaDiagnosticDomain.BATTERY_MANAGEMENT, result?.domain)
        assertEquals(VerificationState.VERIFIED, result?.confidence)
    }

    @Test
    fun unknownLabelDoesNotGuessDomain() {
        val ecu = DiscoveredEcu(ecuId = "ecu-1", name = "Battery", verified = true)

        assertNull(TeslaEcuDomainMapper.map(ecu, "battery_control_unit"))
    }

    @Test
    fun unverifiedEcuCannotBecomeTeslaDomain() {
        val ecu = DiscoveredEcu(ecuId = "ecu-1", name = "BMS", verified = false)

        assertNull(TeslaEcuDomainMapper.map(ecu, "bms"))
    }
}
