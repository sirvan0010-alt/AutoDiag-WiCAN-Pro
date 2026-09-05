package com.autodiag.wican.core.diagnostics

import kotlin.math.abs

enum class BatteryHealthPhase { STATIC, LOAD, RECOVERY, TREND, CONFIDENCE }
enum class BatteryHealthResult { PASS, ATTENTION, NOT_AVAILABLE, INSUFFICIENT_EVIDENCE }

data class BatteryHealthSample(
    val timestampMs: Long,
    val socPercent: Double? = null,
    val packVoltageV: Double? = null,
    val packCurrentA: Double? = null,
    val minCellV: Double? = null,
    val maxCellV: Double? = null,
    val minTemperatureC: Double? = null,
    val maxTemperatureC: Double? = null,
    val phase: BatteryHealthPhase
)

data class BatteryHealthAssessment(
    val result: BatteryHealthResult,
    val confidence: Double,
    val cellSpreadMv: Double? = null,
    val recoveryDeltaMv: Double? = null,
    val sampleCount: Int,
    val notes: List<String> = emptyList()
)

/** Evidence-first analysis. No universal cell-voltage limit is embedded here. */
object EvBatteryHealthAnalyzer {
    fun assess(samples: List<BatteryHealthSample>): BatteryHealthAssessment {
        if (samples.isEmpty()) return BatteryHealthAssessment(BatteryHealthResult.NOT_AVAILABLE, 0.0, sampleCount = 0)
        val spreads = samples.mapNotNull { s ->
            if (s.minCellV != null && s.maxCellV != null) (s.maxCellV - s.minCellV) * 1000.0 else null
        }
        val spread = spreads.lastOrNull()
        val recovery = recoveryDelta(samples)
        val phases = samples.map { it.phase }.toSet()
        val evidence = (phases.size / BatteryHealthPhase.entries.size.toDouble()).coerceIn(0.0, 1.0)
        val confidence = (0.25 + 0.75 * evidence).coerceIn(0.0, 1.0)
        val notes = buildList {
            if (spread == null) add("Cell spread není dostupný z měření.")
            if (recovery == null) add("Recovery fáze nemá dostatek párovatelných vzorků.")
            add("Hodnocení používá pouze dodaná měření; univerzální mV limit se nepoužívá.")
        }
        return BatteryHealthAssessment(
            result = if (confidence < 0.5) BatteryHealthResult.INSUFFICIENT_EVIDENCE else BatteryHealthResult.ATTENTION,
            confidence = confidence,
            cellSpreadMv = spread,
            recoveryDeltaMv = recovery,
            sampleCount = samples.size,
            notes = notes
        )
    }

    private fun recoveryDelta(samples: List<BatteryHealthSample>): Double? {
        val load = samples.lastOrNull { it.phase == BatteryHealthPhase.LOAD } ?: return null
        val recovery = samples.asSequence().filter { it.phase == BatteryHealthPhase.RECOVERY }.lastOrNull() ?: return null
        if (load.minCellV == null || recovery.minCellV == null) return null
        return abs(recovery.minCellV - load.minCellV) * 1000.0
    }
}
