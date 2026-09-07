package com.autodiag.core.live

import com.autodiag.core.automation.AutomationAction
import com.autodiag.core.automation.AutomationPolicy
import com.autodiag.core.automation.AutomationRule
import com.autodiag.core.automation.AutomationSession
import com.autodiag.core.automation.ComparisonOperator
import com.autodiag.core.obd.LiveDataFreshness
import com.autodiag.core.obd.LiveDataQuality
import com.autodiag.core.obd.LiveDataSample
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObdAutomationBridgeTest {
    @Test
    fun standardObdSampleReachesReadOnlyAutomation() {
        val rule = AutomationRule(
            id = "low-rpm-alert",
            triggerSignalId = "engine.rpm",
            triggerOperator = ComparisonOperator.LT,
            triggerThreshold = 1000.0,
            action = AutomationAction("notify.low-rpm", AutomationPolicy.NOTIFY_ALERT),
            enabled = true
        )
        val session = AutomationSession(listOf(rule), cooldownMs = 0L, maxSignalAgeMs = 30_000L)

        val first = ObdAutomationBridge.process(rpmSample(1500.0, 100_000L), session)
        val second = ObdAutomationBridge.process(rpmSample(800.0, 100_500L), session)
        val third = ObdAutomationBridge.process(rpmSample(700.0, 101_000L), session)

        assertTrue(first.isEmpty())
        assertEquals(1, second.size)
        assertEquals("low-rpm-alert", second.single().ruleId)
        assertTrue(third.isEmpty())
    }

    @Test
    fun invalidObdSampleCannotReachAutomation() {
        val rule = AutomationRule(
            id = "low-rpm-alert",
            triggerSignalId = "engine.rpm",
            triggerOperator = ComparisonOperator.LT,
            triggerThreshold = 1000.0,
            action = AutomationAction("notify.low-rpm", AutomationPolicy.NOTIFY_ALERT),
            enabled = true
        )
        val session = AutomationSession(listOf(rule), cooldownMs = 0L)

        val notifications = ObdAutomationBridge.process(
            rpmSample(800.0, 100_000L, LiveDataQuality.INVALID),
            session
        )

        assertTrue(notifications.isEmpty())
    }

    private fun rpmSample(
        value: Double,
        timestampMs: Long,
        quality: LiveDataQuality = LiveDataQuality.GOOD
    ) = LiveDataSample(
        pid = 0x0C,
        labelCs = "Otáčky motoru",
        value = value,
        unit = "rpm",
        rawHex = "0000",
        timestampEpochMs = timestampMs,
        quality = quality,
        freshness = if (quality == LiveDataQuality.GOOD) LiveDataFreshness.FRESH else LiveDataFreshness.STALE
    )
}
