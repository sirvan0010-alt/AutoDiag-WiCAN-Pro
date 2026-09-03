package com.autodiag.core.can

import com.autodiag.core.transport.ConnectionState
import com.autodiag.core.transport.TransportConfig
import com.autodiag.core.transport.TransportMetrics
import com.autodiag.core.transport.WiCanTransport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SlcanCanFrameStreamTest {
    @Test
    fun publishesDecodedFramesFromTransportChunks() = runTest {
        val incoming = MutableSharedFlow<ByteArray>(extraBufferCapacity = 4)
        val transport = FakeTransport(incoming)
        val bridge = SlcanCanFrameStream(transport, this)
        val received = mutableListOf<CanFrame>()
        val collector = backgroundScope.launch {
            bridge.frames.collect { received += it }
        }

        incoming.emit("t1232A55\r".toByteArray())
        testScheduler.advanceUntilIdle()

        assertEquals(1, received.size)
        assertEquals(0x123L, received.single().id)
        assertEquals("2A 55", received.single().hex())
        collector.cancel()
        bridge.stop()
    }

    private class FakeTransport(private val incoming: Flow<ByteArray>) : WiCanTransport {
        override val name = "test"
        override val state = ConnectionState.CONNECTED
        override val metrics: StateFlow<TransportMetrics> = MutableStateFlow(TransportMetrics())
        override suspend fun connect(config: TransportConfig) = Result.success(Unit)
        override suspend fun disconnect() = Unit
        override suspend fun send(data: ByteArray) = Result.success(Unit)
        override fun observeIncoming(): Flow<ByteArray> = incoming
    }
}
