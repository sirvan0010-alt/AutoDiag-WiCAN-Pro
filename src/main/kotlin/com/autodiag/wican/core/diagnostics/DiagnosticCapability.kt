package com.autodiag.wican.core.diagnostics

/**
 * Capability-first model for a long-lived multi-vehicle diagnostic platform.
 *
 * A capability is never considered universal merely because a protocol exists:
 * vehicle, ECU, adapter and verification scope remain part of the result.
 */
data class DiagnosticCapability(
    val id: String,
    val name: String,
    val status: CapabilityStatus = CapabilityStatus.UNKNOWN,
    val scope: DiagnosticScope? = null,
    val requirements: Set<CapabilityRequirement> = emptySet(),
    val verification: EvidenceVerification = EvidenceVerification.UNVERIFIED,
    val evidenceIds: List<String> = emptyList()
)

enum class CapabilityStatus {
    AVAILABLE,
    AVAILABLE_WITH_PREREQUISITES,
    REQUIRES_OEM_SECURITY,
    REQUIRES_ADDITIONAL_HARDWARE,
    NOT_SUPPORTED,
    UNKNOWN,
    BLOCKED
}

enum class CapabilityRequirement {
    VEHICLE_MATCH,
    ECU_MATCH,
    ADAPTER_FIRMWARE,
    PROTOCOL_SUPPORT,
    OEM_SECURITY,
    ADDITIONAL_HARDWARE,
    LICENSED_DATA,
    USER_CONFIRMATION,
    QUALIFIED_SERVICE
}

data class AdapterCapabilityScope(
    val adapterModel: String? = null,
    val firmwareVersion: String? = null,
    val transport: String? = null,
    val protocol: String? = null
)

/** Combined scope used when a capability depends on both vehicle and adapter. */
data class DiagnosticCapabilityScope(
    val vehicle: VehicleEvidenceScope? = null,
    val ecuAddress: Int? = null,
    val adapter: AdapterCapabilityScope? = null
)
