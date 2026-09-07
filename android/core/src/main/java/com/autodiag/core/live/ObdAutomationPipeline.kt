package com.autodiag.core.live

import com.autodiag.core.automation.AutomationNotification
import com.autodiag.core.automation.AutomationSession
import com.autodiag.core.obd.LiveDataSample
import com.autodiag.core.obd.ObdLiveDataEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Read-only runtime pipeline from decoded OBD live data to automation.
 *
 * The OBD engine remains responsible for transport, polling and decoding.
 * This layer only converts LIVE samples to semantic signals and evaluates the
 * configured AutomationSession. No vehicle write/command API is exposed here.
 */
class ObdAutomationPipeline(
    private val session: AutomationSession
) {
    fun process(sample: LiveDataSample): List<AutomationNotification> =
        ObdAutomationBridge.process(sample, session)

    fun processStream(samples: Flow<ObdLiveDataEngine.SensorSample>): Flow<AutomationNotification> =
        samples.mapNotNullToNotifications { sensor ->
            if (sensor.state != ObdLiveDataEngine.State.LIVE) return@mapNotNullToNotifications null
            process(
                LiveDataSample(
                    pid = sensor.pid,
                    labelCs = sensor.labelCs,
                    value = sensor.value,
                    unit = sensor.unit,
                    rawHex = sensor.rawHex,
                    timestampEpochMs = sensor.timestampEpochMs,
                    quality = com.autodiag.core.obd.LiveDataQuality.GOOD,
                    freshness = com.autodiag.core.obd.LiveDataFreshness.FRESH,
                    error = sensor.error
                )
            )
        }
}

private fun <T, R> Flow<T>.mapNotNullToNotifications(
    transform: suspend (T) -> List<AutomationNotification>?
): Flow<AutomationNotification> = kotlinx.coroutines.flow.flow {
    collect { value ->
        transform(value)?.forEach { emit(it) }
    }
}
