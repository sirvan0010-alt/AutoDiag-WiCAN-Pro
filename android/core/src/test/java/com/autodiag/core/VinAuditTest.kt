package com.autodiag.core

import com.autodiag.core.capability.VinResponseParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VinAuditTest {
    @Test
    fun parsesVinPerEcuAndFindsMismatch() {
        val body = """
            7E8 49 02 01 57 56 57 5A 5A 5A 5A 5A 5A 5A 5A 5A 5A 5A
            7EA 49 02 01 57 56 57 41 41 41 41 41 41 41 41 41 41 41
        """.trimIndent()

        val records = VinResponseParser.parse(body)

        assertEquals(2, records.size)
        assertEquals("7E8", records[0].ecuAddress)
        assertEquals("7EA", records[1].ecuAddress)
        assertTrue(records[0].vin != records[1].vin)
    }

    @Test
    fun parsesPlainAsciiVinWithoutCanHeader() {
        val vin = "WVWZZZ1JZXW000001"
        val records = VinResponseParser.parse("$vin")
        assertEquals(1, records.size)
        assertEquals(vin, records.single().vin)
        assertEquals(null, records.single().ecuAddress)
    }
}
