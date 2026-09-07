package com.autodiag.core.live

import com.autodiag.core.automation.AutomationAction
import com.autodiag.core.automation.AutomationPolicy
import com.autodiag.core.automation.AutomationRule
import com.autodiag.core.automation.AutomationSession
import com.autodiag.core.automation.ComparisonOperator
import com.autodiag.core.obd.LiveDataFreshness
import com.autodiag.core.obd.LiveDataQuality
import com.autodiag.core.obd.ObdLiveDataEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

class ObdAutomationPipelineTest {
    @Test
    fun liveEngineSensorStreamFeedsReadOnlyAutomation() = runBlocking {
        val rule = AutomationRule(
            id = "low-rpm-alert",
            triggerSignalId = "engine.rpm",
            triggerOperator = ComparisonOperator.LT,
            triggerThreshold = 1000.0,
            action = AutomationAction("notify.low-rpm", AutomationPolicy.NOTIFY_ALERT),
            enabled = true
        )
        val pipeline = ObdAutomationPipeline(
            AutomationSession(listOf(rule), cooldownMs = 0L, maxSignalAgeMs = 30_000L)
        )

        val events = pipeline.processStream(
            flowOf(
                sensor(1500.0, 100_000L),
                sensor(800.0, 100_500L),
                sensor(700.0, 101_000L)
            )
        ).toList()

        assertEquals(1, events.size)
        assertEquals("low-rpm-alert", events.single().ruleId)
    }

    @Test
    fun nonLiveSensorIsNotPromoted() = runBlocking {
        val rule = AutomationRule(
            id = "low-rpm-alert",
            triggerSignalId = "engine.rpm",
            triggerOperator = ComparisonOperator.LT,
            triggerThreshold = 1000.0,
            action = AutomationAction("notify.low-rpm", AutomationPolicy.NOTIFY_ALERT),
            enabled = true
        )
        val pipeline = ObdAutomationPipeline(AutomationSession(listOf(rule), cooldownMs = 0L))
        val events = pipeline.processStream(
            flowOf(sensor(800.0, 100_000L, ObdLiveDataEngine.State.UNAVAILABLE))
        ).toList()

        assertTrue(events.isEmpty())
    }

    private fun sensor(
        value: Double,
        timestampMs: Long,
        state: ObdLiveDataEngine.State = ObdLiveDataEngine.State.LIVE
    ) = ObdLiveDataEngine.SensorSample(
        pid = 0x0C,
        labelCs = "Otáčky motoru",
        value = value,
        unit = "rpm",
        rawHex = "05DC",
        timestampEpochMs = timestampMs,
        state = state
    )
}
