package com.autodiag.core.obd

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
import kotlin.test.assertNull

/** End-to-end replay test: fake WiCAN transport -> ELM327 session -> Mode 01 decoder. */
class ObdLiveDataEngineIntegrationTest {
    @Test
    fun rpmResponseIsDecodedThroughRealSessionPath() = runBlocking {
        val transport = FakeElmTransport(mapOf("010c" to "41 0C 1A F8"))
        val session = Elm327Session(transport)
        val engine = ObdLiveDataEngine(session)

        val sample = engine.stream(setOf(0x0C), intervalMs = 1_000)
            .take(1)
            .toList()
            .single()

        assertEquals(0x0C, sample.pid)
        assertEquals(1726.0, sample.value)
        assertEquals("ot/min", sample.unit)
        assertEquals(ObdLiveDataEngine.State.LIVE, sample.state)
        assertEquals("41 0C 1A F8", sample.rawHex)
        assertNull(sample.error)
    }

    @Test
    fun noDataDoesNotBecomeMeasuredValue() = runBlocking {
        val transport = FakeElmTransport(mapOf("010c" to "NO DATA"))
        val session = Elm327Session(transport)
        val engine = ObdLiveDataEngine(session)

        val sample = engine.stream(setOf(0x0C), intervalMs = 1_000)
            .take(1)
            .toList()
            .single()

        assertEquals(ObdLiveDataEngine.State.UNAVAILABLE, sample.state)
        assertNull(sample.value)
    }

    private class FakeElmTransport(
        private val responses: Map<String, String>
    ) : WiCanTransport {
        private val incoming = MutableSharedFlow<ByteArray>(extraBufferCapacity = 8)
        private val _metrics = MutableStateFlow(TransportMetrics())

        override val name: String = "fake-elm"
        override val state: ConnectionState = ConnectionState.CONNECTED
        override val metrics: StateFlow<TransportMetrics> = _metrics

        override suspend fun connect(config: TransportConfig): Result<Unit> = Result.success(Unit)
        override suspend fun disconnect() = Unit

        override suspend fun send(data: ByteArray): Result<Unit> {
            val command = data.toString(Charsets.US_ASCII).trim().lowercase()
            val response = responses[command] ?: "NO DATA"
            incoming.emit((response + "\r>").toByteArray(Charsets.US_ASCII))
            return Result.success(Unit)
        }

        override fun observeIncoming(): Flow<ByteArray> = incoming
    }
}
