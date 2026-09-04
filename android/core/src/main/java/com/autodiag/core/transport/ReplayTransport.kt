package com.autodiag.core.transport

import com.autodiag.core.diagnostic.CaptureDirection
import com.autodiag.core.diagnostic.DiagnosticCaptureRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Deterministic transport that replays captured RX chunks in response to matching TX. */
class ReplayTransport(
    private val records: List<DiagnosticCaptureRecord>,
    override val name: String = "replay"
) : WiCanTransport {
    private val incoming = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    private val mutableState = MutableStateFlow(ConnectionState.DISCONNECTED)
    private val mutableMetrics = MutableStateFlow(TransportMetrics())
    private var cursor = 0

    override val state: ConnectionState get() = mutableState.value
    override val metrics: StateFlow<TransportMetrics> get() = mutableMetrics

    override suspend fun connect(config: TransportConfig): Result<Unit> {
        cursor = 0
        mutableState.value = ConnectionState.CONNECTED
        mutableMetrics.value = TransportMetrics(connectedAtMs = System.currentTimeMillis())
        return Result.success(Unit)
    }

    override suspend fun disconnect() {
        mutableState.value = ConnectionState.DISCONNECTED
    }

    override suspend fun send(data: ByteArray): Result<Unit> {
        if (state != ConnectionState.CONNECTED) {
            return Result.failure(IllegalStateException("Replay transport is not connected"))
        }
        if (cursor >= records.size) {
            return Result.failure(IllegalStateException("Replay exhausted; no TX record expected"))
        }
        val expected = records[cursor]
        if (expected.direction != CaptureDirection.TX || !expected.payloadEquals(data)) {
            return Result.failure(IllegalStateException("Replay TX mismatch at index $cursor"))
        }
        cursor++
        var rxChunks = 0L
        var rxBytes = 0L
        while (cursor < records.size && records[cursor].direction == CaptureDirection.RX) {
            val payload = records[cursor].payload.copyOf()
            incoming.tryEmit(payload)
            rxChunks++
            rxBytes += payload.size
            cursor++
        }
        val now = System.currentTimeMillis()
        val old = mutableMetrics.value
        mutableMetrics.value = old.copy(
            txChunks = old.txChunks + 1,
            txBytes = old.txBytes + data.size,
            rxChunks = old.rxChunks + rxChunks,
            rxBytes = old.rxBytes + rxBytes,
            lastTxAtMs = now,
            lastRxAtMs = if (rxChunks > 0) now else old.lastRxAtMs
        )
        return Result.success(Unit)
    }

    override fun observeIncoming(): Flow<ByteArray> = incoming
}
