package com.autodiag.core.can

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CanFrameTest {
    @Test fun framePreservesIdAndPayload() {
        val frame = CanFrame(0x7E8, byteArrayOf(0x41, 0x0C, 0x1A, 0xF8))
        assertEquals(0x7E8L, frame.id)
        assertEquals(4, frame.dataLength)
        assertEquals("41 0C 1A F8", frame.hex())
    }

    @Test fun maskFilterMatchesExpectedIds() {
        val filter = CanFrameFilter(id = 0x7E8, mask = 0x7F8)
        assertTrue(filter.matches(CanFrame(0x7E8)))
        assertTrue(filter.matches(CanFrame(0x7E9)))
        assertFalse(filter.matches(CanFrame(0x700)))
    }

    @Test fun statsCalculateFrameRateFromTimestamps() {
        var stats = CanBusStats()
        stats = stats.onFrame(CanFrame(1, timestampNanos = 0L))
        stats = stats.onFrame(CanFrame(1, timestampNanos = 1_000_000_000L))
        assertEquals(2L, stats.receivedFrames)
        assertEquals(2.0, stats.frameRateHz()!!, 0.0001)
    }
}
