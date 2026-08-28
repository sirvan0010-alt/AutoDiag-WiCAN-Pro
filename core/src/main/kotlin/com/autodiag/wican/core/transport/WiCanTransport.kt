package com.autodiag.wican.core.transport

import kotlinx.coroutines.flow.StateFlow

interface WiCanTransport {
    val state: StateFlow<ConnectionState>
    suspend fun connect(config: TransportConfig)
    suspend fun send(data: ByteArray)
    suspend fun receive(): ByteArray
    suspend fun close()
}

data class TransportConfig(
    val host: String,
    val port: Int,
    val connectTimeoutMs: Long = 5_000,
    val readTimeoutMs: Long = 5_000
)

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, ERROR, CLOSED }
en
enum class TransportMode { ELM327_TCP, SLCAN_TCP }
