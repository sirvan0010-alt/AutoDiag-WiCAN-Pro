package com.autodiag.core.transport

import kotlinx.coroutines.flow.Flow

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING, ERROR }

enum class TransportMode { ELM327, SLCAN_RAW, SIMULATOR }

data class TransportConfig(
    val host: String,
    val port: Int,
    val mode: TransportMode = TransportMode.ELM327,
    val connectTimeoutMs: Long = 5_000,
    val readTimeoutMs: Long = 3_000,
    val autoReconnect: Boolean = true
)

interface WiCanTransport {
    val name: String
    val state: ConnectionState
    suspend fun connect(config: TransportConfig): Result<Unit>
    suspend fun disconnect()
    suspend fun send(data: ByteArray): Result<Unit>
    fun observeIncoming(): Flow<ByteArray>
}
