package com.autodiag.core.capability

import org.junit.Assert.assertEquals
import org.junit.Test

class Mode01SupportedPidBitmapTest {
    @Test
    fun saeBitmapMapsMostSignificantBitToFirstPid() {
        val bytes = intArrayOf(0x80, 0x00, 0x00, 0x01)
        val supported = (0 until 32).filter { bit ->
            val byteIndex = bit / 8
            val bitInByte = 7 - (bit % 8)
            (bytes[byteIndex] and (1 shl bitInByte)) != 0
        }.map { it + 1 }
        assertEquals(listOf(1, 32), supported)
    }
}
