package com.autodiag.core.experimental.summon

/** Fail-closed gate. LIVE_VEHICLE is always rejected in this scaffold. */
object SummonSafetyGate {
    fun evaluate(request: SummonRequest): SummonTick {
        val mode = request.mode
        if (!request.featureToggleEnabled) {
            return blocked(mode, SummonBlockReason.FEATURE_DISABLED, "Summon je vypnutý (experimental toggle).")
        }
        if (!request.expertModeEnabled) {
            return blocked(mode, SummonBlockReason.EXPERT_MODE_REQUIRED, "Vyžaduje Expert režim.")
        }
        if (mode == SummonExecutionMode.LIVE_VEHICLE) {
            return blocked(mode, SummonBlockReason.LIVE_VEHICLE_FORBIDDEN, "LIVE přenos je v této verzi zakázán.")
        }
        if (request.parkEvidence != true) {
            return blocked(mode, SummonBlockReason.VEHICLE_NOT_PARKED, "Chybí evidence PARK.")
        }
        val speed = request.speedKmh
        if (speed == null) {
            return blocked(mode, SummonBlockReason.MISSING_EVIDENCE, "Rychlost není změřená — UNAVAILABLE ≠ 0.")
        }
        if (speed > 0.1) {
            return blocked(mode, SummonBlockReason.SPEED_NOT_ZERO, "Rychlost není 0 km/h.")
        }
        if (!request.holdActive) {
            return SummonTick(
                phase = SummonPhase.CANCELLED,
                mode = mode,
                blockReason = SummonBlockReason.HOLD_RELEASED,
                wouldTransmit = false,
                messageCs = "Držení uvolněno — fail-safe stop.",
                audit = "summon cancelled hold_released",
            )
        }
        return SummonTick(
            phase = SummonPhase.HOLDING,
            mode = mode,
            blockReason = null,
            wouldTransmit = false,
            messageCs = when (mode) {
                SummonExecutionMode.DRY_RUN -> "DRY-RUN: žádné CAN rámce se neodesílají."
                SummonExecutionMode.SIMULATOR -> "SIMULATOR: fiktivní summon tick."
                SummonExecutionMode.LIVE_VEHICLE -> "LIVE blokováno."
            },
            audit = "summon holding mode=$mode transmit=false",
        )
    }

    private fun blocked(mode: SummonExecutionMode, reason: SummonBlockReason, msg: String) =
        SummonTick(
            phase = SummonPhase.BLOCKED,
            mode = mode,
            blockReason = reason,
            wouldTransmit = false,
            messageCs = msg,
            audit = "summon blocked $reason",
        )
}
