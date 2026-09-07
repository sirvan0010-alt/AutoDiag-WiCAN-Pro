package com.autodiag.core.automation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AutomationRuleValidatorTest {
    private fun validRule() = AutomationRule(
        id = "battery.low.notify",
        triggerSignalId = "battery.usable_soc",
        triggerOperator = ComparisonOperator.LTE,
        triggerThreshold = 20.0,
        conditions = listOf(Condition("vehicle_state.sentry_mode", ComparisonOperator.EQ, 1.0)),
        action = AutomationAction("lowBattery", AutomationPolicy.NOTIFY_ALERT),
        enabled = true
    )

    @Test
    fun validRuleIsAccepted() {
        assertTrue(AutomationRuleValidator.validate(validRule()).valid)
    }

    @Test
    fun blankIdIsRejected() {
        val result = AutomationRuleValidator.validate(validRule().copy(id = " "))
        assertFalse(result.valid)
    }

    @Test
    fun invalidVersionIsRejected() {
        val result = AutomationRuleValidator.validate(validRule().copy(version = 0))
        assertFalse(result.valid)
    }

    @Test
    fun blankSignalIdsAreRejected() {
        val result = AutomationRuleValidator.validate(
            validRule().copy(
                triggerSignalId = "",
                conditions = listOf(Condition("", ComparisonOperator.EQ, 1.0))
            )
        )
        assertFalse(result.valid)
    }

    @Test
    fun nonFiniteThresholdsAreRejected() {
        val trigger = AutomationRuleValidator.validate(validRule().copy(triggerThreshold = Double.NaN))
        val condition = AutomationRuleValidator.validate(
            validRule().copy(conditions = listOf(Condition("x", ComparisonOperator.GT, Double.POSITIVE_INFINITY)))
        )
        assertFalse(trigger.valid)
        assertFalse(condition.valid)
    }

    @Test
    fun blankActionIdIsRejected() {
        val result = AutomationRuleValidator.validate(
            validRule().copy(action = AutomationAction("", AutomationPolicy.NOTIFY_ALERT))
        )
        assertFalse(result.valid)
    }
}
