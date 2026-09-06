package com.autodiag.core.can

import com.autodiag.core.transport.ConnectionState
import com.autodiag.core.transport.TransportConfig
import com.autodiag.core.transport.TransportMetrics
import com.autodiag.core.transport.WiCanTransport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class SlcanCaptureControllerTest {
    @Test
    fun captures_frames_from_live_slcan_stream_and_returns_immutable_session() = runTest {
        val incoming = MutableSharedFlow<ByteArray>(extraBufferCapacity = 4)
        val stream = SlcanCanFrameStream(FakeTransport(incoming), backgroundScope)
        val controller = SlcanCaptureController(stream, CanCapture(clockNanos = { 1_000L }), backgroundScope)

        controller.start(nowNanos = 1_000L)
        incoming.emit("t1232A55\r".toByteArray())
        testScheduler.advanceUntilIdle()

        val session = controller.stop()
        assertNotNull(session)
        assertEquals(1, session!!.frameCount)
        assertEquals(0x123L, session.records.single().frame.id)
        assertEquals("2A 55", session.records.single().frame.hex())
        assertFalse(controller.isCapturing)
        stream.stop()
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
