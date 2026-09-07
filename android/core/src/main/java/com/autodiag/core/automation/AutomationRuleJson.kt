package com.autodiag.core.automation

import org.json.JSONArray
import org.json.JSONObject

/** Stable JSON representation for versioned/exportable automation rules. */
object AutomationRuleJson {
    fun encode(rule: AutomationRule): String = JSONObject().apply {
        AutomationRuleValidator.requireValid(rule)
        put("id", rule.id)
        put("version", rule.version)
        put("triggerSignalId", rule.triggerSignalId)
        put("triggerOperator", rule.triggerOperator.name)
        put("triggerThreshold", rule.triggerThreshold)
        put("enabled", rule.enabled)
        put("conditions", JSONArray().apply {
            rule.conditions.forEach { c ->
                put(JSONObject().apply {
                    put("signalId", c.signalId)
                    put("operator", c.operator.name)
                    put("threshold", c.threshold)
                })
            }
        })
        put("action", JSONObject().apply {
            put("id", rule.action.id)
            put("policy", rule.action.policy.name)
        })
    }.toString()

    fun decode(json: String): AutomationRule {
        val root = JSONObject(json)
        val action = root.getJSONObject("action")
        val conditionsJson = root.optJSONArray("conditions") ?: JSONArray()
        val conditions = buildList {
            for (i in 0 until conditionsJson.length()) {
                val c = conditionsJson.getJSONObject(i)
                add(Condition(
                    c.getString("signalId"),
                    ComparisonOperator.valueOf(c.getString("operator")),
                    c.getDouble("threshold")
                ))
            }
        }
        return AutomationRuleValidator.requireValid(
            AutomationRule(
                id = root.getString("id"),
                version = root.optInt("version", 1),
                triggerSignalId = root.getString("triggerSignalId"),
                triggerOperator = ComparisonOperator.valueOf(root.getString("triggerOperator")),
                triggerThreshold = root.getDouble("triggerThreshold"),
                conditions = conditions,
                action = AutomationAction(
                    action.getString("id"),
                    AutomationPolicy.valueOf(action.getString("policy"))
                ),
                enabled = root.optBoolean("enabled", false)
            )
        )
    }
}
