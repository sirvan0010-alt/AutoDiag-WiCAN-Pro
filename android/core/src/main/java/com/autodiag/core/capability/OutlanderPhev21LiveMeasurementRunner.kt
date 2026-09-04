package com.autodiag.core.capability

import com.autodiag.core.obd.Elm327ResponseKind
import com.autodiag.core.obd.Elm327Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Explicitly started, read-only polling of the source-derived Outlander PHEV
 * 21 01 block.
 *
 * This runner deliberately does not configure or invent CAN request/response
 * identifiers. The connected adapter/session must already be usable for the
 * target vehicle. A failed/unsupported 21 01 exchange is reported to the
 * callback and is never converted into a zero or "unavailable" value.
 */
class OutlanderPhev21LiveMeasurementRunner(
    private val session: Elm327Session,
    private val scope: CoroutineScope,
    private val onResult: (Result) -> Unit
) {
    data class Result(
        val timestampEpochMs: Long,
        val rawRequest: String = REQUEST,
        val rawResponse: String? = null,
        val isolationResistance: OutlanderResistanceMeasurement? = null,
        val internalResistanceMax: OutlanderResistanceMeasurement? = null,
        val internalResistanceMin: OutlanderResistanceMeasurement? = null,
        val adapterStatus: Elm327ResponseKind,
        val error: String? = null
    )

    private var job: Job? = null

    val isRunning: Boolean get() = job?.isActive == true

    fun start(intervalMs: Long) {
        require(intervalMs in OutlanderLiveSamplingSettings.OPTIONS_MS) {
            "Unsupported Outlander sampling interval: $intervalMs ms"
        }
        stop()
        job = scope.launch {
            while (isActive) {
                val startedAt = System.currentTimeMillis()
                val response = session.commandDetailed(REQUEST)
                if (response.kind == Elm327ResponseKind.POSITIVE) {
                    val decoded = runCatching {
                        val parsed = OutlanderPhev21ResponseParser.parse(response.normalized)
                        val iso = OutlanderPhevResistanceDecoder.decodeIsolationMeasurement(
                            response = parsed,
                            timestampEpochMs = startedAt,
                            rawRequest = REQUEST,
                            rawResponse = response.raw
                        )
                        val max = OutlanderResistanceMeasurement(
                            kind = OutlanderResistanceKind.INTERNAL_RESISTANCE_MAX_UNVERIFIED,
                            value = OutlanderPhevResistanceDecoder.decodeUnverifiedInternalResistanceMaximum(parsed),
                            timestampEpochMs = startedAt,
                            rawRequest = REQUEST,
                            rawResponse = response.raw,
                            verification = OutlanderMeasurementVerification.UNVERIFIED
                        )
                        val min = OutlanderResistanceMeasurement(
                            kind = OutlanderResistanceKind.INTERNAL_RESISTANCE_MIN_UNVERIFIED,
                            value = OutlanderPhevResistanceDecoder.decodeUnverifiedInternalResistanceMinimum(parsed),
                            timestampEpochMs = startedAt,
                            rawRequest = REQUEST,
                            rawResponse = response.raw,
                            verification = OutlanderMeasurementVerification.UNVERIFIED
                        )
                        Result(startedAt, response.raw, iso, max, min, response.kind)
                    }
                    onResult(
                        decoded.getOrElse {
                            Result(
                                timestampEpochMs = startedAt,
                                rawResponse = response.raw,
                                adapterStatus = response.kind,
                                error = it.message ?: "Outlander 21 01 decode failed"
                            )
                        }
                    )
                } else {
                    onResult(
                        Result(
                            timestampEpochMs = startedAt,
                            rawResponse = response.raw.ifBlank { null },
                            adapterStatus = response.kind,
                            error = response.error ?: "21 01 returned ${response.kind}"
                        )
                    )
                }
                delay(intervalMs)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    companion object {
        const val REQUEST = "21 01"
    }
}
