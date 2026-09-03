package com.autodiag.core.can

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CanFrameStreamTest {
    @Test fun publishesFramesToSubscribers() = runBlocking {
        val stream = CanFrameStream()
        val expected = CanFrame(0x7E8, byteArrayOf(0x41, 0x0C))
        val received = async { stream.frames.first() }

        assertTrue(stream.publish(expected))
        assertEquals(expected, received.await())
    }

    @Test fun doesNotReplayOldFrames() = runBlocking {
        val stream = CanFrameStream()
        assertTrue(stream.publish(CanFrame(0x123, byteArrayOf(0x01))))

        val received = async { stream.frames.first() }
        assertTrue(stream.publish(CanFrame(0x456, byteArrayOf(0x02))))

        assertEquals(0x456L, received.await().id)
    }
}
