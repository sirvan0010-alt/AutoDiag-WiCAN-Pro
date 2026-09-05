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
    private val diagnosticData: DiagnosticDataProvider = GitHubDiagnosticDataProvider(),
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

                        fun resolve(signalId: String): OutlanderPhevDecoderResolver.Resolution =
                            OutlanderPhevDecoderResolver.resolve(candidates, signalId)

                        fun verification(definition: SignalDecoderDefinition): OutlanderMeasurementVerification =
                            when (definition.verification) {
                                VerificationState.VERIFIED -> OutlanderMeasurementVerification.VERIFIED
                                VerificationState.PARTIALLY_VERIFIED -> OutlanderMeasurementVerification.PARTIALLY_VERIFIED
                                VerificationState.UNVERIFIED -> OutlanderMeasurementVerification.UNVERIFIED
                            }

                        fun definitionOrNull(signalId: String): SignalDecoderDefinition? =
                            when (val result = resolve(signalId)) {
                                is OutlanderPhevDecoderResolver.Resolution.Resolved -> result.definition
                                OutlanderPhevDecoderResolver.Resolution.NotFound -> null
                                is OutlanderPhevDecoderResolver.Resolution.Ambiguous -> null
                            }

                        fun ambiguity(signalId: String): String? =
                            when (val result = resolve(signalId)) {
                                is OutlanderPhevDecoderResolver.Resolution.Ambiguous ->
                                    "Ambiguous decoder variant for $REQUEST/$signalId: ${result.variantIds.joinToString()}; vehicle/ECU variant evidence required"
                                else -> null
                            }

                        val isolationDefinition = definitionOrNull("battery.isolation_resistance")
                        val maxDefinition = definitionOrNull("battery.internal_resistance.max")
                        val minDefinition = definitionOrNull("battery.internal_resistance.min")
                        val ambiguities = listOfNotNull(
                            ambiguity("battery.isolation_resistance"),
                            ambiguity("battery.internal_resistance.max"),
                            ambiguity("battery.internal_resistance.min")
                        )

                        fun decode(definition: SignalDecoderDefinition?): OutlanderResistanceMeasurement? = definition?.let {
                            OutlanderPhevResistanceDecoder.decodeMeasurement(
                                definition = it,
                                response = parsed,
                                timestampEpochMs = startedAt,
                                rawRequest = REQUEST,
                                rawResponse = response.raw,
                                verification = verification(it)
                            )
                        }

                        Result(
                            timestampEpochMs = startedAt,
                            rawRequest = REQUEST,
                            rawResponse = response.raw,
                            isolationResistance = decode(isolationDefinition),
                            internalResistanceMax = decode(maxDefinition),
                            internalResistanceMin = decode(minDefinition),
                            adapterStatus = response.kind,
                            error = when {
                                candidates.isEmpty() -> "No diagnostic-data decoder candidate for $REQUEST"
                                ambiguities.isNotEmpty() -> ambiguities.joinToString("; ")
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
