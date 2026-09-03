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
    fun splitTcpChunksAreReassembledBeforeDecode() = runBlocking {
        val transport = FakeElmTransport(mapOf("010d" to "41 0D 28"), splitResponses = true)
        val session = Elm327Session(transport)
        val engine = ObdLiveDataEngine(session)

        val sample = engine.stream(setOf(0x0D), intervalMs = 1_000)
            .take(1)
            .toList()
            .single()

        assertEquals(0x0D, sample.pid)
        assertEquals(40.0, sample.value)
        assertEquals("km/h", sample.unit)
        assertEquals(ObdLiveDataEngine.State.LIVE, sample.state)
    }

    @Test
    fun successfulPollUpdatesStoreAndEcuHealth() = runBlocking {
        val transport = FakeElmTransport(mapOf("0111" to "41 11 80"))
        val store = LiveDataStore()
        val monitor = EcuConnectionMonitor(nowMs = { 10_000L })
        val engine = ObdLiveDataEngine(
            Elm327Session(transport),
            nowEpochMs = { 10_000L },
            store = store,
            ecuMonitor = monitor
        )

        engine.stream(setOf(0x11), intervalMs = 1_000).take(1).toList()

        val stored = store.get(0x11)
        assertEquals(50.19607843137255, stored?.value)
        assertEquals(LiveDataQuality.GOOD, stored?.quality)
        assertEquals(LiveDataFreshness.FRESH, stored?.freshness)
        assertEquals(EcuConnectionState.ONLINE, monitor.state())
    }

    @Test
    fun noDataDoesNotBecomeMeasuredValueOrEraseLastKnownValue() = runBlocking {
        val store = LiveDataStore()
        val monitor = EcuConnectionMonitor(nowMs = { 10_000L })
        val first = FakeElmTransport(mapOf("010c" to "41 0C 1A F8"))
        ObdLiveDataEngine(
            Elm327Session(first),
            nowEpochMs = { 10_000L },
            store = store,
            ecuMonitor = monitor
        ).stream(setOf(0x0C), intervalMs = 1_000).take(1).toList()

        val second = FakeElmTransport(mapOf("010c" to "NO DATA"))
        val sample = ObdLiveDataEngine(
            Elm327Session(second),
            nowEpochMs = { 10_000L },
            store = store,
            ecuMonitor = monitor
        ).stream(setOf(0x0C), intervalMs = 1_000).take(1).toList().single()

        assertEquals(ObdLiveDataEngine.State.UNAVAILABLE, sample.state)
        assertNull(sample.value)
        assertEquals(1726.0, store.get(0x0C)?.value)
        assertEquals(EcuConnectionState.ONLINE, monitor.state())
    }

    private class FakeElmTransport(
        private val responses: Map<String, String>,
        private val splitResponses: Boolean = false
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
            val bytes = (response + "\r>").toByteArray(Charsets.US_ASCII)
            if (splitResponses && bytes.size > 2) {
                val midpoint = bytes.size / 2
                incoming.emit(bytes.copyOfRange(0, midpoint))
                incoming.emit(bytes.copyOfRange(midpoint, bytes.size))
            } else {
                incoming.emit(bytes)
            }
            return Result.success(Unit)
        }

        override fun observeIncoming(): Flow<ByteArray> = incoming
    }
}
