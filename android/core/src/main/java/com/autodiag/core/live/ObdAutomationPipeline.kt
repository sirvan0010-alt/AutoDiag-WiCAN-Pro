package com.autodiag.core.live

import com.autodiag.core.automation.AutomationNotification
import com.autodiag.core.automation.AutomationSession
import com.autodiag.core.obd.LiveDataFreshness
import com.autodiag.core.obd.LiveDataQuality
import com.autodiag.core.obd.LiveDataSample
import com.autodiag.core.obd.ObdLiveDataEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

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

    fun processStream(samples: Flow<ObdLiveDataEngine.SensorSample>): Flow<AutomationNotification> = flow {
        samples.collect { sensor ->
            if (sensor.state != ObdLiveDataEngine.State.LIVE) return@collect
            process(
                LiveDataSample(
                    pid = sensor.pid,
                    labelCs = sensor.labelCs,
                    value = sensor.value,
                    unit = sensor.unit,
                    rawHex = sensor.rawHex,
                    timestampEpochMs = sensor.timestampEpochMs,
                    quality = LiveDataQuality.GOOD,
                    freshness = LiveDataFreshness.FRESH,
                    error = sensor.error
                )
            ).forEach { emit(it) }
        }
    }
}
