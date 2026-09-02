package com.autodiag.core.obd

/** Where a DTC was read from the vehicle. */
enum class DtcMemory {
    STORED,
    PENDING,
    PERMANENT,
    UNKNOWN
}

enum class DtcProtocol {
    OBD_MODE_03,
    OBD_MODE_07,
    OBD_MODE_0A,
    UDS_19,
    UNKNOWN
}

/** A diagnostic trouble code as reported by an ECU. */
data class DiagnosticTroubleCode(
    val code: String,
    val memory: DtcMemory,
    val protocol: DtcProtocol,
    val rawBytes: ByteArray = byteArrayOf(),
    val statusMask: Int? = null,
    val ecuAddress: Int? = null
) {
    init {
        require(code.matches(Regex("^[PCBU][0-9A-F]{4}$"))) { "Invalid DTC code: $code" }
        require(statusMask == null || statusMask in 0..0xFF) { "statusMask must be 0..255" }
    }

    override fun equals(other: Any?): Boolean = other is DiagnosticTroubleCode &&
        code == other.code && memory == other.memory && protocol == other.protocol &&
        rawBytes.contentEquals(other.rawBytes) && statusMask == other.statusMask && ecuAddress == other.ecuAddress

    override fun hashCode(): Int = (((code.hashCode() * 31 + memory.hashCode()) * 31 + protocol.hashCode()) * 31 +
        rawBytes.contentHashCode()) * 31 + (statusMask ?: -1) * 31 + (ecuAddress ?: -1)
}

enum class DtcClearDecision {
    ALLOWED_WITH_CONFIRMATION,
    REQUIRES_EXACT_SCOPE,
    REQUIRES_SECURITY,
    NOT_SUPPORTED,
    UNKNOWN
}

/** Safety policy for clearing ECU DTC memory. Clearing is a write/state-changing operation. */
object DtcClearPolicy {
    fun evaluate(
        exactVehicleAndEcuMatch: Boolean,
        explicitlySupported: Boolean,
        securityRequired: Boolean,
        securityEstablished: Boolean,
        userConfirmed: Boolean
    ): DtcClearDecision = when {
        !exactVehicleAndEcuMatch -> DtcClearDecision.REQUIRES_EXACT_SCOPE
        !explicitlySupported -> DtcClearDecision.UNKNOWN
        securityRequired && !securityEstablished -> DtcClearDecision.REQUIRES_SECURITY
        !userConfirmed -> DtcClearDecision.ALLOWED_WITH_CONFIRMATION
        else -> DtcClearDecision.ALLOWED_WITH_CONFIRMATION
    }
}
