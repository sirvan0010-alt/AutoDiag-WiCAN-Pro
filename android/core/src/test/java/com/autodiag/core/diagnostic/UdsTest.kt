package com.autodiag.core.diagnostic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UdsTest {
    @Test fun positiveResponseIsCorrelatedByService() {
        val request = Uds.Request(0x22, byteArrayOf(0xBC.toByte(), 0x03))
        val response = Uds.parse(request, byteArrayOf(0x62, 0xBC.toByte(), 0x03, 0x12))
        assertTrue(response.positive)
        assertEquals(0x62, response.responseService)
    }

    @Test fun responsePendingDoesNotFinishRequest() {
        val request = Uds.Request(0x22, byteArrayOf(0x01, 0x02))
        val response = Uds.parse(request, byteArrayOf(0x7F, 0x22, 0x78))
        assertFalse(response.positive)
        assertTrue(response.pending)
        assertEquals(0x78, response.negativeResponseCode)
    }

    @Test fun mismatchedPositiveServiceFails() {
        val request = Uds.Request(0x22)
        val result = runCatching { Uds.parse(request, byteArrayOf(0x63)) }
        assertTrue(result.isFailure)
    }
}
