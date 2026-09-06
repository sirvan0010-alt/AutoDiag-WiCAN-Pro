package com.autodiag.core.automation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AutomationReplayTest {
    @Test
    fun triggerOccursOnlyOnEntryIntoThreshold() {
        val rule = AutomationRule(
            id = "battery.low",
            triggerSignalId = "battery.usable_soc",
            triggerOperator = ComparisonOperator.LTE,
            triggerThreshold = 20.0,
            action = AutomationAction("record", AutomationPolicy.READ_LOG_ANALYZE),
            enabled = true
        )
        val detector = AutomationEdgeDetector()
        val low = listOf(SemanticSignal("battery.usable_soc", 15.0))
        val high = listOf(SemanticSignal("battery.usable_soc", 30.0))

        assertTrue(detector.evaluate(rule, low).triggered)
        assertEquals("ALREADY_SATISFIED", detector.evaluate(rule, low).reason)
        detector.evaluate(rule, high)
        assertTrue(detector.evaluate(rule, low).triggered)
    }

    @Test
    fun replayIsChronologicalAndDoesNotDispatch() {
        val rule = AutomationRule(
            id = "battery.low",
            triggerSignalId = "battery.usable_soc",
            triggerOperator = ComparisonOperator.LTE,
            triggerThreshold = 20.0,
            action = AutomationAction("record", AutomationPolicy.READ_LOG_ANALYZE),
            enabled = true
        )
        val result = AutomationReplay.run(rule, listOf(
            ReplaySample(2000, listOf(SemanticSignal("battery.usable_soc", 10.0))),
            ReplaySample(1000, listOf(SemanticSignal("battery.usable_soc", 30.0)))
        ))
        assertEquals(listOf(1000L, 2000L), result.map { it.timestampMs })
        assertTrue(result[1].evaluation.triggered)
    }
}
