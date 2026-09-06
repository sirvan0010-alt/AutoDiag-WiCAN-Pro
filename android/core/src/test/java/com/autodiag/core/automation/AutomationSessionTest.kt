package com.autodiag.core.automation

import kotlin.test.Test
import kotlin.test.assertEquals

class AutomationSessionTest {
    @Test
    fun notifyRuleIsEdgeAndCooldownLimited() {
        val rule = AutomationRule(
            id = "battery.low.notify",
            triggerSignalId = "battery.usable_soc",
            triggerOperator = ComparisonOperator.LTE,
            triggerThreshold = 20.0,
            action = AutomationAction("lowBattery", AutomationPolicy.NOTIFY_ALERT),
            enabled = true
        )
        val session = AutomationSession(listOf(rule), cooldownMs = 60_000)
        val low = { t: Long -> ReplaySample(t, listOf(SemanticSignal("battery.usable_soc", 15.0))) }
        val high = { t: Long -> ReplaySample(t, listOf(SemanticSignal("battery.usable_soc", 30.0))) }
        assertEquals(1, session.process(low(100_000)).size)
        assertEquals(0, session.process(low(110_000)).size)
        assertEquals(0, session.process(high(120_000)).size)
        assertEquals(1, session.process(low(130_000)).size)
    }

    @Test
    fun resetClearsEdgeAndCooldownState() {
        val rule = AutomationRule(
            id = "battery.low.notify",
            triggerSignalId = "battery.usable_soc",
            triggerOperator = ComparisonOperator.LTE,
            triggerThreshold = 20.0,
            action = AutomationAction("lowBattery", AutomationPolicy.NOTIFY_ALERT),
            enabled = true
        )
        val session = AutomationSession(listOf(rule), cooldownMs = 60_000)
        val sample = { t: Long -> ReplaySample(t, listOf(SemanticSignal("battery.usable_soc", 15.0))) }
        assertEquals(1, session.process(sample(100_000)).size)
        session.reset()
        assertEquals(1, session.process(sample(110_000)).size)
    }
}
