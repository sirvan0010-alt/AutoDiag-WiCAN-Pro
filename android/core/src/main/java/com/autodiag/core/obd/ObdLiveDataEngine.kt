package com.autodiag.core.obd

import com.autodiag.core.diagnostic.DiagnosticEvidenceStore
import com.autodiag.core.diagnostic.DiagnosticEvent
import com.autodiag.core.diagnostic.DiagnosticEventStream
import com.autodiag.core.diagnostic.DiagnosticEventType
import com.autodiag.core.diagnostic.EvidenceSource
import com.autodiag.core.diagnostic.LiveDataEvidenceFactory
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

/**
 * Prioritized, read-only Mode 01 live-data polling engine.
 *
 * Only PIDs discovered as supported and present in the registry are requested.
 * Commands remain serialized by Elm327Session. Poll failures are represented
 * as samples and do not terminate the stream. When supplied, evidenceStore
 * records every poll outcome without replacing earlier observations.
 */
class ObdLiveDataEngine(
    private val session: Elm327Session,
    private val nowEpochMs: () -> Long = { System.currentTimeMillis() },
    private val store: LiveDataStore? = null,
    private val ecuMonitor: EcuConnectionMonitor? = null,
    private val freshnessPolicy: LiveDataFreshnessPolicy = LiveDataFreshnessPolicy(),
    private val eventStream: DiagnosticEventStream? = null,
    private val sessionId: String? = null,
    private val evidenceStore: DiagnosticEvidenceStore? = null,
    private val evidenceVerification: com.autodiag.core.diagnostic.EvidenceVerification = com.autodiag.core.diagnostic.EvidenceVerification.UNVERIFIED,
    private val evidenceEcuId: String? = null,
    private val evidenceSourceId: String? = null,
    private val evidenceSource: EvidenceSource = EvidenceSource.OBD_MODE_01
) {
    data class SensorSample(
        val pid: Int,
        val labelCs: String,
        val value: Double?,
        val unit: String?,
        val rawHex: String,
        val timestampEpochMs: Long,
        val state: State,
        val error: String? = null
    )

    enum class State { LIVE, UNAVAILABLE, ERROR }

    fun stream(supportedPids: Set<Int>, intervalMs: Long = 500L): Flow<SensorSample> = stream(
        supportedPids = supportedPids,
        plans = supportedPids.map(LiveDataPidPolicy::plan),
        fallbackIntervalMs = intervalMs
    )

    fun stream(
        supportedPids: Set<Int>,
        plans: List<LiveDataPollPlan>,
        fallbackIntervalMs: Long = 500L
    ): Flow<SensorSample> = flow {
        require(fallbackIntervalMs >= 0L) { "fallbackIntervalMs must be >= 0" }
        val allowed = supportedPids.filter { it in 0x01..0xE0 && ObdPidRegistry.isSupported(it) }.toSet()
        val activePlans = plans.filter { it.pid in allowed && it.intervalMs >= 0L }
            .distinctBy { it.pid }
            .sortedWith(compareBy<LiveDataPollPlan> { it.priority.ordinal }.thenBy { it.pid })
        if (activePlans.isEmpty()) return@flow
        val now = nowEpochMs()
        val nextDue = activePlans.associate { it.pid to now }.toMutableMap()
        while (currentCoroutineContext().isActive) {
            val current = nowEpochMs()
            var emitted = false
            for (plan in activePlans) {
                if (!currentCoroutineContext().isActive) break
                if (current < (nextDue[plan.pid] ?: current)) continue
                emit(poll(plan.pid))
                emitted = true
                val interval = if (plan.intervalMs == 0L) fallbackIntervalMs else plan.intervalMs
                nextDue[plan.pid] = nowEpochMs() + interval
            }
            if (!emitted) {
                val next = nextDue.values.minOrNull() ?: nowEpochMs()
                delay((next - nowEpochMs()).coerceAtLeast(1L))
            }
        }
    }

    private suspend fun poll(pid: Int): SensorSample {
        val definition = ObdPidRegistry.get(pid) ?: return SensorSample(
            pid, "PID 0x%02X".format(pid), null, null, "", nowEpochMs(), State.UNAVAILABLE
        ).also { recordEvidence(it) }

        return try {
            val response = session.commandDetailed("01${pid.toString(16).padStart(2, '0')}")
            val timestamp = nowEpochMs()
            when (response.kind) {
                Elm327ResponseKind.POSITIVE -> {
                    val parsed = Mode01Decoder.decodeDetailed(response.normalized)
                    if (parsed == null || parsed.pid != pid || parsed.availability != ObdValueAvailability.AVAILABLE) {
                        ecuMonitor?.onFailure()
                        SensorSample(pid, definition.labelCs, null, definition.unit, parsed?.rawHex ?: response.normalized, timestamp, State.UNAVAILABLE, "PID response was not decodable").also { recordEvidence(it) }
                    } else {
                        ecuMonitor?.onSuccess()
                        val sample = LiveDataSample(pid, definition.labelCs, parsed.value, definition.unit, parsed.rawHex, timestamp, LiveDataQuality.GOOD, freshnessPolicy.evaluate(timestamp, timestamp))
                        store?.update(sample)
                        eventStream?.let { stream -> sessionId?.let { id -> stream.append(DiagnosticEvent(DiagnosticEventType.MEASUREMENT_RECEIVED, timestamp, id, definition.labelCs, "obd.mode01.pid.%02X".format(pid), evidenceEcuId)) } }
                        val sensor = SensorSample(pid, definition.labelCs, parsed.value, definition.unit, parsed.rawHex, timestamp, State.LIVE)
                        evidenceStore?.append(LiveDataEvidenceFactory.fromMode01(sample, evidenceVerification, evidenceEcuId, evidenceSourceId, evidenceSource))
                        sensor
                    }
                }
                Elm327ResponseKind.NO_DATA -> {
                    ecuMonitor?.onFailure()
                    SensorSample(pid, definition.labelCs, null, definition.unit, response.normalized, timestamp, State.UNAVAILABLE, "ECU/adapter reported NO DATA").also { recordEvidence(it) }
                }
                Elm327ResponseKind.NEGATIVE -> {
                    ecuMonitor?.onFailure()
                    SensorSample(pid, definition.labelCs, null, definition.unit, response.normalized, timestamp, State.UNAVAILABLE, "Negative diagnostic response").also { recordEvidence(it) }
                }
                Elm327ResponseKind.TIMEOUT, Elm327ResponseKind.ERROR, Elm327ResponseKind.MALFORMED -> {
                    ecuMonitor?.onFailure()
                    SensorSample(pid, definition.labelCs, null, definition.unit, response.normalized, timestamp, State.ERROR, response.error ?: response.kind.name).also { recordEvidence(it) }
                }
            }
        } catch (t: Throwable) {
            ecuMonitor?.onFailure()
            SensorSample(pid, definition.labelCs, null, definition.unit, "", nowEpochMs(), State.ERROR, t.message ?: t::class.simpleName).also { recordEvidence(it) }
        }
    }

    private fun recordEvidence(sensor: SensorSample) {
        val sample = LiveDataSample(
            sensor.pid, sensor.labelCs, sensor.value, sensor.unit, sensor.rawHex, sensor.timestampEpochMs,
            when (sensor.state) { State.LIVE -> LiveDataQuality.GOOD; State.UNAVAILABLE -> LiveDataQuality.UNAVAILABLE; State.ERROR -> LiveDataQuality.ERROR },
            if (sensor.state == State.LIVE) freshnessPolicy.evaluate(sensor.timestampEpochMs, sensor.timestampEpochMs) else LiveDataFreshness.STALE,
            sensor.error
        )
        evidenceStore?.append(LiveDataEvidenceFactory.fromMode01(sample, evidenceVerification, evidenceEcuId, evidenceSourceId, evidenceSource))
    }
}
