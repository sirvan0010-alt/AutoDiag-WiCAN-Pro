package com.autodiag.core.obd

import com.autodiag.core.diagnostic.CaptureDirection
import com.autodiag.core.diagnostic.DiagnosticCaptureRecord
import com.autodiag.core.diagnostic.DiagnosticEvidenceStore
import com.autodiag.core.diagnostic.EvidenceAvailability
import com.autodiag.core.diagnostic.EvidenceSource
import com.autodiag.core.diagnostic.EvidenceVerification
import com.autodiag.core.transport.ReplayTransport
import com.autodiag.core.transport.TransportConfig
import com.autodiag.core.transport.TransportMode
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ReplayObdEvidenceIntegrationTest {
    @Test
    fun replayedMode01MeasurementProducesReplayProvenance() = runBlocking {
        val records = listOf(
            record(CaptureDirection.TX, "010C\r"),
            record(CaptureDirection.RX, "41 0C 1A F8\r>")
        )
        val transport = ReplayTransport(records)
        transport.connect(TransportConfig("replay", 0, TransportMode.SIMULATOR)).getOrThrow()

        val evidence = DiagnosticEvidenceStore()
        val sample = ObdLiveDataEngine(
            session = Elm327Session(transport),
            nowEpochMs = { 10_000L },
            evidenceStore = evidence,
            evidenceVerification = EvidenceVerification.PARTIALLY_VERIFIED,
            evidenceEcuId = "replay-ecu",
            evidenceSourceId = "capture-001",
            evidenceSource = EvidenceSource.REPLAY
        ).stream(setOf(0x0C), intervalMs = 1_000)
            .take(1)
            .toList()
            .single()

        assertEquals(1726.0, sample.value)
        val item = evidence.snapshot().single()
        assertEquals("obd.mode01.pid.0C", item.key)
        assertEquals(1726.0, item.value)
        assertEquals(EvidenceAvailability.AVAILABLE, item.availability)
        assertEquals(EvidenceVerification.PARTIALLY_VERIFIED, item.verification)
        assertEquals(EvidenceSource.REPLAY, item.provenance.source)
        assertEquals("capture-001", item.provenance.sourceId)
        assertEquals("replay-ecu", item.provenance.ecuId)
        assertEquals("41 0C 1A F8", item.provenance.rawRepresentation)
    }

    private fun record(direction: CaptureDirection, payload: String) =
        DiagnosticCaptureRecord("replay-session", 1_000L, direction, payload.toByteArray(), "replay")
}
