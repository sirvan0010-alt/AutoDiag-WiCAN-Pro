package com.autodiag.core.obd

/**
 * Metadata and decode formula for a single standard SAE J1979 Mode 01 PID.
 *
 * This is intentionally limited to publicly documented, standardized OBD-II PIDs.
 * It never encodes manufacturer-specific or reverse-engineered signals — those
 * belong to vehicle profiles (Tesla/VAG/...), not this generic registry.
 */
data class ObdPidDefinition(
    val pid: Int,
    val minimumBytes: Int,
    val unit: String?,
    val labelCs: String,
    val decodeValue: (List<Int>) -> Double?
)

enum class ObdValueAvailability { AVAILABLE, UNAVAILABLE, UNKNOWN_PID }

data class ObdPidResult(
    val mode: Int,
    val pid: Int,
    val rawHex: String,
    val value: Double?,
    val unit: String?,
    val labelCs: String,
    val availability: ObdValueAvailability,
    val definition: ObdPidDefinition?
)
