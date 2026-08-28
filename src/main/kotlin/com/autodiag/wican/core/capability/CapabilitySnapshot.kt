package com.autodiag.wican.core.capability

/** Granular capability state: vehicle may expose one signal without exposing a related signal. */
enum class CapabilityStatus { SUPPORTED, UNSUPPORTED, UNKNOWN, PARTIAL }

data class CapabilityItem(
    val id: String,
    val status: CapabilityStatus,
    val source: String? = null,
    val verification: String = "unverified",
    val note: String? = null
)

data class VehicleIdentity(
    val vin: String? = null,
    val make: String? = null,
    val model: String? = null,
    val modelYear: Int? = null,
    val market: String? = null,
    val ecuSoftwareVersions: Map<String, String> = emptyMap()
)

data class CapabilityCacheKey(
    val vin: String?,
    val firmwareVersion: String?,
    val softwareFingerprint: String?
)

data class CapabilitySnapshot(
    val identity: VehicleIdentity,
    val cacheKey: CapabilityCacheKey,
    val capabilities: List<CapabilityItem>,
    val observedAtEpochMs: Long
)
