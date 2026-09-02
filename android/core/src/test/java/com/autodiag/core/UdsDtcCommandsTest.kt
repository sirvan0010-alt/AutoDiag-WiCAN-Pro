package com.autodiag.core

import com.autodiag.core.obd.DtcMemory
import com.autodiag.core.obd.DtcProtocol
import com.autodiag.core.obd.UdsDtcClearResponse
import com.autodiag.core.obd.UdsDtcClearResult
import com.autodiag.core.obd.UdsDtcDecoder
import com.autodiag.core.obd.UdsDtcRequestBuilder
import com.autodiag.core.obd.UdsDtcSubFunction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UdsDtcCommandsTest {
    @Test
    fun report_by_status_mask_uses_19_02() {
        assertTrue(UdsDtcRequestBuilder.reportByStatusMask(0xFF).contentEquals(byteArrayOf(0x19, 0x02, 0xFF.toByte())))
        assertEquals(0x01, UdsDtcSubFunction.REPORT_NUMBER_BY_STATUS_MASK)
        assertEquals(0x02, UdsDtcSubFunction.REPORT_DTC_BY_STATUS_MASK)
    }

    @Test
    fun clear_request_encodes_three_byte_group() {
        assertTrue(UdsDtcRequestBuilder.clearRequest(0x123456).contentEquals(byteArrayOf(0x14, 0x12, 0x34, 0x56)))
    }

    @Test
    fun decoder_preserves_raw_three_byte_uds_identifier_and_status() {
        val response = byteArrayOf(0x59, 0x02, 0x01, 0xAB.toByte(), 0xCD.toByte(), 0xA8.toByte())
        val result = UdsDtcDecoder.decode(response, ecuAddress = 0x7E0)

        assertEquals(1, result.size)
        val dtc = result.single()
        assertEquals("DTC01ABCD", dtc.code)
        assertEquals(DtcMemory.UNKNOWN, dtc.memory)
        assertEquals(DtcProtocol.UDS_19, dtc.protocol)
        assertEquals(0xA8, dtc.statusMask)
        assertEquals(0x7E0, dtc.ecuAddress)
        assertTrue(dtc.rawBytes.contentEquals(byteArrayOf(0x01, 0xAB.toByte(), 0xCD.toByte())))
    }

    @Test
    fun clear_response_distinguishes_success_rejection_and_no_response() {
        assertEquals(UdsDtcClearResult.CLEARED, UdsDtcClearResponse.classify(byteArrayOf(0x54)))
        assertEquals(UdsDtcClearResult.REJECTED, UdsDtcClearResponse.classify(byteArrayOf(0x7F, 0x14, 0x31)))
        assertEquals(UdsDtcClearResult.NO_RESPONSE, UdsDtcClearResponse.classify(null))
        assertEquals(UdsDtcClearResult.INVALID_RESPONSE, UdsDtcClearResponse.classify(byteArrayOf(0x55)))
    }
}
