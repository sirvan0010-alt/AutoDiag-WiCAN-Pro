package com.autodiag.simulator

import com.autodiag.core.transport.ConnectionState
import com.autodiag.core.transport.TransportConfig
import com.autodiag.core.transport.TransportMetrics
import com.autodiag.core.transport.WiCanTransport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update

/** No-vehicle transport for UI and protocol development. Never writes to a real CAN bus. */
class SimulatorTransport : WiCanTransport {
    override val name = "Simulator"
    private val _state = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val state: ConnectionState get() = _state.value
    private val _metrics = MutableStateFlow(TransportMetrics())
    override val metrics: StateFlow<TransportMetrics> = _metrics
    private val incoming = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)

    override suspend fun connect(config: TransportConfig): Result<Unit> {
        _state.value = ConnectionState.CONNECTING
        _state.value = ConnectionState.CONNECTED
        _metrics.value = TransportMetrics(connectedAtMs = System.currentTimeMillis())
        return Result.success(Unit)
    }

    override suspend fun disconnect() {
        _state.value = ConnectionState.DISCONNECTED
        _metrics.value = TransportMetrics()
    }

    override suspend fun send(data: ByteArray): Result<Unit> {
        if (_state.value != ConnectionState.CONNECTED) {
            return Result.failure(IllegalStateException("Simulator není připojen."))
        }
        val response = "OK\r>".toByteArray()
        incoming.tryEmit(response)
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
        return Result.success(Unit)
    }

    override fun observeIncoming(): Flow<ByteArray> = incoming.asSharedFlow()
}
