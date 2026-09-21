package com.autodiag.core.diagnostic

import com.autodiag.core.can.CanFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UdsTimeoutPolicyTest {
    @Test
    fun responsePendingSwitchesFromP2ToP2Star() = runBlocking {
        val session = IsoTpSession(
            txId = 0x7E0,
            rxId = 0x7E8,
            sendFrame = { },
        )
        val client = UdsClient(session)
        val frames = ArrayDeque(
            listOf(
                CanFrame(0x7E8, byteArrayOf(0x03, 0x7F, 0x22, 0x78)),
                CanFrame(0x7E8, byteArrayOf(0x03, 0x62, 0xF1.toByte(), 0x90)),
            )
        )

        val result = client.requestAndWait(
            service = 0x22,
            payload = byteArrayOf(0xF1.toByte(), 0x90),
            policy = UdsTimeoutPolicy(p2Millis = 100, p2StarMillis = 500),
            receiveFrame = { frames.removeFirst() },
        ).getOrThrow()

        assertEquals(0x62, result.responseService)
        assertTrue(result.positive)
    }

    @Test
    fun p2ExpiryReturnsFailureAndResetsClient() = runBlocking {
        val client = UdsClient(
            IsoTpSession(0x7E0, 0x7E8, sendFrame = { })
        )

        val result = client.requestAndWait(
            service = 0x22,
            policy = UdsTimeoutPolicy(p2Millis = 10, p2StarMillis = 100),
            receiveFrame = {
                delay(100)
                CanFrame(0x7E8, byteArrayOf(0x03, 0x62, 0xF1.toByte(), 0x90))
            },
        )

        assertTrue(result.isFailure)
        assertEquals(false, client.hasOutstandingRequest())
    }

    @Test
    fun p2StarExpiryAfterPendingReturnsFailure() = runBlocking {
        val client = UdsClient(
            IsoTpSession(0x7E0, 0x7E8, sendFrame = { })
        )

        val result = client.requestAndWait(
            service = 0x22,
            policy = UdsTimeoutPolicy(p2Millis = 100, p2StarMillis = 10),
            receiveFrame = {
                CanFrame(0x7E8, byteArrayOf(0x03, 0x7F, 0x22, 0x78))
            },
        )

        assertTrue(result.isFailure)
        assertEquals(false, client.hasOutstandingRequest())
    }
}
