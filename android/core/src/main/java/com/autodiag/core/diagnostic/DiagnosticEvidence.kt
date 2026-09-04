package com.autodiag.core.diagnostic

/** Availability of the underlying diagnostic information. */
enum class EvidenceAvailability {
    AVAILABLE,
    PARTIAL,
    UNAVAILABLE,
    UNKNOWN,
    ERROR
}

/** Degree to which the information is verified for its declared scope. */
enum class EvidenceVerification {
    UNVERIFIED,
    PARTIALLY_VERIFIED,
    VERIFIED
}

/** Where a diagnostic value originated. */
enum class EvidenceSource {
    ELM327,
    CAN,
    ISO_TP,
    UDS,
    OBD_MODE_01,
    OBD_MODE_02,
    OBD_MODE_03,
    OBD_MODE_06,
    OBD_MODE_09,
    VEHICLE_PROFILE,
    DERIVED,
    REPLAY
}

/** Immutable provenance attached to a diagnostic observation. */
data class EvidenceProvenance(
    val source: EvidenceSource,
    val sourceId: String? = null,
    val ecuId: String? = null,
    val vehicleProfile: String? = null,
    val rawRepresentation: String? = null
)

/** Generic evidence record usable by live data, DTCs, tests and reports. */
data class DiagnosticEvidence<T>(
    val key: String,
    val value: T?,
    val unit: String? = null,
    val timestampEpochMs: Long,
    val availability: EvidenceAvailability,
    val verification: EvidenceVerification,
    val provenance: EvidenceProvenance,
    val isDerived: Boolean = false,
    val quality: String? = null,
    val note: String? = null
)

/** Identity of one diagnostic session. */
data class DiagnosticSessionIdentity(
    val sessionId: String,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long? = null
)

/** Minimal immutable session envelope; additional evidence can be attached by higher layers. */
data class DiagnosticSessionEvidence(
    val identity: DiagnosticSessionIdentity,
    val vehicleIdentity: String? = null,
    val adapterIdentity: String? = null,
    val capabilities: Map<String, EvidenceAvailability> = emptyMap(),
    val measurements: List<DiagnosticEvidence<*>> = emptyList()
)
