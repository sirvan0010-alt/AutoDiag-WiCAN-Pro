package com.autodiag.core.diagnostics.uds

import com.autodiag.core.diagnostic.DiagnosticEvidence
import com.autodiag.core.diagnostic.DiagnosticEvidenceStore
import com.autodiag.core.diagnostic.EvidenceAvailability
import com.autodiag.core.diagnostic.EvidenceProvenance
import com.autodiag.core.diagnostic.EvidenceSource
import com.autodiag.core.diagnostic.EvidenceVerification

object UdsDtcEvidenceFactory {
    fun fromReport(
        report: UdsDtcReport,
        timestampEpochMs: Long? = null,
        verification: EvidenceVerification = EvidenceVerification.UNVERIFIED,
        sourceId: String? = null,
        ecuId: String? = null,
    ): DiagnosticEvidence<UdsDtcReport> = DiagnosticEvidence(
        key = "uds.dtc.subfunction.%02X".format(report.subFunction),
        value = report,
        unit = null,
        timestampEpochMs = timestampEpochMs ?: System.currentTimeMillis(),
        availability = EvidenceAvailability.AVAILABLE,
        verification = verification,
        provenance = EvidenceProvenance(
            source = EvidenceSource.UDS,
            sourceId = sourceId,
            ecuId = ecuId,
            rawRepresentation = report.rawPayload.joinToString(" ") { "%02X".format(it.toInt() and 0xFF) },
        ),
        isDerived = false,
        quality = "dtc:${report.dtcs.size}",
    )
}

fun DiagnosticEvidenceStore.appendUdsDtcReport(
    report: UdsDtcReport,
    timestampEpochMs: Long? = null,
    verification: EvidenceVerification = EvidenceVerification.UNVERIFIED,
    sourceId: String? = null,
    ecuId: String? = null,
) {
    append(UdsDtcEvidenceFactory.fromReport(report, timestampEpochMs, verification, sourceId, ecuId))
}
