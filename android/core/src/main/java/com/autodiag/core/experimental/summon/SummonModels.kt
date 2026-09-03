package com.autodiag.core.experimental.summon

/** Experimental Tesla-like Summon. Isolated WRITE path. No invented CAN IDs. */
enum class SummonExecutionMode {
    DRY_RUN,
    SIMULATOR,
    LIVE_VEHICLE,
}

enum class SummonPhase {
    DISABLED,
    ARMED,
    HOLDING,
    COMPLETE,
    CANCELLED,
    BLOCKED,
    FAULT,
}

enum class SummonBlockReason {
    FEATURE_DISABLED,
    EXPERT_MODE_REQUIRED,
    LIVE_VEHICLE_FORBIDDEN,
    NO_VERIFIED_BINDING,
    VEHICLE_NOT_PARKED,
    SPEED_NOT_ZERO,
    HOLD_RELEASED,
    MISSING_EVIDENCE,
}

data class SummonCommandBinding(
    val vehicleScope: String,
    val protocolNote: String,
    val verification: String,
    val frames: List<String> = emptyList(),
) {
    val isVerifiedForLive: Boolean
        get() = verification == "verified" && frames.isNotEmpty()
}

data class SummonRequest(
    val expertModeEnabled: Boolean,
    val featureToggleEnabled: Boolean,
    val holdActive: Boolean,
    val mode: SummonExecutionMode = SummonExecutionMode.DRY_RUN,
    val parkEvidence: Boolean? = null,
    val speedKmh: Double? = null,
    val binding: SummonCommandBinding? = null,
)

data class SummonTick(
    val phase: SummonPhase,
    val mode: SummonExecutionMode,
    val blockReason: SummonBlockReason? = null,
    val wouldTransmit: Boolean,
    val messageCs: String,
    val audit: String,
)
