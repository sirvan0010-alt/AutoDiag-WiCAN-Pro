package com.autodiag.wican.core.diagnostics

/**
 * Immutable evidence captured during diagnostics.
 *
 * Evidence is deliberately separate from interpretation: a DTC or raw response
 * must never be converted into a failed-component claim without provenance.
 */
data class DiagnosticEvidence(
    val timestampEpochMs: Long,
    val source: EvidenceSource,
    val vehicleScope: VehicleEvidenceScope? = null,
    val ecuAddress: Int? = null,
    val protocol: String? = null,
    val request: ByteArray? = null,
    val response: ByteArray? = null,
    val text: String? = null,
    val verification: EvidenceVerification = EvidenceVerification.UNVERIFIED,
    val provenance: String? = null
)

enum class EvidenceSource {
    LIVE_ADAPTER,
    REPLAY,
    SIMULATOR,
    IMPORTED_LOG,
    USER_ENTERED
}

enum class EvidenceVerification {
    UNVERIFIED,
    PARTIALLY_VERIFIED,
    VERIFIED
}

data class VehicleEvidenceScope(
    val vin: String? = null,
    val make: String? = null,
    val model: String? = null,
    val generation: String? = null,
    val modelYear: Int? = null,
    val productionDate: String? = null,
    val engine: String? = null,
    val motor: String? = null,
    val battery: String? = null,
    val transmission: String? = null,
    val drivetrain: String? = null,
    val region: String? = null
)

/** High-level event types used by the diagnostic evidence stream. */
sealed interface DiagnosticEvent {
    val timestampEpochMs: Long

    data class ConnectionChanged(
        override val timestampEpochMs: Long,
        val connected: Boolean,
        val transport: String
    ) : DiagnosticEvent

    data class FrameReceived(
        override val timestampEpochMs: Long,
        val canId: Int,
        val data: ByteArray,
        val extended: Boolean = false
    ) : DiagnosticEvent

    data class DiagnosticResponse(
        override val timestampEpochMs: Long,
        val evidence: DiagnosticEvidence
    ) : DiagnosticEvent

    data class CommunicationError(
        override val timestampEpochMs: Long,
        val message: String
    ) : DiagnosticEvent
}
