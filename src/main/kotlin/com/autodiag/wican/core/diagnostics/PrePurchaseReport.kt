package com.autodiag.wican.core.diagnostics

/** A pre-purchase result never implies a value that was not actually reported. */
enum class EvidenceState { REPORTED, ESTIMATED, NOT_AVAILABLE }
enum class FindingSeverity { INFO, WARNING, CRITICAL }

data class DiagnosticFinding(
    val id: String,
    val titleCs: String,
    val severity: FindingSeverity,
    val evidence: EvidenceState,
    val explanationCs: String,
    val tooltipCs: String,
    val source: String? = null
)

data class PrePurchaseReport(
    val vehicle: String,
    val marketWarning: String? = null,
    val findings: List<DiagnosticFinding> = emptyList(),
    val completedChecks: Int = 0,
    val availableChecks: Int = 0
)
