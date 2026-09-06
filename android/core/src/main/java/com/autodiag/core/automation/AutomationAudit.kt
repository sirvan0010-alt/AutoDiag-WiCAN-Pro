package com.autodiag.core.automation

/** Immutable audit record; it contains observations, not secrets or credentials. */
data class AutomationAuditEvent(
    val ruleId: String,
    val ruleVersion: Int,
    val timestampMs: Long,
    val observedValues: Map<String, Double?>,
    val conditionResults: Map<String, Boolean>,
    val actionId: String,
    val policy: AutomationPolicy,
    val outcome: String,
    val error: String? = null
)

object AutomationAudit {
    fun from(rule: AutomationRule, evaluation: AutomationEvaluation, timestampMs: Long): AutomationAuditEvent {
        val results = rule.conditions.associate { condition ->
            condition.signalId to evaluation.observedValues[condition.signalId].let {
                when (it) {
                    null -> false
                    else -> when (condition.operator) {
                        ComparisonOperator.LT -> it < condition.threshold
                        ComparisonOperator.LTE -> it <= condition.threshold
                        ComparisonOperator.GT -> it > condition.threshold
                        ComparisonOperator.GTE -> it >= condition.threshold
                        ComparisonOperator.EQ -> it == condition.threshold
                        ComparisonOperator.NEQ -> it != condition.threshold
                    }
                }
            }
        }
        return AutomationAuditEvent(
            rule.id, rule.version, timestampMs, evaluation.observedValues, results,
            rule.action.id, rule.action.policy,
            evaluation.reason
        )
    }
}
