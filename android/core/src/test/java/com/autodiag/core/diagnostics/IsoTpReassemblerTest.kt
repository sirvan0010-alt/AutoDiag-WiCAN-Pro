package com.autodiag.core.diagnostics

import com.autodiag.core.can.CanFrame
import com.autodiag.core.diagnostics.isotp.IsoTpReassembler
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class IsoTpReassemblerTest {
    @Test fun reassemblesFirstAndConsecutiveFrames() {
        val reassembler = IsoTpReassembler()
        val first = CanFrame(0x7E8, byteArrayOf(0x10, 0x0A, 0x62, 0xF1, 0x90, 0x56, 0x49, 0x4E))
        val consecutive = CanFrame(0x7E8, byteArrayOf(0x21, 0x31, 0x32, 0x33, 0x34))

        assertEquals(null, reassembler.accept(first).getOrThrow())
        assertArrayEquals(
            byteArrayOf(0x62, 0xF1.toByte(), 0x90.toByte(), 0x56, 0x49, 0x4E, 0x31, 0x32, 0x33, 0x34),
            reassembler.accept(consecutive).getOrThrow()
        )
    }

    @Test fun singleFrameCompletesImmediately() {
        val result = IsoTpReassembler().accept(
            CanFrame(0x7E8, byteArrayOf(0x03, 0x41, 0x0C, 0x1A))
        ).getOrThrow()
        assertArrayEquals(byteArrayOf(0x41, 0x0C, 0x1A), result)
    }
}
