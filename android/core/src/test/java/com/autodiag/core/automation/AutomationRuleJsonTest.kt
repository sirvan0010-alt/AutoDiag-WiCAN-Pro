package com.autodiag.core.automation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AutomationRuleJsonTest {
    @Test
    fun encodeDecodeRoundTripPreservesRule() {
        val rule = AutomationRule(
            id = "battery.low.notify",
            version = 2,
            triggerSignalId = "battery.usable_soc",
            triggerOperator = ComparisonOperator.LTE,
            triggerThreshold = 20.0,
            conditions = listOf(
                Condition("vehicle_state.sentry_mode", ComparisonOperator.EQ, 1.0)
            ),
            action = AutomationAction("lowBattery", AutomationPolicy.NOTIFY_ALERT),
            enabled = true
        )

        assertEquals(rule, AutomationRuleJson.decode(AutomationRuleJson.encode(rule)))
    }

    @Test
    fun decodeRejectsNonFiniteThreshold() {
        val json = """
            {
              "id":"invalid",
              "version":1,
              "triggerSignalId":"battery.usable_soc",
              "triggerOperator":"LTE",
              "triggerThreshold":1e309,
              "enabled":true,
              "conditions":[],
              "action":{"id":"notify","policy":"NOTIFY_ALERT"}
            }
        """.trimIndent()

        assertFailsWith<IllegalArgumentException> { AutomationRuleJson.decode(json) }
    }
}
