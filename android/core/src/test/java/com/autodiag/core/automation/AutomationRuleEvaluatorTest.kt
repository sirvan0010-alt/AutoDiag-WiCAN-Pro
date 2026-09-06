package com.autodiag.core.automation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AutomationRuleEvaluatorTest {
    private val rule = AutomationRule(
        id = "tesla.low-battery-sentry-off",
        triggerSignalId = "battery.usable_soc",
        triggerOperator = ComparisonOperator.LTE,
        triggerThreshold = 20.0,
        conditions = listOf(
            Condition("vehicle.parked", ComparisonOperator.EQ, 1.0),
            Condition("vehicle.sentry_mode", ComparisonOperator.EQ, 1.0)
        ),
        action = AutomationAction("disableSentryMode", AutomationPolicy.WRITE_COMMAND),
        enabled = true
    )

    @Test
    fun lowBatteryRuleIsBlockedByWriteBoundary() {
        val result = AutomationRuleEvaluator.evaluate(
            rule,
            listOf(
                SemanticSignal("battery.usable_soc", 15.0),
                SemanticSignal("vehicle.parked", 1.0),
                SemanticSignal("vehicle.sentry_mode", 1.0)
            )
        )
        assertFalse(result.triggered)
        assertTrue(result.conditionsSatisfied)
        assertTrue(result.reason == "WRITE_COMMAND_REQUIRES_SEPARATE_SAFETY_GATE")
    }

    @Test
    fun missingSignalDoesNotBecomeFailureOrTrigger() {
        val result = AutomationRuleEvaluator.evaluate(
            rule,
            listOf(SemanticSignal("battery.usable_soc", null))
        )
        assertFalse(result.triggered)
        assertFalse(result.conditionsSatisfied)
    }

    @Test
    fun readOnlyRuleCanTrigger() {
        val readOnly = rule.copy(
            action = AutomationAction("recordLowBattery", AutomationPolicy.READ_LOG_ANALYZE)
        )
        val result = AutomationRuleEvaluator.evaluate(
            readOnly,
            listOf(
                SemanticSignal("battery.usable_soc", 15.0),
                SemanticSignal("vehicle.parked", 1.0),
                SemanticSignal("vehicle.sentry_mode", 1.0)
            )
        )
        assertTrue(result.triggered)
    }
}
