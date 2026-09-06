package com.autodiag.core.automation

import kotlin.test.Test
import kotlin.test.assertEquals

class AutomationAuditTest {
    @Test
    fun sessionEmitsAuditEventForEvaluatedRule() {
        val rule = AutomationRule(
            id = "battery.low.notify",
            triggerSignalId = "battery.usable_soc",
            triggerOperator = ComparisonOperator.LTE,
            triggerThreshold = 20.0,
            action = AutomationAction("lowBattery", AutomationPolicy.NOTIFY_ALERT),
            enabled = true
        )
        val events = mutableListOf<AutomationAuditEvent>()
        val session = AutomationSession(listOf(rule), auditSink = events::add)

        session.process(
            ReplaySample(100_000, listOf(SemanticSignal("battery.usable_soc", 15.0, 100_000)))
        )

        assertEquals(1, events.size)
        assertEquals("battery.low.notify", events.single().ruleId)
        assertEquals(15.0, events.single().observedValues["battery.usable_soc"])
        assertEquals("lowBattery", events.single().actionId)
    }
}
