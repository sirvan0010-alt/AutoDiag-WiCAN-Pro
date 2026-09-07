package com.autodiag.core.live

import com.autodiag.core.automation.AutomationNotification
import com.autodiag.core.automation.AutomationSession
import com.autodiag.core.automation.ReplaySample
import com.autodiag.core.obd.LiveDataSample

/**
 * Read-only boundary between decoded OBD measurements and generic automation.
 *
 * This deliberately performs no vehicle command dispatch. It only promotes
 * evidence-bounded standard OBD samples to semantic signals and feeds them
 * into the deterministic AutomationSession.
 */
object ObdAutomationBridge {
    fun process(
        sample: LiveDataSample,
        session: AutomationSession
    ): List<AutomationNotification> {
        val signal = ObdSemanticSignalAdapter.toSemanticSignal(sample) ?: return emptyList()
        return session.process(
            ReplaySample(
                timestampMs = sample.timestampEpochMs,
                signals = listOf(signal)
            )
        )
    }

    fun process(
        samples: Iterable<LiveDataSample>,
        session: AutomationSession
    ): List<AutomationNotification> = samples
        .sortedBy { it.timestampEpochMs }
        .flatMap { process(it, session) }
}
