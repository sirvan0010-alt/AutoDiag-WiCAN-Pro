package com.autodiag.core.diagnostic

import com.autodiag.core.can.CanFrame
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IsoTpExtendedAddressingTest {
    @Test
    fun max29BitIdentifiersAreAcceptedForExtendedSession() = runBlocking {
        val sent = mutableListOf<CanFrame>()
        val session = IsoTpSession(
            txId = 0x1FFFFFFF,
            rxId = 0x1ABCDEFF,
            sendFrame = { sent += it },
            isExtended = true,
        )

        session.send(byteArrayOf(0x22, 0xF1.toByte(), 0x90)).getOrThrow()

        assertEquals(0x1FFFFFFF, sent.single().id)
        assertTrue(sent.single().isExtended)
        assertNull(session.accept(CanFrame(0x1ABCDEFF, byteArrayOf(0x03, 0x62, 0xF1.toByte(), 0x90))).getOrThrow())
        assertEquals(0x1ABCDEFF, session.rxId)
    }

    @Test
    fun extendedSessionRejectsStandardFrameWithSameIdentifier() = runBlocking {
        val session = IsoTpSession(
            txId = 0x18DAF110,
            rxId = 0x18DA10F1,
            sendFrame = { },
            isExtended = true,
        )

        val result = session.accept(
            CanFrame(0x18DA10F1, byteArrayOf(0x03, 0x62, 0xF1.toByte(), 0x90))
        ).getOrThrow()

        assertNull(result)
    }

    @Test
    fun identifierAbove29BitBoundaryIsRejected() {
        val failed = runCatching {
            IsoTpSession(
                txId = 0x20000000,
                rxId = 0x18DA10F1,
                sendFrame = { },
                isExtended = true,
            )
        }
        assertTrue(failed.isFailure)
    }
}
