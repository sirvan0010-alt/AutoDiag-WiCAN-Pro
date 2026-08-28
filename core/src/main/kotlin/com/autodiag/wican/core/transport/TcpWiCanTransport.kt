package com.autodiag.wican.core.transport

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.InetSocketAddress
import java.net.Socket

/** Read-only transport primitive. It does not interpret automotive payloads. */
class TcpWiCanTransport : WiCanTransport {
    private val _state = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val state: StateFlow<ConnectionState> = _state.asStateFlow()

    private var socket: Socket? = null
    private var input: BufferedInputStream? = null
    private var output: BufferedOutputStream? = null

    override suspend fun connect(config: TransportConfig) = withContext(Dispatchers.IO) {
        check(config.port in 1..65535) { "Invalid TCP port" }
        closeInternal()
        _state.value = ConnectionState.CONNECTING
        try {
            Socket().also { s ->
                s.soTimeout = config.readTimeoutMs.toInt()
                s.connect(InetSocketAddress(config.host, config.port), config.connectTimeoutMs.toInt())
                socket = s
                input = BufferedInputStream(s.getInputStream())
                output = BufferedOutputStream(s.getOutputStream())
            }
            _state.value = ConnectionState.CONNECTED
        } catch (t: Throwable) {
            closeInternal()
            _state.value = ConnectionState.ERROR
            throw t
        }
    }

    override suspend fun send(data: ByteArray) = withContext(Dispatchers.IO) {
        check(_state.value == ConnectionState.CONNECTED) { "Transport is not connected" }
        output?.write(data)
        output?.flush()
    }

    override suspend fun receive(): ByteArray = withContext(Dispatchers.IO) {
        check(_state.value == ConnectionState.CONNECTED) { "Transport is not connected" }
        val buffer = ByteArray(4096)
        val count = input?.read(buffer) ?: -1
        if (count < 0) {
            _state.value = ConnectionState.CLOSED
            return@withContext ByteArray(0)
        }
        buffer.copyOf(count)
    }

    override suspend fun close() = withContext(Dispatchers.IO) { closeInternal() }

    private fun closeInternal() {
        runCatching { input?.close() }
        runCatching { output?.close() }
        runCatching { socket?.close() }
        input = null
        output = null
        socket = null
        _state.value = ConnectionState.CLOSED
    }
}

class TcpElm327Transport : WiCanTransport by TcpWiCanTransport()
class TcpSlcanTransport : WiCanTransport by TcpWiCanTransport()
