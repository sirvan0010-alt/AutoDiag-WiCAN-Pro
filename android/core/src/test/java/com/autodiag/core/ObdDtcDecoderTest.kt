package com.autodiag.core

import com.autodiag.core.obd.DtcMemory
import com.autodiag.core.obd.ObdDtcDecoder
import com.autodiag.core.obd.ObdDtcCommands
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObdDtcDecoderTest {
    @Test
    fun decodesStoredDtc() {
        val result = ObdDtcDecoder.decode(byteArrayOf(0x43, 0x03, 0x01, 0x40, 0x33), DtcMemory.STORED)
        assertEquals(listOf("P0301", "C0033"), result.map { it.code })
        assertTrue(result.all { it.memory == DtcMemory.STORED })
    }

    @Test
    fun decodesPendingAndSkipsPadding() {
        val result = ObdDtcDecoder.decode(byteArrayOf(0x47, 0x01, 0x02, 0x00, 0x00), DtcMemory.PENDING)
        assertEquals(listOf("P0102"), result.map { it.code })
    }

    @Test
    fun decodesPermanent() {
        val result = ObdDtcDecoder.decode(byteArrayOf(0x4A, 0xB1.toByte(), 0x23), DtcMemory.PERMANENT)
        assertEquals("U1123", result.single().code)
        assertEquals(DtcMemory.PERMANENT, result.single().memory)
    }

    @Test
    fun clearRequestIsMode04AndStateChanging() {
        assertEquals(0x04, ObdDtcCommands.clearRequest()[0].toInt() and 0xFF)
    }
}
