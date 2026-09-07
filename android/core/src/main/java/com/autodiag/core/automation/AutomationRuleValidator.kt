package com.autodiag.core.automation

/** Deterministic validation for runtime and serialized automation rules. */
data class RuleValidationResult(
    val valid: Boolean,
    val errors: List<String> = emptyList()
)

object AutomationRuleValidator {
    fun validate(rule: AutomationRule): RuleValidationResult {
        val errors = buildList {
            if (rule.id.isBlank()) add("id must not be blank")
            if (rule.version < 1) add("version must be >= 1")
            if (rule.triggerSignalId.isBlank()) add("triggerSignalId must not be blank")
            if (!rule.triggerThreshold.isFinite()) add("triggerThreshold must be finite")
            rule.conditions.forEachIndexed { index, condition ->
                if (condition.signalId.isBlank()) add("conditions[$index].signalId must not be blank")
                if (!condition.threshold.isFinite()) add("conditions[$index].threshold must be finite")
            }
            if (rule.action.id.isBlank()) add("action.id must not be blank")
        }
        return RuleValidationResult(errors.isEmpty(), errors)
    }

    fun requireValid(rule: AutomationRule): AutomationRule {
        val result = validate(rule)
        require(result.valid) { "Invalid automation rule '${rule.id}': ${result.errors.joinToString("; ")}" }
        return rule
    }
}
