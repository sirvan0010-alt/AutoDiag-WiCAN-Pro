package com.autodiag.core.transport

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update

/**
 * Deterministic in-process ELM327-like transport for UI and CI tests.
 * No sockets and no real vehicle data. Simulator responses are explicitly synthetic.
 */
class SimulatorWiCanTransport : WiCanTransport {
    override val name: String = "Simulator"

    private val _state = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val state: ConnectionState get() = _state.value

    private val _metrics = MutableStateFlow(TransportMetrics())
    override val metrics: StateFlow<TransportMetrics> = _metrics

    private val stream = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)

    override suspend fun connect(config: TransportConfig): Result<Unit> {
        _state.value = ConnectionState.CONNECTED
        _metrics.value = TransportMetrics(connectedAtMs = System.currentTimeMillis())
        return Result.success(Unit)
    }

    override suspend fun disconnect() {
        _state.value = ConnectionState.DISCONNECTED
        _metrics.value = TransportMetrics()
    }

    override suspend fun send(data: ByteArray): Result<Unit> = runCatching {
        val cmd = data.toString(Charsets.US_ASCII)
            .trim()
            .trimEnd('\r', '\n')
            .uppercase()
            .replace("\\s+".toRegex(), "")
        val body = responseFor(cmd)
        val response = (body + "\r\n>").toByteArray(Charsets.US_ASCII)
        stream.emit(response)
        val now = System.currentTimeMillis()
        _metrics.update {
            it.copy(
                txChunks = it.txChunks + 1,
                txBytes = it.txBytes + data.size,
                rxChunks = it.rxChunks + 1,
                rxBytes = it.rxBytes + response.size,
                lastTxAtMs = now,
                lastRxAtMs = now
            )
        }
    }

    override fun observeIncoming(): Flow<ByteArray> = stream.asSharedFlow()

    private fun responseFor(cmd: String): String = when {
        cmd == "ATZ" || cmd == "ATD" -> "ELM327 v1.5"
        cmd == "ATE0" || cmd == "ATL0" || cmd == "ATH1" || cmd == "ATSP0" -> "OK"
        cmd == "ATI" -> "ELM327 v1.5"
        cmd == "ATRV" -> "12.6V"
        cmd == "ATDP" -> "AUTO, ISO 15765-4 (CAN 11/500)"
        cmd == "ATDPN" -> "A6"
        cmd == "0902" -> "49 02 01 SIMTEST0AUTODIAG01"
        cmd == "03" -> "43 00"
        cmd == "010C" -> "41 0C 00 00"
        cmd.startsWith("AT") -> "OK"
        else -> "NO DATA"
    }
}
