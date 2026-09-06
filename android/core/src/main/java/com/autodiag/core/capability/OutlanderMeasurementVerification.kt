package com.autodiag.core.capability

/**
 * Verification state for Outlander PHEV measurements.
 *
 * The values deliberately mirror the repository evidence lifecycle. Static
 * extraction alone remains UNVERIFIED until vehicle/capture evidence supports
 * promotion.
 */
enum class OutlanderMeasurementVerification {
    UNVERIFIED,
    PARTIALLY_VERIFIED,
    VERIFIED,
}
