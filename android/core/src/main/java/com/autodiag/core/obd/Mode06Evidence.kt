package com.autodiag.core.obd

import com.autodiag.core.diagnostic.DiagnosticEvidence
import com.autodiag.core.diagnostic.DiagnosticEvidenceStore
import com.autodiag.core.diagnostic.EvidenceAvailability
import com.autodiag.core.diagnostic.EvidenceProvenance
import com.autodiag.core.diagnostic.EvidenceSource
import com.autodiag.core.diagnostic.EvidenceVerification

object Mode06EvidenceFactory {
    fun fromReport(
        report: ObdMode06Report,
        timestampEpochMs: Long,
        verification: EvidenceVerification = EvidenceVerification.UNVERIFIED,
        ecuId: String? = null,
        sourceId: String? = null,
    ): DiagnosticEvidence<ObdMode06Report> = DiagnosticEvidence(
        key = "obd.mode06.mid.${report.obdMid?.let { "%02X".format(it) } ?: "unknown"}",
        value = report,
        timestampEpochMs = timestampEpochMs,
        availability = if (report.results.isEmpty()) EvidenceAvailability.PARTIAL else EvidenceAvailability.AVAILABLE,
        verification = verification,
        provenance = EvidenceProvenance(
            source = EvidenceSource.OBD_MODE_06,
            sourceId = sourceId,
            ecuId = ecuId,
            rawRepresentation = report.rawPayload.joinToString(" ") { "%02X".format(it) }
        ),
        isDerived = false,
        quality = "tests:${report.results.size}"
    )
}

fun DiagnosticEvidenceStore.appendMode06Report(
    report: ObdMode06Report,
    timestampEpochMs: Long,
    verification: EvidenceVerification = EvidenceVerification.UNVERIFIED,
    ecuId: String? = null,
    sourceId: String? = null,
) = append(Mode06EvidenceFactory.fromReport(report, timestampEpochMs, verification, ecuId, sourceId))
