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

    /** Records a rejected evaluation so stale/missing data is auditable too. */
    fun fromQualityFailure(
        rule: AutomationRule,
        timestampMs: Long,
        quality: DataQualityResult
    ): AutomationAuditEvent {
        val missing = quality.missingSignalIds.joinToString(",")
        val stale = quality.staleSignalIds.joinToString(",")
        val details = buildList {
            if (missing.isNotEmpty()) add("missing=$missing")
            if (stale.isNotEmpty()) add("stale=$stale")
        }.joinToString(";")
        return AutomationAuditEvent(
            ruleId = rule.id,
            ruleVersion = rule.version,
            timestampMs = timestampMs,
            observedValues = emptyMap(),
            conditionResults = emptyMap(),
            actionId = rule.action.id,
            policy = rule.action.policy,
            outcome = "DATA_QUALITY_REJECTED",
            error = listOf(quality.reason, details).filter { it.isNotEmpty() }.joinToString(";")
        )
    }
}
