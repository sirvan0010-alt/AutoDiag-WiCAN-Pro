package com.autodiag.core.automation

import com.autodiag.core.diagnostic.PrePurchaseStage

/** Read-only AUTO TEST plan. Execution is intentionally delegated to verified stage adapters. */
data class AutoTestPlan(
    val stages: List<PrePurchaseStage> = listOf(
        PrePurchaseStage.CONNECT,
        PrePurchaseStage.IDENTIFY,
        PrePurchaseStage.DISCOVER,
        PrePurchaseStage.DTC,
        PrePurchaseStage.FREEZE_FRAME,
        PrePurchaseStage.READINESS,
        PrePurchaseStage.LIVE_DATA,
        PrePurchaseStage.MONITORS,
        PrePurchaseStage.EV_TESTS,
        PrePurchaseStage.ANALYZE,
        PrePurchaseStage.REPAIR_ESTIMATE,
        PrePurchaseStage.REPORT
    ),
    val dryRun: Boolean = true
)

data class AutoTestResult(
    val completed: List<PrePurchaseStage>,
    val skipped: List<PrePurchaseStage>,
    val failed: Map<PrePurchaseStage, String>
)
