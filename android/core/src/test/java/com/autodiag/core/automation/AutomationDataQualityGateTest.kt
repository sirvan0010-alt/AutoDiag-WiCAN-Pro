package com.autodiag.core.automation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AutomationDataQualityGateTest {
    private val gate = AutomationDataQualityGate(maxAgeMs = 30_000L)

    @Test
    fun acceptsFreshValue() {
        val result = gate.validate(
            setOf("battery.usable_soc"),
            listOf(SemanticSignal("battery.usable_soc", 15.0, timestampMs = 100_000L)),
            nowMs = 120_000L
        )
        assertTrue(result.accepted)
        assertEquals("DATA_QUALITY_OK", result.reason)
    }

    @Test
    fun rejectsStaleValue() {
        val result = gate.validate(
            setOf("battery.usable_soc"),
            listOf(SemanticSignal("battery.usable_soc", 15.0, timestampMs = 10_000L)),
            nowMs = 50_001L
        )
        assertFalse(result.accepted)
        assertEquals("STALE_OR_INVALID_SIGNAL", result.reason)
        assertEquals(listOf("battery.usable_soc"), result.staleSignalIds)
    }

    @Test
    fun rejectsMissingValue() {
        val result = gate.validate(
            setOf("battery.usable_soc"),
            emptyList(),
            nowMs = 100_000L
        )
        assertFalse(result.accepted)
        assertEquals("REQUIRED_SIGNAL_UNAVAILABLE", result.reason)
        assertEquals(listOf("battery.usable_soc"), result.missingSignalIds)
    }

    @Test
    fun rejectsFutureTimestamp() {
        val result = gate.validate(
            setOf("battery.usable_soc"),
            listOf(SemanticSignal("battery.usable_soc", 15.0, timestampMs = 101_000L)),
            nowMs = 100_000L
        )
        assertFalse(result.accepted)
        assertEquals("STALE_OR_INVALID_SIGNAL", result.reason)
    }
}
