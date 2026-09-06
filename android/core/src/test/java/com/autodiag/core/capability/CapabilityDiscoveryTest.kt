package com.autodiag.core.capability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilityDiscoveryTest {
    @Test
    fun supportedMode01BitmapDecodingUsesMsbFirstPidOrder() {
        val bitmap = listOf(0x80, 0x00, 0x00, 0x01)
        val supported = linkedSetOf<Int>()
        for (bit in 0 until 32) {
            if (((bitmap[0].toLong() shl 24) or
                    (bitmap[1].toLong() shl 16) or
                    (bitmap[2].toLong() shl 8) or
                    bitmap[3].toLong()) and (1L shl (31 - bit)) != 0L) {
                supported += bit + 1
            }
        }
        assertEquals(setOf(1, 32), supported)
        assertTrue(0x0C !in supported)
    }
}
