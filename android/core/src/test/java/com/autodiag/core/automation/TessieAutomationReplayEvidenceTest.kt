package com.autodiag.core.automation

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Evidence-backed replay only. Signal IDs are semantic candidates derived from
 * Tessie telemetry strings; this test deliberately exercises NOTIFY_ALERT only.
 */
class TessieAutomationReplayEvidenceTest {
    @Test
    fun lowBatteryAndSentryConditionProducesReadOnlyNotificationOnEntry() {
        val rule = AutomationRule(
            id = "tessie.candidate.low_battery_sentry.notify",
            triggerSignalId = "tesla.charge_state.usable_battery_level",
            triggerOperator = ComparisonOperator.LTE,
            triggerThreshold = 20.0,
            conditions = listOf(
                Condition("tesla.vehicle_state.sentry_mode", ComparisonOperator.EQ, 1.0)
            ),
            action = AutomationAction("disableSentryMode", AutomationPolicy.NOTIFY_ALERT),
            enabled = true
        )

        val samples = listOf(
            ReplaySample(100_000L, listOf(
                SemanticSignal("tesla.charge_state.usable_battery_level", 35.0, timestampMs = 100_000L),
                SemanticSignal("tesla.vehicle_state.sentry_mode", 1.0, timestampMs = 100_000L)
            )),
            ReplaySample(110_000L, listOf(
                SemanticSignal("tesla.charge_state.usable_battery_level", 18.0, timestampMs = 110_000L),
                SemanticSignal("tesla.vehicle_state.sentry_mode", 1.0, timestampMs = 110_000L)
            )),
            ReplaySample(120_000L, listOf(
                SemanticSignal("tesla.charge_state.usable_battery_level", 15.0, timestampMs = 120_000L),
                SemanticSignal("tesla.vehicle_state.sentry_mode", 1.0, timestampMs = 120_000L)
            )),
            ReplaySample(130_000L, listOf(
                SemanticSignal("tesla.charge_state.usable_battery_level", 25.0, timestampMs = 130_000L),
                SemanticSignal("tesla.vehicle_state.sentry_mode", 1.0, timestampMs = 130_000L)
            )),
            ReplaySample(140_000L, listOf(
                SemanticSignal("tesla.charge_state.usable_battery_level", 18.0, timestampMs = 140_000L),
                SemanticSignal("tesla.vehicle_state.sentry_mode", 1.0, timestampMs = 140_000L)
            ))
        )

        val events = AutomationReplay.run(rule, samples)
        assertEquals(listOf(false, true, false, false, true), events.map { it.evaluation.triggered })
        assertEquals("TRIGGERED", events[1].evaluation.reason)
        assertEquals("TRIGGERED", events[4].evaluation.reason)
    }

    @Test
    fun writePolicyRemainsBlockedEvenWhenEvidenceConditionMatches() {
        val rule = AutomationRule(
            id = "tessie.candidate.low_battery_sentry.write_blocked",
            triggerSignalId = "tesla.charge_state.usable_battery_level",
            triggerOperator = ComparisonOperator.LTE,
            triggerThreshold = 20.0,
            conditions = listOf(
                Condition("tesla.vehicle_state.sentry_mode", ComparisonOperator.EQ, 1.0)
            ),
            action = AutomationAction("disableSentryMode", AutomationPolicy.WRITE_COMMAND),
            enabled = true
        )

        val result = AutomationReplay.run(
            rule,
            listOf(ReplaySample(100_000L, listOf(
                SemanticSignal("tesla.charge_state.usable_battery_level", 15.0, timestampMs = 100_000L),
                SemanticSignal("tesla.vehicle_state.sentry_mode", 1.0, timestampMs = 100_000L)
            )))
        ).single().evaluation

        assertEquals(false, result.triggered)
        assertEquals("WRITE_COMMAND_REQUIRES_SEPARATE_SAFETY_GATE", result.reason)
    }
}
