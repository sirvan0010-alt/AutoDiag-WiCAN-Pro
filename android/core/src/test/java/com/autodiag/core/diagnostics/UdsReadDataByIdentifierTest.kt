package com.autodiag.core.diagnostics

import com.autodiag.core.diagnostics.uds.UdsDidValue
import com.autodiag.core.diagnostics.uds.UdsPositiveResponse
import com.autodiag.core.diagnostics.uds.UdsReadDataByIdentifierParser
import com.autodiag.core.diagnostics.uds.UdsReadDataByIdentifierRequest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UdsReadDataByIdentifierTest {
    @Test
    fun requestEncodesReadOnlyDid() {
        assertArrayEquals(
            byteArrayOf(0x22, 0xF1.toByte(), 0x90.toByte()),
            UdsReadDataByIdentifierRequest(0xF190).toPayload(),
        )
    }

    @Test
    fun parserPreservesRawDidValue() {
        val result = UdsReadDataByIdentifierParser.parse(
            0xF190,
            UdsPositiveResponse(0x62, byteArrayOf(0xF1.toByte(), 0x90.toByte(), 'T'.code.toByte(), 'E'.code.toByte(), 'S'.code.toByte(), 'T'.code.toByte())),
        ).getOrThrow()
        assertEquals(UdsDidValue(0xF190, byteArrayOf('T'.code.toByte(), 'E'.code.toByte(), 'S'.code.toByte(), 'T'.code.toByte())), result)
    }

    @Test
    fun parserRejectsUnexpectedDid() {
        val result = UdsReadDataByIdentifierParser.parse(
            0xF190,
            UdsPositiveResponse(0x62, byteArrayOf(0xF1.toByte(), 0x91.toByte(), 0x01)),
        )
        assertTrue(result.isFailure)
    }
}
