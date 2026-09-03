package com.autodiag.core.experimental.summon

/** Session controller. Never writes to WiCAN / SLCAN. */
class SummonController(
    private val gate: (SummonRequest) -> SummonTick = SummonSafetyGate::evaluate,
) {
    private val auditLog = mutableListOf<String>()
    var phase: SummonPhase = SummonPhase.DISABLED
        private set

    fun audit(): List<String> = auditLog.toList()

    fun tick(request: SummonRequest): SummonTick {
        val result = gate(request)
        phase = result.phase
        auditLog += result.audit
        check(!result.wouldTransmit) { "Summon must never set wouldTransmit in this scaffold" }
        return result
    }

    fun reset() {
        phase = SummonPhase.DISABLED
        auditLog += "summon reset"
    }
}
