package com.autodiag.core.capability

import com.autodiag.core.obd.Elm327ResponseKind
import com.autodiag.core.obd.Elm327Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Explicitly started, read-only polling of the source-derived Outlander PHEV 21 01 block. */
class OutlanderPhev21LiveMeasurementRunner(
    private val session: Elm327Session,
    private val scope: CoroutineScope,
    private val onResult: (Result) -> Unit,
    private val diagnosticData: DiagnosticDataProvider = EmptyDiagnosticDataProvider,
    private val variantId: String? = null
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
        require(intervalMs in OutlanderLiveSamplingSettings.OPTIONS_MS) { "Unsupported Outlander sampling interval: $intervalMs ms" }
        stop()
        job = scope.launch {
            while (isActive) {
                val startedAt = System.currentTimeMillis()
                val response = session.commandDetailed(REQUEST)
                if (response.kind == Elm327ResponseKind.POSITIVE) {
                    val decoded = runCatching {
                        val parsed = OutlanderPhev21ResponseParser.parse(response.normalized)
                        val candidates = diagnosticData.findDecoderCandidates(REQUEST, variantId)

                        fun unique(signalId: String): SignalDecoderDefinition? =
                            candidates.filter { it.signalId == signalId }.singleOrNull()

                        val isolationDefinition = unique("battery.isolation_resistance")
                        val maxDefinition = unique("battery.internal_resistance.max")
                        val minDefinition = unique("battery.internal_resistance.min")

                        val isolation = isolationDefinition?.let {
                            runCatching {
                                OutlanderPhevResistanceDecoder.decodeMeasurement(
                                    definition = it,
                                    response = parsed,
                                    timestampEpochMs = startedAt,
                                    rawRequest = REQUEST,
                                    rawResponse = response.raw,
                                    verification = OutlanderMeasurementVerification.PARTIALLY_VERIFIED
                                )
                            }.getOrNull()
                        }
                        val max = maxDefinition?.let {
                            runCatching {
                                OutlanderPhevResistanceDecoder.decodeMeasurement(
                                    definition = it,
                                    response = parsed,
                                    timestampEpochMs = startedAt,
                                    rawRequest = REQUEST,
                                    rawResponse = response.raw,
                                    verification = OutlanderMeasurementVerification.UNVERIFIED
                                )
                            }.getOrNull()
                        }
                        val min = minDefinition?.let {
                            runCatching {
                                OutlanderPhevResistanceDecoder.decodeMeasurement(
                                    definition = it,
                                    response = parsed,
                                    timestampEpochMs = startedAt,
                                    rawRequest = REQUEST,
                                    rawResponse = response.raw,
                                    verification = OutlanderMeasurementVerification.UNVERIFIED
                                )
                            }.getOrNull()
                        }

                        val ambiguity = listOf(
                            "battery.isolation_resistance" to isolationDefinition,
                            "battery.internal_resistance.max" to maxDefinition,
                            "battery.internal_resistance.min" to minDefinition
                        ).filter { (signal, definition) ->
                            definition == null && candidates.any { it.signalId == signal }
                        }.map { it.first }

                        Result(
                            timestampEpochMs = startedAt,
                            rawRequest = REQUEST,
                            rawResponse = response.raw,
                            isolationResistance = isolation,
                            internalResistanceMax = max,
                            internalResistanceMin = min,
                            adapterStatus = response.kind,
                            error = when {
                                candidates.isEmpty() -> "No diagnostic-data decoder candidate for $REQUEST"
                                ambiguity.isNotEmpty() -> "Ambiguous decoder variant for $REQUEST: ${ambiguity.joinToString()}; vehicle/ECU variant evidence required"
                                else -> null
                            }
                        )
                    }
                    onResult(decoded.getOrElse { error ->
                        Result(startedAt, REQUEST, response.raw, adapterStatus = response.kind, error = error.message ?: "Outlander 21 01 decode failed")
                    })
                } else {
                    onResult(Result(startedAt, REQUEST, response.raw.ifBlank { null }, adapterStatus = response.kind, error = response.error ?: "21 01 returned ${response.kind}"))
                }
                delay(intervalMs)
            }
        }
    }

    fun stop() { job?.cancel(); job = null }
    companion object { const val REQUEST = "21 01" }
}
