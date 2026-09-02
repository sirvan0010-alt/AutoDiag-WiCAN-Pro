package com.autodiag.core.capability

enum class CapabilityStatus { AVAILABLE, PARTIAL, UNAVAILABLE, UNKNOWN, ERROR }

enum class VerificationState { UNVERIFIED, PARTIALLY_VERIFIED, VERIFIED }

data class VehicleIdentity(
    val vin: String? = null,
    val make: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val softwareVersion: String? = null,
    val adapterInfo: String? = null
)

data class Capability(
    val id: String,
    val displayName: String,
    val status: CapabilityStatus,
    val detail: String? = null,
    /** User-facing explanation. Never invents vehicle values. */
    val userMessage: String? = null,
    val verification: VerificationState = VerificationState.UNVERIFIED
)

data class CapabilitySnapshot(
    val vehicleIdentity: VehicleIdentity?,
    val capabilities: Map<String, Capability>,
    val vinAudit: VinAudit = VinAudit(),
    val discoveredAtEpochMs: Long = System.currentTimeMillis(),
    val scopeKey: String = "session"
)

object CapabilityIds {
    const val COMMUNICATION = "system.communication"
    const val OBD_PROTOCOL = "obd.protocol"
    const val OBD_VIN = "obd.vin"
    const val OBD_MODE_01 = "obd.mode01"
    const val OBD_MODE_03 = "obd.mode03"
    const val BATTERY_CELLS = "battery.cells"
    const val BATTERY_SOC = "battery.soc"
    const val HV_ISOLATION_STATUS = "hv.isolation_status"
    const val HV_ISOLATION_NUMERIC = "hv.isolation_numeric"
    const val DTC_ALERTS = "diagnostics.dtc_alerts"
}
