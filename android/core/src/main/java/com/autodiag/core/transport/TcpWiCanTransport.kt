package com.autodiag.core.transport

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.EOFException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import kotlinx.coroutines.CoroutineScope

class TcpWiCanTransport : WiCanTransport {
    override val name: String = "WiCAN TCP"
    private val _state = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val state: ConnectionState get() = _state.value
    val stateFlow: StateFlow<ConnectionState> = _state

    private val _metrics = MutableStateFlow(TransportMetrics())
    override val metrics: StateFlow<TransportMetrics> = _metrics.asStateFlow()

    private val stream = MutableSharedFlow<ByteArray>(extraBufferCapacity = 256)
    private var socket: Socket? = null
    private var readerJob: Job? = null
    private var activeConfig: TransportConfig? = null

    override suspend fun connect(config: TransportConfig): Result<Unit> = runCatching {
        activeConfig = config
        readerJob?.cancel()
        closeSocket()
        _state.value = ConnectionState.CONNECTING
        openSocket(config)
        _metrics.value = TransportMetrics(connectedAtMs = System.currentTimeMillis())
        _state.value = ConnectionState.CONNECTED
        startReader(config)
    }

    override suspend fun disconnect() {
        activeConfig = null
        readerJob?.cancel()
        readerJob = null
        closeSocket()
        _state.value = ConnectionState.DISCONNECTED
        _metrics.value = TransportMetrics()
    }

    override suspend fun send(data: ByteArray): Result<Unit> = runCatching {
        val current = socket ?: error("WiCAN není připojen.")
        withContext(Dispatchers.IO) {
            current.getOutputStream().write(data)
            current.getOutputStream().flush()
        }
        _metrics.update {
            it.copy(
                txChunks = it.txChunks + 1,
                txBytes = it.txBytes + data.size,
                lastTxAtMs = System.currentTimeMillis()
            )
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
        readerJob = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(4096)
            var reconnectDelayMs = 500L
            try {
                while (isActive && activeConfig != null) {
                    try {
                        val count = socket?.getInputStream()?.read(buffer) ?: -1
                        if (count < 0) throw EOFException("WiCAN ukončil TCP spojení.")
                        if (count > 0) {
                            stream.emit(buffer.copyOf(count))
                            _metrics.update {
                                it.copy(
                                    rxChunks = it.rxChunks + 1,
                                    rxBytes = it.rxBytes + count,
                                    lastRxAtMs = System.currentTimeMillis()
                                )
                            }
                        }
                    } catch (_: SocketTimeoutException) {
                        // A read timeout is not a disconnect. Keep the link alive.
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Throwable) {
                        if (activeConfig?.autoReconnect != true) {
                            _state.value = ConnectionState.ERROR
                            return@launch
                        }
                        _state.value = ConnectionState.RECONNECTING
                        closeSocket()
                        while (isActive && activeConfig != null) {
                            try {
                                openSocket(config)
                                reconnectDelayMs = 500L
                                _metrics.update {
                                    it.copy(
                                        reconnects = it.reconnects + 1,
                                        connectedAtMs = System.currentTimeMillis()
                                    )
                                }
                                _state.value = ConnectionState.CONNECTED
                                break
                            } catch (_: Throwable) {
                                delay(reconnectDelayMs)
                                reconnectDelayMs = (reconnectDelayMs * 2).coerceAtMost(8_000L)
                            }
                        }
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            }
        }
    }

    private fun closeSocket() {
        runCatching { socket?.close() }
        socket = null
    }
}
