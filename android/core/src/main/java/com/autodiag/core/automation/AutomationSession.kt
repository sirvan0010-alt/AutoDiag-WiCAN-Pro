package com.autodiag.core.automation

/** Runtime session for deterministic, read-only automation processing. */
class AutomationSession(
    private val rules: List<AutomationRule>,
    cooldownMs: Long = 60_000L,
    maxSignalAgeMs: Long = 30_000L,
    private val auditSink: (AutomationAuditEvent) -> Unit = {}
) {
    init {
        require(cooldownMs >= 0L) { "cooldownMs must be non-negative" }
        require(maxSignalAgeMs >= 0L) { "maxSignalAgeMs must be non-negative" }
        rules.forEach(AutomationRuleValidator::requireValid)
        require(rules.map { it.id }.distinct().size == rules.size) {
            "Automation rule IDs must be unique"
        }
    }

    private val detectors = rules.associate { it.id to AutomationEdgeDetector() }
    private val limiter = NotificationRateLimiter(cooldownMs)
    private val dataQualityGate = AutomationDataQualityGate(maxSignalAgeMs)

    fun process(sample: ReplaySample): List<AutomationNotification> {
        val notifications = mutableListOf<AutomationNotification>()
        // Preserve source timestamps so the quality gate can reject stale/future data.
        val signals = sample.signals
        for (rule in rules) {
            val required = buildSet {
                add(rule.triggerSignalId)
                rule.conditions.forEach { add(it.signalId) }
            }
            val quality = dataQualityGate.validate(required, signals, sample.timestampMs)
            if (!quality.accepted) continue

            val detector = detectors[rule.id] ?: continue
            val evaluation = detector.evaluate(rule, signals)
            auditSink(AutomationAudit.from(rule, evaluation, sample.timestampMs))

            if (rule.action.policy != AutomationPolicy.NOTIFY_ALERT) continue
            if (!evaluation.triggered) continue
            if (!limiter.allow(rule.id, sample.timestampMs)) continue
            AutomationNotificationAdapter.create(evaluation, sample.timestampMs)?.let(notifications::add)
        }
        return notifications
    }

    /** Ends the logical session and clears edge/cooldown state. */
    fun reset() {
        rules.forEach { rule ->
            detectors[rule.id]?.reset(rule.id)
            limiter.reset(rule.id)
        }
    }
}
