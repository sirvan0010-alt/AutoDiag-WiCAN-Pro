package com.autodiag.core.automation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AutomationNotificationTest {
    @Test
    fun cooldownSuppressesRepeatedNotification() {
        val limiter = NotificationRateLimiter(60_000)
        assertTrue(limiter.allow("battery.low", 100_000))
        assertFalse(limiter.allow("battery.low", 120_000))
        assertTrue(limiter.allow("battery.low", 160_000))
    }

    @Test
    fun notificationIsCreatedOnlyForTriggeredEvaluation() {
        val evaluation = AutomationEvaluation(
            ruleId = "battery.low",
            triggered = true,
            conditionsSatisfied = true,
            observedValues = mapOf("battery.usable_soc" to 15.0),
            reason = "TRIGGERED"
        )
        assertNotNull(AutomationNotificationAdapter.create(evaluation, 1000))
    }
}
