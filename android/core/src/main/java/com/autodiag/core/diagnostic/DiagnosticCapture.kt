package com.autodiag.core.diagnostic

import com.autodiag.core.transport.WiCanTransport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.CopyOnWriteArrayList

/** Raw transport capture. It deliberately carries no vehicle/OBD semantics. */
enum class CaptureDirection { TX, RX }

data class DiagnosticCaptureRecord(
    val sessionId: String,
    val timestampEpochMs: Long,
    val direction: CaptureDirection,
    val payload: ByteArray,
    val transport: String? = null
) {
    fun payloadEquals(other: ByteArray): Boolean = payload.contentEquals(other)
}

class DiagnosticCaptureStore {
    private val records = CopyOnWriteArrayList<DiagnosticCaptureRecord>()

    fun append(record: DiagnosticCaptureRecord) {
        records += record
    }

    fun snapshot(): List<DiagnosticCaptureRecord> = records.toList()

    fun clear() = records.clear()
}

/** Adds raw TX/RX capture around an existing transport without decoding it. */
class DiagnosticCapturingTransport(
    private val delegate: WiCanTransport,
    private val sessionId: String,
    private val store: DiagnosticCaptureStore,
    private val clockMs: () -> Long = { System.currentTimeMillis() }
) : WiCanTransport by delegate {
    override suspend fun send(data: ByteArray): Result<Unit> {
        val result = delegate.send(data)
        if (result.isSuccess) {
            store.append(
                DiagnosticCaptureRecord(
                    sessionId = sessionId,
                    timestampEpochMs = clockMs(),
                    direction = CaptureDirection.TX,
                    payload = data.copyOf(),
                    transport = delegate.name
                )
            )
        }
        return result
    }

    override fun observeIncoming(): Flow<ByteArray> =
        delegate.observeIncoming().onEach { data ->
            store.append(
                DiagnosticCaptureRecord(
                    sessionId = sessionId,
                    timestampEpochMs = clockMs(),
                    direction = CaptureDirection.RX,
                    payload = data.copyOf(),
                    transport = delegate.name
                )
            )
        }
}
