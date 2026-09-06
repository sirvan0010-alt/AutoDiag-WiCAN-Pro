package com.autodiag.core.automation

/** Stable semantic value used by read-only automation rules. */
data class SemanticSignal(
    val id: String,
    val value: Double?,
    val unit: String? = null,
    val timestampMs: Long = 0L,
    val source: String? = null
)

enum class ComparisonOperator { LT, LTE, GT, GTE, EQ, NEQ }

data class Condition(
    val signalId: String,
    val operator: ComparisonOperator,
    val threshold: Double
)

enum class AutomationPolicy { READ_LOG_ANALYZE, NOTIFY_ALERT, WRITE_COMMAND }

data class AutomationAction(
    val id: String,
    val policy: AutomationPolicy = AutomationPolicy.READ_LOG_ANALYZE
)

data class AutomationRule(
    val id: String,
    val version: Int = 1,
    val triggerSignalId: String,
    val triggerOperator: ComparisonOperator,
    val triggerThreshold: Double,
    val conditions: List<Condition> = emptyList(),
    val action: AutomationAction,
    val enabled: Boolean = false
)

data class AutomationEvaluation(
    val ruleId: String,
    val triggered: Boolean,
    val conditionsSatisfied: Boolean,
    val observedValues: Map<String, Double?>,
    val reason: String
)

/** Read-only evaluator. WRITE_COMMAND is deliberately never executable here. */
object AutomationRuleEvaluator {
    fun evaluate(rule: AutomationRule, signals: List<SemanticSignal>): AutomationEvaluation {
        val values = signals.associate { it.id to it.value }
        val triggerValue = values[rule.triggerSignalId]
        val trigger = compare(triggerValue, rule.triggerOperator, rule.triggerThreshold)
        val conditions = rule.conditions.all { compare(values[it.signalId], it.operator, it.threshold) }
        val writeBlocked = rule.action.policy == AutomationPolicy.WRITE_COMMAND
        val triggered = rule.enabled && trigger && conditions && !writeBlocked
        val reason = when {
            !rule.enabled -> "RULE_DISABLED"
            triggerValue == null -> "TRIGGER_VALUE_UNAVAILABLE"
            !trigger -> "TRIGGER_NOT_SATISFIED"
            !conditions -> "CONDITION_NOT_SATISFIED"
            writeBlocked -> "WRITE_COMMAND_REQUIRES_SEPARATE_SAFETY_GATE"
            else -> "TRIGGERED"
        }
        return AutomationEvaluation(rule.id, triggered, trigger && conditions, values, reason)
    }

    private fun compare(value: Double?, op: ComparisonOperator, threshold: Double): Boolean {
        if (value == null) return false
        return when (op) {
            ComparisonOperator.LT -> value < threshold
            ComparisonOperator.LTE -> value <= threshold
            ComparisonOperator.GT -> value > threshold
            ComparisonOperator.GTE -> value >= threshold
            ComparisonOperator.EQ -> value == threshold
            ComparisonOperator.NEQ -> value != threshold
        }
    }
}
