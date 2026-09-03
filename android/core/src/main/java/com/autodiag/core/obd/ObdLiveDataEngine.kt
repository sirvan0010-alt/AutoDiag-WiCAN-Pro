package com.autodiag.core.obd

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
 * as samples and do not terminate the stream.
 */
class ObdLiveDataEngine(
    private val session: Elm327Session,
    private val nowEpochMs: () -> Long = { System.currentTimeMillis() },
    private val store: LiveDataStore? = null,
    private val ecuMonitor: EcuConnectionMonitor? = null,
    private val freshnessPolicy: LiveDataFreshnessPolicy = LiveDataFreshnessPolicy()
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

    enum class State {
        LIVE,
        UNAVAILABLE,
        ERROR
    }

    fun stream(
        supportedPids: Set<Int>,
        intervalMs: Long = 500L
    ): Flow<SensorSample> = stream(
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

        val allowed = supportedPids
            .filter { it in 0x01..0xE0 && ObdPidRegistry.isSupported(it) }
            .toSet()

        val activePlans = plans
            .filter { it.pid in allowed && it.intervalMs >= 0L }
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
        val definition = ObdPidRegistry.get(pid)
            ?: return SensorSample(
                pid = pid,
                labelCs = "PID 0x%02X".format(pid),
                value = null,
                unit = null,
                rawHex = "",
                timestampEpochMs = nowEpochMs(),
                state = State.UNAVAILABLE
            )

        return try {
            val response = session.command("01${pid.toString(16).padStart(2, '0')}")
            val timestamp = nowEpochMs()
            val parsed = Mode01Decoder.decodeDetailed(response)

            if (parsed == null || parsed.pid != pid ||
                parsed.availability != ObdValueAvailability.AVAILABLE) {
                ecuMonitor?.onFailure()
                SensorSample(
                    pid = pid,
                    labelCs = definition.labelCs,
                    value = null,
                    unit = definition.unit,
                    rawHex = parsed?.rawHex.orEmpty(),
                    timestampEpochMs = timestamp,
                    state = State.UNAVAILABLE
                )
            } else {
                ecuMonitor?.onSuccess()
                store?.update(
                    LiveDataSample(
                        pid = pid,
                        labelCs = definition.labelCs,
                        value = parsed.value,
                        unit = definition.unit,
                        rawHex = parsed.rawHex,
                        timestampEpochMs = timestamp,
                        quality = LiveDataQuality.GOOD,
                        freshness = freshnessPolicy.evaluate(timestamp, timestamp)
                    )
                )
                SensorSample(
                    pid = pid,
                    labelCs = definition.labelCs,
                    value = parsed.value,
                    unit = definition.unit,
                    rawHex = parsed.rawHex,
                    timestampEpochMs = timestamp,
                    state = State.LIVE
                )
            }
        } catch (t: Throwable) {
            ecuMonitor?.onFailure()
            SensorSample(
                pid = pid,
                labelCs = definition.labelCs,
                value = null,
                unit = definition.unit,
                rawHex = "",
                timestampEpochMs = nowEpochMs(),
                state = State.ERROR,
                error = t.message ?: t::class.simpleName
            )
        }
    }
}
