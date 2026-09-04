package com.autodiag.core.capability

/**
 * Evidence gate for Mitsubishi Outlander PHEV signal definitions.
 *
 * A catalog entry becomes vehicle evidence only when a concrete request/response
 * exchange is observed. This class deliberately does not discover or invent
 * CAN IDs, UDS DIDs, Mode-22 PIDs, byte layouts or scaling factors.
 */
object OutlanderPhevEvidenceGate {
    data class ObservedExchange(
        val request: String,
        val response: String,
        val source: String,
        val timestampEpochMs: Long
    )

    enum class EvidenceStatus { VERIFIED, PARTIAL, REJECTED }

    data class Evaluation(
        val status: EvidenceStatus,
        val signal: SignalDataDefinition?,
        val reason: String
    )

    fun evaluate(
        signal: SignalDataDefinition,
        exchange: ObservedExchange
    ): Evaluation {
        val expected = signal.request?.replace(" ", "")?.uppercase()
        val actual = exchange.request.replace(" ", "").uppercase()

        if (expected.isNullOrBlank()) {
            return Evaluation(
                EvidenceStatus.REJECTED,
                null,
                "Signál nemá evidovaný request; nelze jej ověřit proti konkrétní komunikaci."
            )
        }

        if (expected != actual) {
            return Evaluation(
                EvidenceStatus.REJECTED,
                null,
                "Observed request neodpovídá definici signálu."
            )
        }

        if (exchange.response.isBlank()) {
            return Evaluation(
                EvidenceStatus.REJECTED,
                null,
                "Chybí odpověď vozidla; samotný request není důkaz hodnoty."
            )
        }

        val verified = signal.copy(
            verification = VerificationState.PARTIALLY_VERIFIED,
            provenance = "${signal.provenance}; observed=${exchange.source}"
        )

        return Evaluation(
            EvidenceStatus.PARTIAL,
            verified,
            "Request/response pár byl pozorován; decoder a význam hodnoty musí být ověřeny samostatně."
        )
    }
}
