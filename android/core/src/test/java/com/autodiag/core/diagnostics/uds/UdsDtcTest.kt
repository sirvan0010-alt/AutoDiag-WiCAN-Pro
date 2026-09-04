package com.autodiag.core.diagnostics.uds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UdsDtcTest {
    @Test fun requestEncodesReadDtcInformation() {
        assertTrue(UdsReadDtcInformationRequest(0x02).toPayload().contentEquals(byteArrayOf(0x19, 0x02)))
    }

    @Test fun parsesMultipleDtcRecordsAndPreservesStatus() {
        val response = UdsPositiveResponse(
            serviceId = 0x59,
            payload = byteArrayOf(
                0x02, 0xFF.toByte(),
                0x01, 0x23, 0x45, 0x28,
                0x0A, 0xBC.toByte(), 0xDE.toByte(), 0x01,
            ),
        )
        val report = UdsReadDtcInformationParser.parse(response).getOrThrow()
        assertEquals(0x02, report.subFunction)
        assertEquals(0xFF, report.statusAvailabilityMask)
        assertEquals(2, report.dtcs.size)
        assertEquals(0x012345, report.dtcs[0].code)
        assertEquals(0x28, report.dtcs[0].statusByte)
        assertEquals(0x0ABCDE, report.dtcs[1].code)
    }

    @Test fun rejectsTruncatedDtcRecord() {
        val response = UdsPositiveResponse(0x59, byteArrayOf(0x02, 0xFF.toByte(), 0x01, 0x23, 0x45))
        assertTrue(UdsReadDtcInformationParser.parse(response).isFailure)
    }

    @Test fun negativeResponseRemainsNegative() {
        val response = UdsNegativeResponse(0x19, 0x12)
        assertTrue(UdsReadDtcInformationParser.parse(response).isFailure)
    }
}
