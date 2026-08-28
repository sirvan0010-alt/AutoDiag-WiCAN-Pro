package com.autodiag.wican.core.automation

/** Data-only, auditable automation rule. WRITE/COMMAND actions are intentionally not represented here. */
data class AutomationRule(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val whenCondition: Condition,
    val action: Action,
    val cooldownSeconds: Long = 0
)

sealed interface Condition {
    data class MetricAbove(val metric: String, val threshold: Double) : Condition
    data class MetricBelow(val metric: String, val threshold: Double) : Condition
    data class MetricEquals(val metric: String, val value: String) : Condition
    data class All(val conditions: List<Condition>) : Condition
    data class Any(val conditions: List<Condition>) : Condition
}

sealed interface Action {
    data class Notify(val message: String) : Action
    data class SaveCapture(val label: String) : Action
    data class PublishMqtt(val topic: String, val payloadTemplate: String) : Action
}

data class RuleEvaluation(
    val ruleId: String,
    val triggered: Boolean,
    val reason: String,
    val dryRun: Boolean
)
