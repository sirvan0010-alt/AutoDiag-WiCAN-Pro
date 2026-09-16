package com.autodiag.core.capability

/**
 * Verification state for Outlander PHEV experimental measurements.
 * Distinct from [VerificationState] so session export stays explicit.
 * UNVERIFIED is the only safe default — never auto-upgrade from static parse.
 */
enum class OutlanderMeasurementVerification {
    UNVERIFIED,
    PARTIALLY_VERIFIED,
    VEHICLE_VERIFIED,
}
