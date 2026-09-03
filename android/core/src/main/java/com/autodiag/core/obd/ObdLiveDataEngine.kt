package com.autodiag.core.obd

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

/**
 * Read-only Mode 01 live-data polling engine.
 *
 * Only PIDs discovered as supported are requested. One ELM command is sent at
 * a time (Elm327Session already serializes commands), and a failed PID does
 * not terminate the whole live-data stream.
 */
class ObdLiveDataEngine(
    private val session: Elm327Session,
    private val nowEpochMs: () -> Long = { System.currentTimeMillis() }
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

    /**
     * Polls the supplied supported PID set forever until the collector is
     * cancelled. [intervalMs] is the minimum delay between complete polling
     * rounds, not between individual commands.
     */
    fun stream(
        supportedPids: Set<Int>,
        intervalMs: Long = 500L
    ): Flow<SensorSample> = flow {
        require(intervalMs >= 0) { "intervalMs must be >= 0" }

        val pids = supportedPids
            .filter { it in 0x01..0xE0 && ObdPidRegistry.isSupported(it) }
            .sorted()

        if (pids.isEmpty()) return@flow

        while (currentCoroutineContext().isActive) {
            val roundStarted = nowEpochMs()

            for (pid in pids) {
                if (!currentCoroutineContext().isActive) break

                val definition = ObdPidRegistry.get(pid) ?: continue
                val timestamp = nowEpochMs()

                try {
                    val response = session.command(
                        "01${pid.toString(16).padStart(2, '0')}"
                    )
                    val parsed = ObdResponseParser.parseMode01(response)
                        .firstOrNull { it.pid == pid }

                    if (parsed == null || parsed.data.size < definition.minimumBytes) {
                        emit(
                            SensorSample(
                                pid = pid,
                                labelCs = definition.labelCs,
                                value = null,
                                unit = definition.unit,
                                rawHex = parsed?.data?.toHex() ?: "",
                                timestampEpochMs = timestamp,
                                state = State.UNAVAILABLE
                            )
                        )
                    } else {
                        val bytes = parsed.data.map { it.toInt() and 0xFF }
                        val value = definition.decodeValue(bytes)
                        emit(
                            SensorSample(
                                pid = pid,
                                labelCs = definition.labelCs,
                                value = value,
                                unit = definition.unit,
                                rawHex = parsed.data.toHex(),
                                timestampEpochMs = timestamp,
                                state = if (value != null) State.LIVE else State.UNAVAILABLE
                            )
                        )
                    }
                } catch (t: Throwable) {
                    emit(
                        SensorSample(
                            pid = pid,
                            labelCs = definition.labelCs,
                            value = null,
                            unit = definition.unit,
                            rawHex = "",
                            timestampEpochMs = timestamp,
                            state = State.ERROR,
                            error = t.message ?: t::class.simpleName
                        )
                    )
                }
            }

            val elapsed = nowEpochMs() - roundStarted
            val remaining = intervalMs - elapsed
            if (remaining > 0) delay(remaining)
        }
    }

    private fun ByteArray.toHex(): String = joinToString(" ") {
        "%02X".format(it.toInt() and 0xFF)
    }
}
