package com.autodiag.core.automation

/** A recorded sample for deterministic rule replay. */
data class ReplaySample(
    val timestampMs: Long,
    val signals: List<SemanticSignal>
)

data class ReplayEvent(
    val timestampMs: Long,
    val evaluation: AutomationEvaluation
)

/** Replay is simulation-only and cannot dispatch vehicle commands. */
object AutomationReplay {
    fun run(rule: AutomationRule, samples: List<ReplaySample>): List<ReplayEvent> {
        val detector = AutomationEdgeDetector()
        return samples.sortedBy { it.timestampMs }.map { sample ->
            val signals = sample.signals.map { it.copy(timestampMs = sample.timestampMs) }
            ReplayEvent(sample.timestampMs, detector.evaluate(rule, signals))
        }
    }
}
