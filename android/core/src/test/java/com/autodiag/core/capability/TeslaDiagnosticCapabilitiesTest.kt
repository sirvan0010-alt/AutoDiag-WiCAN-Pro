package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TeslaDiagnosticCapabilitiesTest {
    @Test
    fun catalogContainsAllRequestedTeslaDomains() {
        val domains = TeslaDiagnosticCapabilities.all.map { it.domain }.toSet()
        assertEquals(TeslaDiagnosticDomain.entries.toSet(), domains)
        assertEquals(11, TeslaDiagnosticCapabilities.all.size)
    }

    @Test
    fun everyDeferredFunctionRequiresVerifiedTarget() {
        assertTrue(TeslaDiagnosticCapabilities.all.all { it.requiresVerifiedTarget })
        assertTrue(TeslaDiagnosticCapabilities.all.all { it.requiresVehicleSpecificDefinition })
    }

    @Test
    fun batteryManagementExposesReadAndServiceScope() {
        val bms = TeslaDiagnosticCapabilities.forDomain(TeslaDiagnosticDomain.BATTERY_MANAGEMENT).single()
        assertTrue(DiagnosticOperationSafety.READ_ONLY in bms.operations)
        assertTrue(DiagnosticOperationSafety.SERVICE_PROCEDURE in bms.operations)
    }
}
