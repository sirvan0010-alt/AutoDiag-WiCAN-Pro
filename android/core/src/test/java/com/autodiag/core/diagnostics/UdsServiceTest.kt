package com.autodiag.core.diagnostics

import com.autodiag.core.diagnostics.uds.UdsService
import com.autodiag.core.diagnostics.uds.UdsServiceRisk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UdsServiceTest {
    @Test
    fun readAndWriteServicesAreClassifiedSeparately() {
        assertEquals(UdsServiceRisk.READ, UdsService.READ_DATA_BY_IDENTIFIER.risk)
        assertEquals(UdsServiceRisk.CONFIG_WRITE, UdsService.WRITE_DATA_BY_IDENTIFIER.risk)
        assertEquals(UdsServiceRisk.SECURITY_CRITICAL, UdsService.SECURITY_ACCESS.risk)
        assertEquals(UdsServiceRisk.PROGRAMMING, UdsService.REQUEST_DOWNLOAD.risk)
    }

    @Test
    fun longCodingStyleConfigurationBelongsToWriteCategory() {
        assertEquals(UdsServiceRisk.CONFIG_WRITE, UdsService.WRITE_DATA_BY_IDENTIFIER.risk)
        assertTrue(UdsService.WRITE_DATA_BY_IDENTIFIER.serviceId == 0x2E)
    }

    @Test
    fun unknownServiceIsConservative() {
        assertEquals(UdsService.UNKNOWN, UdsService.fromServiceId(0x99))
        assertEquals(UdsServiceRisk.UNKNOWN, UdsService.fromServiceId(0x99).risk)
    }

    @Test
    fun positiveResponseIdIsDerivedFromRequest() {
        assertEquals(0x62, UdsService.positiveResponseServiceId(0x22))
        assertTrue(UdsService.isPositiveResponse(0x62, 0x22))
    }
}
