package com.autodiag.core.obd

import com.autodiag.core.diagnostic.DiagnosticEventStream
import com.autodiag.core.diagnostic.DiagnosticEventType
import com.autodiag.core.transport.ConnectionState
import com.autodiag.core.transport.TransportConfig
import com.autodiag.core.transport.TransportMetrics
import com.autodiag.core.transport.WiCanTransport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ObdLiveDataEventIntegrationTest {
    @Test
    fun decodedMeasurementIsPublishedAsDiagnosticEvent() = runBlocking {
        val events = DiagnosticEventStream()
        val transport = FakeTransport("41 0C 1A F8")
        val engine = ObdLiveDataEngine(
            session = Elm327Session(transport),
            nowEpochMs = { 20_000L },
            eventStream = events,
            sessionId = "session-1"
        )

        engine.stream(setOf(0x0C), intervalMs = 1_000).take(1).toList()

        val event = events.snapshot().single()
        assertEquals(DiagnosticEventType.MEASUREMENT_RECEIVED, event.type)
        assertEquals("session-1", event.sessionId)
        assertEquals("obd.mode01.pid.0C", event.evidenceKey)
        assertEquals(20_000L, event.timestampEpochMs)
    }

    private class FakeTransport(private val response: String) : WiCanTransport {
        private val incoming = MutableSharedFlow<ByteArray>(extraBufferCapacity = 4)
        private val metricsState = MutableStateFlow(TransportMetrics())

        override val name: String = "fake-event"
        override val state: ConnectionState = ConnectionState.CONNECTED
        override val metrics: StateFlow<TransportMetrics> = metricsState

        override suspend fun connect(config: TransportConfig): Result<Unit> = Result.success(Unit)
        override suspend fun disconnect() = Unit

        override suspend fun send(data: ByteArray): Result<Unit> {
            incoming.emit((response + "\r>").toByteArray(Charsets.US_ASCII))
            return Result.success(Unit)
        }

        override fun observeIncoming(): Flow<ByteArray> = incoming
    }
}
