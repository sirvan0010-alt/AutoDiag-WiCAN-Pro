package com.autodiag.core.transport

import com.autodiag.core.diagnostic.CaptureDirection
import com.autodiag.core.diagnostic.DiagnosticCaptureRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReplayTransportTest {
    @Test
    fun matchingTxEmitsFollowingRx() = runBlocking {
        val transport = ReplayTransport(listOf(
            record(CaptureDirection.TX, "010C\r"),
            record(CaptureDirection.RX, "41 0C 1A F8\r>"),
            record(CaptureDirection.TX, "010D\r"),
            record(CaptureDirection.RX, "41 0D 28\r>")
        ))
        transport.connect(config())
        val values = mutableListOf<ByteArray>()
        val job = launch { repeat(2) { values += transport.observeIncoming().first() } }
        yield()
        transport.send("010C\r".toByteArray())
        transport.send("010D\r".toByteArray())
        job.join()
        assertArrayEquals("41 0C 1A F8\r>".toByteArray(), values[0])
        assertArrayEquals("41 0D 28\r>".toByteArray(), values[1])
        assertEquals(2, transport.metrics.value.txChunks)
        assertEquals(2, transport.metrics.value.rxChunks)
    }

    @Test
    fun mismatchedTxDoesNotAdvanceReplay() = runBlocking {
        val transport = ReplayTransport(listOf(record(CaptureDirection.TX, "010C\r")))
        transport.connect(config())
        assertTrue(transport.send("010D\r".toByteArray()).isFailure)
        assertEquals(0, transport.metrics.value.txChunks)
        assertTrue(transport.send("010C\r".toByteArray()).isSuccess)
    }

    private fun record(direction: CaptureDirection, payload: String) =
        DiagnosticCaptureRecord("test-session", 1000L, direction, payload.toByteArray(), "test")

    private fun config() = TransportConfig(host = "replay", port = 0, mode = TransportMode.SIMULATOR)
}
