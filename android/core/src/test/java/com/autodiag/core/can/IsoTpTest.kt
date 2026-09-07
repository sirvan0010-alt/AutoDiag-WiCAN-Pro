package com.autodiag.core.can

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class IsoTpTest {
    @Test fun singleFrameRoundTrip() {
        val payload = "22BC03".hex()
        val segment = IsoTp.segment(payload)
        assertEquals(1, segment.frames.size)
        val rx = IsoTp.Reassembler()
        val result = rx.accept(CanFrame(0x700, segment.frames.single()))
        assertArrayEquals(payload, result.payload)
    }

    @Test fun multiFrameRoundTrip() {
        val payload = ByteArray(20) { it.toByte() }
        val segment = IsoTp.segment(payload)
        assertEquals(3, segment.frames.size)
        val rx = IsoTp.Reassembler()
        var result: IsoTp.ReassemblyResult? = null
        segment.frames.forEach { result = rx.accept(CanFrame(0x700, it)) }
        assertArrayEquals(payload, result!!.payload)
    }

    @Test fun sequenceMismatchIsRejected() {
        val payload = ByteArray(12) { it.toByte() }
        val frames = IsoTp.segment(payload).frames
        val rx = IsoTp.Reassembler()
        rx.accept(CanFrame(0x700, frames[0]))
        val bad = frames[1].copyOf().also { it[0] = 0x22 }
        val result = rx.accept(CanFrame(0x700, bad))
        assertEquals(null, result.payload)
        assert(result.error!!.contains("sequence mismatch"))
    }

    private fun String.hex(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
