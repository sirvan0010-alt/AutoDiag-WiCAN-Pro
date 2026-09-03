package com.autodiag.core.can

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SlcanCodecTest {
    @Test fun decodesStandardDataFrame() {
        val frame = SlcanCodec.decode("t7E84010C1AF8", 100L)
        requireNotNull(frame)
        assertEquals(0x7E8L, frame.id)
        assertEquals("41 0C 1A F8", frame.hex())
        assertFalse(frame.isExtended)
        assertFalse(frame.isRemote)
        assertEquals(100L, frame.timestampNanos)
    }

    @Test fun decodesExtendedAndRemoteFrames() {
        val extended = SlcanCodec.decode("T18DAF11048DEADBEA")
        requireNotNull(extended)
        assertTrue(extended.isExtended)
        assertEquals(0x18DAF110L, extended.id)
        assertEquals("8D EA DB EA", extended.hex())

        val remote = SlcanCodec.decode("r7E88")
        requireNotNull(remote)
        assertTrue(remote.isRemote)
        assertEquals(8, remote.dataLength)
    }

    @Test fun roundTripsDataFrame() {
        val original = CanFrame(0x123, byteArrayOf(0x01, 0xAB.toByte(), 0xFF.toByte()))
        val encoded = SlcanCodec.encode(original)
        assertEquals("t123301ABFF\r", encoded)
        assertEquals(original, SlcanCodec.decode(encoded))
    }

    @Test fun streamDecoderHandlesTcpChunkBoundaries() {
        val decoder = SlcanCodec.StreamDecoder()
        assertTrue(decoder.accept("t1232AA").isEmpty())
        val frames = decoder.accept("BB\rt7E802010C\r")
        assertEquals(2, frames.size)
        assertEquals(0x123L, frames[0].id)
        assertEquals("AA BB", frames[0].hex())
        assertEquals(0x7E8L, frames[1].id)
        assertEquals("01 0C", frames[1].hex())
    }

    @Test fun rejectsMalformedFrames() {
        assertEquals(null, SlcanCodec.decode("x1232AABB"))
        assertEquals(null, SlcanCodec.decode("t1239AABB"))
        assertEquals(null, SlcanCodec.decode("t1232AA"))
    }
}
