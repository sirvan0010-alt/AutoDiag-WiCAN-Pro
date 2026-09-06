package com.autodiag.core.automation

/** Notification intent produced by automation evaluation; it never executes vehicle commands. */
data class AutomationNotification(
    val ruleId: String,
    val title: String,
    val message: String,
    val timestampMs: Long,
    val severity: Severity = Severity.WARNING
) {
    enum class Severity { INFO, WARNING, CRITICAL }
}

/** Per-rule cooldown state for NOTIFY_ALERT. */
class NotificationRateLimiter(private val cooldownMs: Long) {
    private val lastSent = mutableMapOf<String, Long>()

    fun allow(ruleId: String, nowMs: Long): Boolean {
        val previous = lastSent[ruleId]
        if (previous != null && nowMs - previous < cooldownMs) return false
        lastSent[ruleId] = nowMs
        return true
    }

    fun reset(ruleId: String) {
        lastSent.remove(ruleId)
    }
}

object AutomationNotificationAdapter {
    fun create(
        evaluation: AutomationEvaluation,
        nowMs: Long,
        title: String = "Automation alert"
    ): AutomationNotification? {
        if (!evaluation.triggered) return null
        return AutomationNotification(
            ruleId = evaluation.ruleId,
            title = title,
            message = "Rule ${evaluation.ruleId} triggered (${evaluation.reason})",
            timestampMs = nowMs
        )
    }
}
