package com.autodiag.core.diagnostic

/** Cancellable, auditable read-only pre-purchase diagnostic state machine. */
enum class PrePurchaseStage { CONNECT, IDENTIFY, DISCOVER, DTC, FREEZE_FRAME, READINESS, LIVE_DATA, MONITORS, EV_TESTS, ANALYZE, REPAIR_ESTIMATE, REPORT }

data class PrePurchaseStep(val stage: PrePurchaseStage, val status: Status = Status.PENDING, val detail: String? = null) {
    enum class Status { PENDING, RUNNING, COMPLETE, SKIPPED, FAILED }
}

class PrePurchaseWorkflow {
    private val stages = PrePurchaseStage.entries.toMutableList()
    private val state = stages.associateWith { PrePurchaseStep(it) }.toMutableMap()

    fun snapshot(): List<PrePurchaseStep> = stages.map { state.getValue(it) }

    fun start(stage: PrePurchaseStage) { state[stage] = PrePurchaseStep(stage, PrePurchaseStep.Status.RUNNING) }
    fun complete(stage: PrePurchaseStage, detail: String? = null) { state[stage] = PrePurchaseStep(stage, PrePurchaseStep.Status.COMPLETE, detail) }
    fun skip(stage: PrePurchaseStage, detail: String? = null) { state[stage] = PrePurchaseStep(stage, PrePurchaseStep.Status.SKIPPED, detail) }
    fun fail(stage: PrePurchaseStage, detail: String) { state[stage] = PrePurchaseStep(stage, PrePurchaseStep.Status.FAILED, detail) }
}
