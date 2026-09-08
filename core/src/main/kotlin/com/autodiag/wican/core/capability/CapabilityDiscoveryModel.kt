package com.autodiag.wican.core.capability

/** Capability state is intentionally richer than a boolean. */
enum class CapabilityState { AVAILABLE, PARTIAL, UNAVAILABLE, UNKNOWN, ERROR }

enum class VerificationState { UNVERIFIED, PARTIALLY_VERIFIED, VERIFIED }

enum class CapabilitySource { VEHICLE_RESPONSE, ECU_METADATA, ADAPTER_METADATA, DECODER, USER_CONFIRMED }

data class VehicleIdentity(
    val vin: String? = null,
    val make: String? = null,
    val model: String? = null,
    val modelYear: Int? = null,
    val trim: String? = null,
    val batteryVariant: String? = null,
    val driveUnitVariant: String? = null,
    val firmwareVersion: String? = null,
    val softwareVersion: String? = null
)

data class CapabilityCacheKey(
    val vin: String?,
    val make: String?,
    val model: String?,
    val modelYear: Int?,
    val firmwareVersion: String?,
    val softwareVersion: String?,
    val adapterFirmware: String?,
    val protocolVersion: String?
)

data class CapabilityObservation(
    val id: String,
    val state: CapabilityState,
    val source: CapabilitySource,
    val verification: VerificationState,
    val scope: String? = null,
    val detail: String? = null,
    val timestampEpochMs: Long
)

data class MarketHint(
    val market: String?,
    val confidence: Double,
    val source: String,
    val warningRequired: Boolean
)

data class CapabilitySnapshot(
    val identity: VehicleIdentity,
    val observations: List<CapabilityObservation>,
    val marketHint: MarketHint? = null,
    val cacheKey: CapabilityCacheKey
) {
    fun capability(id: String): CapabilityObservation? = observations.firstOrNull { it.id == id }
}

/**
 * Market warning is evidence-gated. Model/year or user location alone must not produce US-market.
 */
fun MarketHint.usWarningText(): String? =
    if (warningRequired && market.equals("US", ignoreCase = true)) {
        "⚠ US-market vehicle detected — some functions/specifications may differ from EU vehicles."
    } else null
