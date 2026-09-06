package com.autodiag.core.capability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilityDiscoveryTest {
    @Test
    fun supportedMode01BitmapMapsMsbFirstToFirstPid() {
        val bitmap = listOf(0x80, 0x00, 0x00, 0x01)
        assertEquals(setOf(1, 32), Mode01SupportedPidBitmapDecoder.supportedPids(bitmap, 0x00))
        assertTrue(Mode01SupportedPidBitmapDecoder.hasContinuation(bitmap))
    }

    @Test
    fun parserAcceptsPlainElm327Response() {
        assertEquals(
            listOf(0x80, 0x00, 0x00, 0x01),
            Mode01SupportedPidBitmapDecoder.parsePayload("41 00 80 00 00 01", 0x00)
        )
    }

    @Test
    fun parserAcceptsCanHeaderPrefixedResponse() {
        assertEquals(
            listOf(0x80, 0x00, 0x00, 0x01),
            Mode01SupportedPidBitmapDecoder.parsePayload("7E8 06 41 00 80 00 00 01", 0x00)
        )
    }

    @Test
    fun parserAcceptsCompactResponse() {
        assertEquals(
            listOf(0x80, 0x00, 0x00, 0x01),
            Mode01SupportedPidBitmapDecoder.parsePayload("410080000001", 0x00)
        )
    }

    @Test
    fun parserRejectsWrongResponsePid() {
        assertNull(Mode01SupportedPidBitmapDecoder.parsePayload("41 20 80 00 00 01", 0x00))
    }

    @Test
    fun continuationBitIsLeastSignificantBitOfFourthByte() {
        assertTrue(Mode01SupportedPidBitmapDecoder.hasContinuation(listOf(0x00, 0x00, 0x00, 0x01)))
        assertTrue(!Mode01SupportedPidBitmapDecoder.hasContinuation(listOf(0x00, 0x00, 0x00, 0x00)))
    }
}
