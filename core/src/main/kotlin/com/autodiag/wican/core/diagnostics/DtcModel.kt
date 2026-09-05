package com.autodiag.core.diagnostics

import com.autodiag.core.obd.VerificationState

enum class DtcStatus { STORED, PENDING, PERMANENT, HISTORY, UNKNOWN }

data class DiagnosticTroubleCode(
    val code: String,
    val status: DtcStatus = DtcStatus.UNKNOWN,
    val ecu: String? = null,
    val source: String? = null,
    val rawResponse: String? = null,
    val freezeFrame: Map<String, String> = emptyMap(),
    val verification: VerificationState = VerificationState.UNKNOWN
)

data class DiagnosticFinding(
    val dtc: DiagnosticTroubleCode,
    val title: String? = null,
    val explanation: String? = null,
    val repairReference: String? = null,
    val confidence: Double? = null,
    val evidenceIds: List<String> = emptyList()
)
