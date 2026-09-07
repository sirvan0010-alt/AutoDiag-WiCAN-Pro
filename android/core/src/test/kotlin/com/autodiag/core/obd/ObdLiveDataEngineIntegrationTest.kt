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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Integration evidence for the Mode 01 pipeline:
 * selected PID -> ELM327 command -> adapter response -> Mode01Decoder -> sample.
 *
 * The transport is synthetic; this test does not claim vehicle verification.
 */
class ObdLiveDataEngineIntegrationTest {

    @Test
    fun `mode01 pid is requested and decoded into live sample`() = runBlocking {
        val transport = FakeElmTransport()
        val session = Elm327Session(transport)
        val engine = ObdLiveDataEngine(session, nowEpochMs = { 1_000L })

        val samples = engine.stream(
            supportedPids = setOf(0x0C),
            plans = listOf(LiveDataPollPlan(pid = 0x0C, intervalMs = 60_000L))
        ).take(1).toList()

        assertEquals(listOf("010C\r"), transport.sentCommands)
        assertEquals(1, samples.size)
        assertEquals(0x0C, samples.single().pid)
        assertEquals(1726.0, samples.single().value!!, 0.0)
        assertEquals("rpm", samples.single().unit)
        assertEquals(ObdLiveDataEngine.State.LIVE, samples.single().state)
        assertTrue(samples.single().rawHex.contains("41 0C 1A F8"))
    }

    private class FakeElmTransport : WiCanTransport {
        override val name: String = "synthetic-elm327"
        override val state: ConnectionState = ConnectionState.CONNECTED
        private val _metrics = MutableStateFlow(TransportMetrics())
        override val metrics: StateFlow<TransportMetrics> = _metrics
        private val incoming = MutableSharedFlow<ByteArray>(extraBufferCapacity = 8)
        val sentCommands = mutableListOf<String>()

        override suspend fun connect(config: TransportConfig): Result<Unit> = Result.success(Unit)
        override suspend fun disconnect() = Unit

        override suspend fun send(data: ByteArray): Result<Unit> {
            val command = data.toString(Charsets.US_ASCII)
            sentCommands += command
            if (command.trim() == "010C") {
                incoming.tryEmit("7E8 04 41 0C 1A F8\r>".toByteArray(Charsets.US_ASCII))
            }
            return Result.success(Unit)
        }

        override fun observeIncoming(): Flow<ByteArray> = incoming
    }
}
