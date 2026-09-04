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

    fun fromInterpreted(
        result: Mode06InterpretedResult,
        timestampEpochMs: Long,
        verification: EvidenceVerification = EvidenceVerification.UNVERIFIED,
        ecuId: String? = null,
        sourceId: String? = null,
    ): DiagnosticEvidence<Mode06InterpretedResult> = DiagnosticEvidence(
        key = "obd.mode06.mid.%02X.tid.%02X".format(result.raw.obdMid, result.raw.testId),
        value = result,
        unit = result.value?.unit,
        timestampEpochMs = timestampEpochMs,
        availability = when {
            result.value == null -> EvidenceAvailability.UNKNOWN
            else -> EvidenceAvailability.AVAILABLE
        },
        verification = verification,
        provenance = EvidenceProvenance(
            source = EvidenceSource.OBD_MODE_06,
            sourceId = sourceId,
            ecuId = ecuId,
            rawRepresentation = listOf(
                result.raw.obdMid, result.raw.testId, result.raw.unitAndScalingId,
                result.raw.testValueRaw ushr 8, result.raw.testValueRaw and 0xFF,
                result.raw.minimumRaw ushr 8, result.raw.minimumRaw and 0xFF,
                result.raw.maximumRaw ushr 8, result.raw.maximumRaw and 0xFF
            ).joinToString(" ") { "%02X".format(it) }
        ),
        isDerived = true,
        quality = "status:${result.status.name};band:${result.bandPosition}"
    )
}

fun DiagnosticEvidenceStore.appendMode06Report(
    report: ObdMode06Report,
    timestampEpochMs: Long,
    verification: EvidenceVerification = EvidenceVerification.UNVERIFIED,
    ecuId: String? = null,
    sourceId: String? = null,
) = append(Mode06EvidenceFactory.fromReport(report, timestampEpochMs, verification, ecuId, sourceId))
