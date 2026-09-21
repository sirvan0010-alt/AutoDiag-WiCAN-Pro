package com.autodiag.core.diagnostic

import com.autodiag.core.can.CanFrame
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsoTpSessionTest {
    @Test
    fun singleFrameSendUsesConfiguredTxId() = runBlocking {
        val sent = mutableListOf<CanFrame>()
        val session = IsoTpSession(
            txId = 0x7E0,
            rxId = 0x7E8,
            sendFrame = { sent += it },
        )

        session.send(byteArrayOf(0x22, 0xF1.toByte(), 0x90)).getOrThrow()

        assertEquals(1, sent.size)
        assertEquals(0x7E0, sent.single().id)
        assertArrayEquals(
            byteArrayOf(0x03, 0x22, 0xF1.toByte(), 0x90),
            sent.single().data,
        )
    }

    @Test
    fun multiFrameSendWaitsForFlowControlAndHonorsBlockSize() = runBlocking {
        val sent = mutableListOf<CanFrame>()
        val session = IsoTpSession(
            txId = 0x7E0,
            rxId = 0x7E8,
            sendFrame = { sent += it },
        )

        val payload = ByteArray(20) { it.toByte() }
        session.send(payload).getOrThrow()
        assertEquals(1, sent.size)

        session.accept(CanFrame(0x7E8, byteArrayOf(0x30, 0x01, 0x00))).getOrThrow()

        assertEquals(2, sent.size)
        assertEquals(0x21, sent[1].data[0].toInt() and 0xFF)
    }

    @Test
    fun wrongRxIdCannotCompleteSession() = runBlocking {
        val sent = mutableListOf<CanFrame>()
        val session = IsoTpSession(
            txId = 0x7E0,
            rxId = 0x7E8,
            sendFrame = { sent += it },
        )

        val result = session.accept(CanFrame(0x7E9, byteArrayOf(0x02, 0x50, 0x01))).getOrThrow()

        assertEquals(null, result)
    }
}

class UdsClientTest {
    @Test
    fun requestRemainsOutstandingForResponsePendingAndClearsOnFinalResponse() = runBlocking {
        val sent = mutableListOf<CanFrame>()
        val session = IsoTpSession(
            txId = 0x7E0,
            rxId = 0x7E8,
            sendFrame = { sent += it },
        )
        val client = UdsClient(session)

        client.request(0x22, byteArrayOf(0xF1.toByte(), 0x90)).getOrThrow()
        assertTrue(client.hasOutstandingRequest())

        val pending = client.accept(
            CanFrame(0x7E8, byteArrayOf(0x03, 0x7F, 0x22, 0x78))
        ).getOrThrow()
        assertTrue(pending!!.pending)
        assertTrue(client.hasOutstandingRequest())

        val final = client.accept(
            CanFrame(0x7E8, byteArrayOf(0x03, 0x62, 0xF1.toByte(), 0x90))
        ).getOrThrow()
        assertFalse(final!!.pending)
        assertFalse(client.hasOutstandingRequest())
    }

    @Test
    fun resetClearsOutstandingRequestAndIsoTpState() = runBlocking {
        val session = IsoTpSession(
            txId = 0x7E0,
            rxId = 0x7E8,
            sendFrame = { },
        )
        val client = UdsClient(session)

        client.request(0x22).getOrThrow()
        assertTrue(client.hasOutstandingRequest())

        client.reset()

        assertFalse(client.hasOutstandingRequest())
    }
}
