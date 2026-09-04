package com.autodiag.core.obd

import com.autodiag.core.diagnostic.DiagnosticEvidenceStore
import com.autodiag.core.diagnostic.EvidenceVerification

/** Read-only Mode 06 workflow: discover supported MIDs, then read each MID. */
class Mode06Reader(private val session: Elm327Session) {
    suspend fun discover(): Mode06DiscoveryResult {
        val windows = mutableListOf<Mode06DiscoveryWindow>()
        var base = 0x00
        while (base <= 0xE0) {
            val response = session.command(Mode06DiscoveryRequest(base).toCommand())
            val window = Mode06DiscoveryDecoder.decode(response, base) ?: break
            windows += window
            val next = Mode06DiscoveryDecoder.nextBase(window) ?: break
            base = next
        }
        return Mode06DiscoveryResult(windows)
    }

    suspend fun readSupportedMonitors(): List<ObdMode06TestResult> =
        discover().supportedMids.flatMap { mid ->
            Mode06Decoder.decode(session.command(ObdMode06Request(mid).toCommand()))?.results.orEmpty()
        }

    /** Execute the complete read-only workflow and preserve decoded reports as evidence. */
    suspend fun readAndRecord(
        evidenceStore: DiagnosticEvidenceStore,
        nowEpochMs: () -> Long,
        verification: EvidenceVerification = EvidenceVerification.UNVERIFIED,
        ecuId: String? = null,
        sourceId: String? = null,
    ): List<ObdMode06Report> {
        val reports = discover().supportedMids.mapNotNull { mid ->
            Mode06Decoder.decode(session.command(ObdMode06Request(mid).toCommand()))
        }
        reports.forEach { report ->
            evidenceStore.appendMode06Report(
                report = report,
                timestampEpochMs = nowEpochMs(),
                verification = verification,
                ecuId = ecuId,
                sourceId = sourceId
            )
        }
        return reports
    }
}
