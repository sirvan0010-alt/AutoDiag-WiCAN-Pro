package com.autodiag.core.obd

import com.autodiag.core.diagnostic.CaptureDirection
import com.autodiag.core.diagnostic.DiagnosticCaptureRecord
import com.autodiag.core.diagnostic.DiagnosticEvidenceStore
import com.autodiag.core.diagnostic.EvidenceSource
import com.autodiag.core.transport.ReplayTransport
import com.autodiag.core.transport.TransportConfig
import com.autodiag.core.transport.TransportMode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class Mode06ReaderEvidenceTest {
    @Test
    fun discoversMidReadsItAndRecordsEvidence() = runBlocking {
        val records = listOf(
            record(CaptureDirection.TX, "0600\r"),
            record(CaptureDirection.RX, "46 00 40 00 00 00\r>"),
            record(CaptureDirection.TX, "0602\r"),
            record(CaptureDirection.RX, "46 02 01 27 00 C8 00 64 00 F0\r>")
        )
        val transport = ReplayTransport(records)
        transport.connect(TransportConfig("replay", 0, TransportMode.SIMULATOR)).getOrThrow()

        val evidence = DiagnosticEvidenceStore()
        val reports = Mode06Reader(Elm327Session(transport)).readAndRecord(
            evidenceStore = evidence,
            nowEpochMs = { 20_000L },
            ecuId = "replay-ecu",
            sourceId = "capture-mode06"
        )

        assertEquals(1, reports.size)
        assertEquals(1, reports.single().results.size)
        assertEquals(2, reports.single().results.single().obdMid)
        assertEquals(200, reports.single().results.single().testValueRaw)
        assertEquals(1, evidence.snapshot().size)
        val item = evidence.snapshot().single()
        assertEquals("obd.mode06.mid.02", item.key)
        assertEquals(EvidenceSource.OBD_MODE_06, item.provenance.source)
        assertEquals("capture-mode06", item.provenance.sourceId)
        assertEquals("replay-ecu", item.provenance.ecuId)
    }

    private fun record(direction: CaptureDirection, payload: String) =
        DiagnosticCaptureRecord("mode06-replay", 1_000L, direction, payload.toByteArray(), "replay")
}
