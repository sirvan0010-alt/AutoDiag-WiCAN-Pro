package com.autodiag.core.transport

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

class TcpWiCanTransport : WiCanTransport {
    override val name: String = "WiCAN TCP"
    private val _state = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val state: ConnectionState get() = _state.value
    val stateFlow: StateFlow<ConnectionState> = _state

    private val incoming = MutableStateFlow(ByteArray(0))
    private val stream = kotlinx.coroutines.flow.MutableSharedFlow<ByteArray>(extraBufferCapacity = 256)
    private var socket: Socket? = null
    private var readerJob: kotlinx.coroutines.Job? = null
    private var activeConfig: TransportConfig? = null

    override suspend fun connect(config: TransportConfig): Result<Unit> = runCatching {
        activeConfig = config
        _state.value = ConnectionState.CONNECTING
        openSocket(config)
        startReader(config)
        _state.value = ConnectionState.CONNECTED
    }

    override suspend fun disconnect() {
        activeConfig = null
        readerJob?.cancel()
        readerJob = null
        closeSocket()
        _state.value = ConnectionState.DISCONNECTED
    }

    override suspend fun send(data: ByteArray): Result<Unit> = runCatching {
        val current = socket ?: error("WiCAN není připojen.")
        withContext(Dispatchers.IO) {
            current.getOutputStream().write(data)
            current.getOutputStream().flush()
        }
    }

    override fun observeIncoming(): Flow<ByteArray> = stream.asSharedFlow()

    private suspend fun openSocket(config: TransportConfig) = withContext(Dispatchers.IO) {
        val newSocket = Socket()
        newSocket.connect(InetSocketAddress(config.host, config.port), config.connectTimeoutMs.toInt())
        newSocket.soTimeout = config.readTimeoutMs.toInt()
        socket = newSocket
    }

    private fun startReader(config: TransportConfig) {
        readerJob?.cancel()
        readerJob = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(4096)
            try {
                while (true) {
                    val count = socket?.getInputStream()?.read(buffer) ?: -1
                    if (count < 0) throw java.io.EOFException("WiCAN ukončil TCP spojení.")
                    if (count > 0) stream.emit(buffer.copyOf(count))
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Throwable) {
                if (activeConfig?.autoReconnect == true) reconnectLoop(config)
                else _state.value = ConnectionState.ERROR
            }
        }
    }

    private suspend fun reconnectLoop(config: TransportConfig) {
        _state.value = ConnectionState.RECONNECTING
        closeSocket()
        var delayMs = 500L
        while (activeConfig != null) {
            try {
                openSocket(config)
                _state.value = ConnectionState.CONNECTED
                startReader(config)
                return
            } catch (_: Throwable) {
                delay(delayMs)
                delayMs = (delayMs * 2).coerceAtMost(8_000L)
            }
        }
    }

    private fun closeSocket() {
        runCatching { socket?.close() }
        socket = null
    }
}
