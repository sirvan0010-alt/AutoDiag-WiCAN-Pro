package com.autodiag.core.automation

/** Detects a transition into a satisfied, enabled read-only trigger without repetition. */
class AutomationEdgeDetector {
    private val previous = mutableMapOf<String, Boolean>()

    fun evaluate(rule: AutomationRule, signals: List<SemanticSignal>): AutomationEvaluation {
        val current = AutomationRuleEvaluator.evaluate(rule, signals)
        val eligible = rule.enabled && current.conditionsSatisfied && rule.action.policy != AutomationPolicy.WRITE_COMMAND
        val wasEligible = previous[rule.id] ?: false
        previous[rule.id] = eligible
        return if (eligible && !wasEligible) {
            current
        } else {
            current.copy(
                triggered = false,
                reason = if (!eligible) current.reason else "ALREADY_SATISFIED"
            )
        }
    }

    fun reset(ruleId: String) {
        previous.remove(ruleId)
    }
}
