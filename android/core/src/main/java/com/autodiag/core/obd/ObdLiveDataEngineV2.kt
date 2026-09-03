package com.autodiag.core.obd

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

/** Prioritized, read-only Mode 01 polling engine. */
class ObdLiveDataEngineV2(
    private val session: Elm327Session,
    private val nowEpochMs: () -> Long = { System.currentTimeMillis() }
) {
    fun stream(supportedPids: Set<Int>, plans: List<LiveDataPollPlan>): Flow<ObdLiveDataEngine.SensorSample> = flow {
        val allowed = supportedPids.filter { it in 0x01..0xE0 && ObdPidRegistry.isSupported(it) }.toSet()
        val active = plans.filter { it.pid in allowed && it.intervalMs >= 0L }
            .distinctBy { it.pid }
            .sortedWith(compareBy<LiveDataPollPlan> { it.priority.ordinal }.thenBy { it.pid })
        if (active.isEmpty()) return@flow
        val nextDue = active.associate { it.pid to nowEpochMs() }.toMutableMap()
        while (currentCoroutineContext().isActive) {
            val now = nowEpochMs()
            var emitted = false
            for (plan in active) {
                if (!currentCoroutineContext().isActive) break
                if (now < (nextDue[plan.pid] ?: now)) continue
                emit(poll(plan.pid))
                emitted = true
                nextDue[plan.pid] = nowEpochMs() + plan.intervalMs
            }
            if (!emitted) delay(((nextDue.values.minOrNull() ?: now) - nowEpochMs()).coerceAtLeast(1L))
        }
    }

    private suspend fun poll(pid: Int): ObdLiveDataEngine.SensorSample {
        val def = ObdPidRegistry.get(pid)
            ?: return ObdLiveDataEngine.SensorSample(pid, "PID 0x%02X".format(pid), null, null, "", nowEpochMs(), ObdLiveDataEngine.State.UNAVAILABLE)
        return try {
            val response = session.command("01${pid.toString(16).padStart(2, '0')}")
            val timestamp = nowEpochMs()
            val parsed = Mode01Decoder.decodeDetailed(response)
            if (parsed == null || parsed.pid != pid || parsed.availability != ObdValueAvailability.AVAILABLE) {
                ObdLiveDataEngine.SensorSample(pid, def.labelCs, null, def.unit, parsed?.rawHex.orEmpty(), timestamp, ObdLiveDataEngine.State.UNAVAILABLE)
            } else {
                ObdLiveDataEngine.SensorSample(pid, def.labelCs, parsed.value, def.unit, parsed.rawHex, timestamp, ObdLiveDataEngine.State.LIVE)
            }
        } catch (t: Throwable) {
            ObdLiveDataEngine.SensorSample(pid, def.labelCs, null, def.unit, "", nowEpochMs(), ObdLiveDataEngine.State.ERROR, t.message ?: t::class.simpleName)
        }
    }
}
