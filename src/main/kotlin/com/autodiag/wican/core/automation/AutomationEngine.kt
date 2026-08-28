package com.autodiag.wican.core.automation

/** Actions are deliberately separated from vehicle WRITE/COMMAND operations. */
enum class AutomationAction { LOG, ANALYZE, NOTIFY, MQTT_PUBLISH }

data class RuleCondition(val signal: String, val operator: String, val value: Double)

data class AutomationRule(
    val id: String,
    val name: String,
    val enabled: Boolean = false,
    val conditions: List<RuleCondition> = emptyList(),
    val actions: List<AutomationAction> = emptyList(),
    val cooldownSeconds: Long = 60,
    val dryRun: Boolean = true
)

data class RuleEvaluation(
    val ruleId: String,
    val matched: Boolean,
    val dryRun: Boolean,
    val triggeredActions: List<AutomationAction>,
    val explanation: String
)

/** Deterministic evaluator suitable for replay/simulation. */
object AutomationEvaluator {
    fun evaluate(rule: AutomationRule, values: Map<String, Double>): RuleEvaluation {
        val missing = rule.conditions.firstOrNull { !values.containsKey(it.signal) }
        if (missing != null) {
            return RuleEvaluation(rule.id, false, rule.dryRun, emptyList(), "Chybí hodnota signálu: ${missing.signal}")
        }
        val matched = rule.conditions.all { c ->
            val v = values.getValue(c.signal)
            when (c.operator) {
                "<" -> v < c.value
                "<=" -> v <= c.value
                "=" -> v == c.value
                ">=" -> v >= c.value
                ">" -> v > c.value
                else -> false
            }
        }
        return RuleEvaluation(rule.id, matched, rule.dryRun, if (matched) rule.actions else emptyList(),
            if (matched) "Pravidlo splnilo všechny podmínky." else "Podmínky pravidla nejsou splněny.")
    }
}
