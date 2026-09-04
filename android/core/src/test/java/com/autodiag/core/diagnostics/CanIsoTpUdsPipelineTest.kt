package com.autodiag.core.diagnostics

import com.autodiag.core.can.CanFrame
import com.autodiag.core.diagnostic.DiagnosticEvidenceStore
import com.autodiag.core.diagnostic.EvidenceAvailability
import com.autodiag.core.diagnostic.EvidenceSource
import com.autodiag.core.diagnostic.EvidenceVerification
import com.autodiag.core.diagnostics.uds.CanIsoTpUdsPipeline
import com.autodiag.core.diagnostics.uds.UdsNegativeResponse
import com.autodiag.core.diagnostics.uds.UdsPipelineResult
import com.autodiag.core.diagnostics.uds.UdsPositiveResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CanIsoTpUdsPipelineTest {
    @Test
    fun singleFrameProducesPositiveUdsEvidence() {
        val store = DiagnosticEvidenceStore()
        val pipeline = CanIsoTpUdsPipeline(
            evidenceStore = store,
            verification = EvidenceVerification.PARTIALLY_VERIFIED,
            sourceId = "7E8",
            ecuId = "engine",
        )

        val result = pipeline.accept(CanFrame(0x7E8, byteArrayOf(0x02, 0x50, 0x01), timestampNanos = 2_000_000)).getOrThrow()
        val complete = result as UdsPipelineResult.Complete
        assertTrue(complete.response is UdsPositiveResponse)
        assertEquals(1, store.snapshot().size)
        assertEquals(EvidenceSource.UDS, store.snapshot().single().provenance.source)
        assertEquals(EvidenceVerification.PARTIALLY_VERIFIED, store.snapshot().single().verification)
    }

    @Test
    fun negativeResponseIsUnavailableNotTransportError() {
        val store = DiagnosticEvidenceStore()
        val pipeline = CanIsoTpUdsPipeline(evidenceStore = store)

        val result = pipeline.accept(CanFrame(0x7E8, byteArrayOf(0x03, 0x7F, 0x22, 0x31))).getOrThrow()
        val complete = result as UdsPipelineResult.Complete
        val response = complete.response as UdsNegativeResponse

        assertEquals(0x22, response.serviceId)
        assertEquals(0x31, response.responseCode)
        assertEquals(EvidenceAvailability.UNAVAILABLE, complete.evidence.availability)
        assertEquals(EvidenceSource.UDS, complete.evidence.provenance.source)
        assertEquals("NRC=0x31", complete.evidence.note)
    }

    @Test
    fun multiFrameResponseIsReassembledBeforeUdsParsing() {
        val pipeline = CanIsoTpUdsPipeline()

        val first = pipeline.accept(
            CanFrame(0x7E8, byteArrayOf(0x10, 0x0A, 0x62, 0xF1.toByte(), 0x90.toByte(), 0x41, 0x42, 0x43))
        ).getOrThrow()
        assertEquals(UdsPipelineResult.Incomplete, first)

        val second = pipeline.accept(
            CanFrame(0x7E8, byteArrayOf(0x21, 0x44, 0x45, 0x46, 0x47))
        ).getOrThrow()
        val complete = second as UdsPipelineResult.Complete
        val response = complete.response as UdsPositiveResponse

        assertEquals(0x62, response.serviceId)
        assertTrue(response.payload.contentEquals(byteArrayOf(0xF1.toByte(), 0x90.toByte(), 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47)))
    }

    @Test
    fun malformedIsoTpFrameFailsAtProtocolLayer() {
        val pipeline = CanIsoTpUdsPipeline()
        val result = pipeline.accept(CanFrame(0x7E8, byteArrayOf(0x10)))
        assertTrue(result.isFailure)
    }
}
