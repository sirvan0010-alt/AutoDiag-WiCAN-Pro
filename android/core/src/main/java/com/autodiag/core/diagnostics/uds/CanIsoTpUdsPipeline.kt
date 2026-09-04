package com.autodiag.core.diagnostics.uds

import com.autodiag.core.can.CanFrame
import com.autodiag.core.diagnostic.DiagnosticEvidence
import com.autodiag.core.diagnostic.DiagnosticEvidenceStore
import com.autodiag.core.diagnostic.EvidenceAvailability
import com.autodiag.core.diagnostic.EvidenceProvenance
import com.autodiag.core.diagnostic.EvidenceSource
import com.autodiag.core.diagnostic.EvidenceVerification
import com.autodiag.core.diagnostics.isotp.IsoTpReassembler

/** Read-only protocol pipeline: CAN frame -> ISO-TP payload -> UDS response. */
class CanIsoTpUdsPipeline(
    private val reassembler: IsoTpReassembler = IsoTpReassembler(),
    private val evidenceStore: DiagnosticEvidenceStore? = null,
    private val verification: EvidenceVerification = EvidenceVerification.UNVERIFIED,
    private val sourceId: String? = null,
    private val ecuId: String? = null,
) {
    fun reset() = reassembler.reset()

    fun accept(frame: CanFrame): Result<UdsPipelineResult> = runCatching {
        val payload = reassembler.accept(frame).getOrThrow() ?: return@runCatching UdsPipelineResult.Incomplete
        val response = UdsResponseParser.parse(payload).getOrThrow()
        val evidence = UdsEvidenceFactory.fromResponse(
            response = response,
            rawPayload = payload,
            timestampEpochMs = frame.timestampNanos?.div(1_000_000) ?: System.currentTimeMillis(),
            verification = verification,
            sourceId = sourceId ?: "can:0x${frame.id.toString(16).uppercase()}",
            ecuId = ecuId,
        )
        evidenceStore?.append(evidence)
        UdsPipelineResult.Complete(response, evidence)
    }
}

sealed interface UdsPipelineResult {
    data object Incomplete : UdsPipelineResult
    data class Complete(val response: UdsResponse, val evidence: DiagnosticEvidence<UdsResponse>) : UdsPipelineResult
}

object UdsEvidenceFactory {
    fun fromResponse(
        response: UdsResponse,
        rawPayload: ByteArray,
        timestampEpochMs: Long? = null,
        verification: EvidenceVerification = EvidenceVerification.UNVERIFIED,
        sourceId: String? = null,
        ecuId: String? = null,
    ): DiagnosticEvidence<UdsResponse> {
        val availability = when (response) {
            is UdsPositiveResponse -> EvidenceAvailability.AVAILABLE
            is UdsNegativeResponse -> EvidenceAvailability.UNAVAILABLE
        }
        val serviceId = response.serviceId and 0xFF
        return DiagnosticEvidence(
            key = "uds.service.%02X".format(serviceId), value = response, unit = null,
            timestampEpochMs = timestampEpochMs ?: System.currentTimeMillis(), availability = availability,
            verification = verification,
            provenance = EvidenceProvenance(
                source = EvidenceSource.UDS, sourceId = sourceId, ecuId = ecuId,
                rawRepresentation = rawPayload.joinToString(" ") { "%02X".format(it.toInt() and 0xFF) },
            ), isDerived = false, quality = if (response is UdsPositiveResponse) "positive" else "negative",
            note = (response as? UdsNegativeResponse)?.let { "NRC=0x%02X".format(it.responseCode) },
        )
    }
}
