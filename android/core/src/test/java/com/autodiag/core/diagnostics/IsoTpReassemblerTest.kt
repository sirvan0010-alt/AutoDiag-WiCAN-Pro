package com.autodiag.core.diagnostics

import com.autodiag.core.can.CanFrame
import com.autodiag.core.diagnostics.isotp.IsoTpReassembler
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsoTpReassemblerTest {
    @Test fun reassemblesFirstAndConsecutiveFrames() {
        val reassembler = IsoTpReassembler()
        val first = CanFrame(0x7E8, byteArrayOf(0x10, 0x0A, 0x62, 0xF1.toByte(), 0x90.toByte(), 0x56, 0x49, 0x4E))
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

    @Test fun rejectsConsecutiveFrameWithoutFirstFrame() {
        val result = IsoTpReassembler().accept(
            CanFrame(0x7E8, byteArrayOf(0x21, 0x01))
        )
        assertTrue(result.isFailure)
    }

    @Test fun rejectsUnexpectedSequenceNumber() {
        val reassembler = IsoTpReassembler()
        reassembler.accept(CanFrame(0x7E8, byteArrayOf(0x10, 0x09, 0x62, 0x01, 0x02, 0x03, 0x04, 0x05)))

        val result = reassembler.accept(CanFrame(0x7E8, byteArrayOf(0x22, 0x06, 0x07, 0x08)))
        assertTrue(result.isFailure)
    }

    @Test fun trimsPaddingAfterFinalPayloadByte() {
        val reassembler = IsoTpReassembler()
        reassembler.accept(CanFrame(0x7E8, byteArrayOf(0x10, 0x08, 0x62, 0xF1.toByte(), 0x90.toByte(), 0x56, 0x49, 0x4E, 0x01)))

        val result = reassembler.accept(CanFrame(0x7E8, byteArrayOf(0x21, 0xFF.toByte(), 0xEE.toByte(), 0xDD.toByte())))
        assertFalse(result.isFailure)
        assertArrayEquals(
            byteArrayOf(0x62, 0xF1.toByte(), 0x90.toByte(), 0x56, 0x49, 0x4E, 0x01, 0xFF.toByte()),
            result.getOrThrow()
        )
    }

    @Test fun resetsAfterCompletedPayload() {
        val reassembler = IsoTpReassembler()
        reassembler.accept(CanFrame(0x7E8, byteArrayOf(0x10, 0x08, 0x62, 0x01, 0x02, 0x03, 0x04, 0x05)))
        assertFalse(reassembler.accept(CanFrame(0x7E8, byteArrayOf(0x21, 0x06))).isFailure)

        val result = reassembler.accept(CanFrame(0x7E8, byteArrayOf(0x03, 0x41, 0x0C, 0x10))).getOrThrow()
        assertArrayEquals(byteArrayOf(0x41, 0x0C, 0x10), result)
    }
}
