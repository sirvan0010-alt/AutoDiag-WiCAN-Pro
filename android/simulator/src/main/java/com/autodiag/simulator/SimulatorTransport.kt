package com.autodiag.simulator

import com.autodiag.core.transport.ConnectionState
import com.autodiag.core.transport.TransportConfig
import com.autodiag.core.transport.WiCanTransport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** No-vehicle transport for UI and protocol development. Never writes to a real CAN bus. */
class SimulatorTransport : WiCanTransport {
    override val name = "Simulator"
    private var _state = ConnectionState.DISCONNECTED
    override val state: ConnectionState get() = _state
    private val incoming = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)

    override suspend fun connect(config: TransportConfig): Result<Unit> {
        _state = ConnectionState.CONNECTING
        _state = ConnectionState.CONNECTED
        return Result.success(Unit)
    }

    override suspend fun disconnect() { _state = ConnectionState.DISCONNECTED }

    override suspend fun send(data: ByteArray): Result<Unit> {
        if (_state != ConnectionState.CONNECTED) {
            return Result.failure(IllegalStateException("Simulator není připojen."))
        }
        incoming.tryEmit("OK\r".toByteArray())
        return Result.success(Unit)
    }

    override fun observeIncoming(): Flow<ByteArray> = incoming.asSharedFlow()
}
