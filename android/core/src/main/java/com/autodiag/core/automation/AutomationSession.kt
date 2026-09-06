package com.autodiag.core.automation

/** Runtime session for deterministic, read-only automation processing. */
class AutomationSession(
    private val rules: List<AutomationRule>,
    cooldownMs: Long = 60_000L,
    maxSignalAgeMs: Long = 30_000L
) {
    private val limiter = NotificationRateLimiter(cooldownMs)
    private val dataQualityGate = AutomationDataQualityGate(maxSignalAgeMs)

    fun process(sample: ReplaySample): List<AutomationNotification> {
        val notifications = mutableListOf<AutomationNotification>()
        val normalizedSignals = sample.signals.map { it.copy(timestampMs = sample.timestampMs) }
        for (rule in rules) {
            val required = buildSet {
                add(rule.triggerSignalId)
                rule.conditions.forEach { add(it.signalId) }
            }
            val quality = dataQualityGate.validate(required, normalizedSignals, sample.timestampMs)
            if (!quality.accepted) continue

            val evaluation = AutomationRuleEvaluator.evaluate(rule, normalizedSignals)
            if (rule.action.policy != AutomationPolicy.NOTIFY_ALERT) continue
            if (!evaluation.triggered) continue
            if (!limiter.allow(rule.id, sample.timestampMs)) continue
            AutomationNotificationAdapter.create(evaluation, sample.timestampMs)?.let(notifications::add)
        }
        return notifications
    }

    /** Ends the logical session and clears notification cooldown state. */
    fun reset() {
        rules.forEach { rule -> limiter.reset(rule.id) }
    }
}
