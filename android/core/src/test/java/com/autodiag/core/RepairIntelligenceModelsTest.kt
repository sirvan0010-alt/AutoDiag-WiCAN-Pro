package com.autodiag.core

import com.autodiag.core.diagnostics.*
import org.junit.Assert.*
import org.junit.Test

class RepairIntelligenceModelsTest {
    @Test
    fun serviceCatalogContainsAllPlannedFunctions() {
        assertEquals(41, ServiceFunctionCatalog.all.size)
        assertNotNull(ServiceFunctionCatalog.find("DPF_REGENERATION"))
        assertNotNull(ServiceFunctionCatalog.find("IMMO_PROG"))
        assertNotNull(ServiceFunctionCatalog.find("HIGH_VOLTAGE_BATTERY_TEST"))
    }

    @Test
    fun incompleteEstimateIsNotInvented() {
        val parts = listOf(RepairPart(component = "example", price = null))
        val labor = listOf(LaborEstimate("example", 1.0, LaborTimeType.OEM_FLAT_RATE))
        assertNull(RepairEstimateEngine.calculate(parts, labor))
    }

    @Test
    fun userConfiguredLaborProducesTransparentTotal() {
        val parts = listOf(RepairPart(component = "sensor", quantity = 2, price = 50.0))
        val labor = listOf(LaborEstimate("replace sensor", 1.5, LaborTimeType.USER_CONFIGURED))
        val estimate = RepairEstimateEngine.calculate(parts, labor, fallbackHourlyRate = 80.0)
        assertNotNull(estimate)
        assertEquals(220.0, estimate!!.totalMin, 0.001)
        assertEquals(220.0, estimate.totalMax, 0.001)
    }

    @Test
    fun dtcModelKeepsPartVerdictNonDeterministicWithoutVerifiedPartEvidence() {
        val intelligence = RepairIntelligence(
            dtcCode = "P0000",
            candidateParts = listOf(
                RepairPart(
                    component = "candidate",
                    confidence = VerificationState.PARTIALLY_VERIFIED
                )
            ),
            sources = listOf(
                RepairSource(
                    id = "community-example",
                    provider = "example",
                    label = "Example",
                    sourceType = "community",
                    access = SourceAccess.COMMUNITY,
                    verification = VerificationState.VERIFIED
                )
            )
        )
        assertFalse(intelligence.hasDeterministicPartVerdict())
    }
}
