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
            ReplaySample(
                100_000L,
                listOf(
                    SemanticSignal(
                        id = "battery.usable_soc",
                        value = 15.0,
                        timestampMs = 100_000L
                    )
                )
            )
        )

        assertEquals(1, events.size)
        assertEquals("battery.low.notify", events.single().ruleId)
        assertEquals(15.0, events.single().observedValues["battery.usable_soc"])
        assertEquals("lowBattery", events.single().actionId)
    }

    @Test
    fun sessionAuditsStaleDataQualityRejection() {
        val rule = AutomationRule(
            id = "battery.low.stale",
            triggerSignalId = "battery.usable_soc",
            triggerOperator = ComparisonOperator.LTE,
            triggerThreshold = 20.0,
            action = AutomationAction("lowBattery", AutomationPolicy.NOTIFY_ALERT),
            enabled = true
        )
        val events = mutableListOf<AutomationAuditEvent>()
        val session = AutomationSession(
            listOf(rule),
            maxSignalAgeMs = 30_000L,
            auditSink = events::add
        )

        session.process(
            ReplaySample(
                100_000L,
                listOf(
                    SemanticSignal(
                        id = "battery.usable_soc",
                        value = 15.0,
                        timestampMs = 60_000L
                    )
                )
            )
        )

        assertEquals(1, events.size)
        assertEquals("DATA_QUALITY_REJECTED", events.single().outcome)
        assertEquals("STALE_OR_INVALID_SIGNAL;stale=battery.usable_soc", events.single().error)
        assertEquals("lowBattery", events.single().actionId)
    }
}
