package com.autodiag.core.automation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AutomationDataQualityTest {
    @Test
    fun staleSignalIsRejected() {
        val gate = AutomationDataQualityGate(maxAgeMs = 30_000)
        val result = gate.validate(
            setOf("battery.usable_soc"),
            listOf(SemanticSignal("battery.usable_soc", 15.0, timestampMs = 60_000)),
            nowMs = 100_001
        )
        assertFalse(result.accepted)
        assertEquals("STALE_OR_INVALID_SIGNAL", result.reason)
        assertTrue(result.staleSignalIds.contains("battery.usable_soc"))
    }

    @Test
    fun futureDatedSignalIsRejected() {
        val gate = AutomationDataQualityGate(maxAgeMs = 30_000)
        val result = gate.validate(
            setOf("battery.usable_soc"),
            listOf(SemanticSignal("battery.usable_soc", 15.0, timestampMs = 101_000)),
            nowMs = 100_000
        )
        assertFalse(result.accepted)
        assertEquals("STALE_OR_INVALID_SIGNAL", result.reason)
    }

    @Test
    fun missingValueIsRejected() {
        val gate = AutomationDataQualityGate()
        val result = gate.validate(
            setOf("battery.usable_soc"),
            listOf(SemanticSignal("battery.usable_soc", null, timestampMs = 100_000)),
            nowMs = 100_000
        )
        assertFalse(result.accepted)
        assertEquals("REQUIRED_SIGNAL_UNAVAILABLE", result.reason)
    }

    @Test
    fun disabledRuleCanTriggerAfterBeingEnabled() {
        val rule = AutomationRule(
            id = "battery.low",
            triggerSignalId = "battery.usable_soc",
            triggerOperator = ComparisonOperator.LTE,
            triggerThreshold = 20.0,
            action = AutomationAction("record", AutomationPolicy.READ_LOG_ANALYZE),
            enabled = false
        )
        val detector = AutomationEdgeDetector()
        val low = listOf(SemanticSignal("battery.usable_soc", 15.0))
        assertFalse(detector.evaluate(rule, low).triggered)
        val enabled = rule.copy(enabled = true)
        assertTrue(detector.evaluate(enabled, low).triggered)
    }
}
