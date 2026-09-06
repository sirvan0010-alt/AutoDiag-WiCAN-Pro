package com.autodiag.core.automation

/** Runtime session for deterministic, read-only automation processing. */
class AutomationSession(
    private val rules: List<AutomationRule>,
    cooldownMs: Long = 60_000L
) {
    private val detectors = rules.associate { it.id to AutomationEdgeDetector() }
    private val limiter = NotificationRateLimiter(cooldownMs)

    fun process(sample: ReplaySample): List<AutomationNotification> {
        val notifications = mutableListOf<AutomationNotification>()
        for (rule in rules) {
            val detector = detectors[rule.id] ?: continue
            val evaluation = detector.evaluate(rule, sample.signals)
            if (rule.action.policy != AutomationPolicy.NOTIFY_ALERT) continue
            if (!evaluation.triggered) continue
            if (!limiter.allow(rule.id, sample.timestampMs)) continue
            AutomationNotificationAdapter.create(evaluation, sample.timestampMs)?.let(notifications::add)
        }
        return notifications
    }
}
