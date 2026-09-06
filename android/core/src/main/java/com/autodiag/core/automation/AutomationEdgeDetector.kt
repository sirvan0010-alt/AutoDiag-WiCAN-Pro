package com.autodiag.core.automation

/** Detects a transition into a satisfied trigger without generating repeated events. */
class AutomationEdgeDetector {
    private val previous = mutableMapOf<String, Boolean>()

    fun evaluate(rule: AutomationRule, signals: List<SemanticSignal>): AutomationEvaluation {
        val current = AutomationRuleEvaluator.evaluate(rule, signals)
        val wasSatisfied = previous[rule.id] ?: false
        val nowSatisfied = current.conditionsSatisfied
        previous[rule.id] = nowSatisfied
        return if (nowSatisfied && !wasSatisfied && current.reason != "WRITE_COMMAND_REQUIRES_SEPARATE_SAFETY_GATE") {
            current
        } else {
            current.copy(triggered = false, reason = if (!nowSatisfied) current.reason else "ALREADY_SATISFIED")
        }
    }

    fun reset(ruleId: String) {
        previous.remove(ruleId)
    }
}
