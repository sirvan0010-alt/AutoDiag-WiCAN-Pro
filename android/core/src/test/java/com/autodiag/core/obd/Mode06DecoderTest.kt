package com.autodiag.core.obd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class Mode06DecoderTest {
    @Test
    fun parses_multiple_nine_byte_records_with_repeated_mid() {
        val response = "46 01 01 0A 0E 66 0E 66 0E 66 01 08 0A 1D 70 13 18 22 90"
        val report = Mode06Decoder.decode(response)

        assertNotNull(report)
        assertEquals(0x01, report!!.obdMid)
        assertEquals(2, report.results.size)
        assertEquals(0x01, report.results[0].obdMid)
        assertEquals(0x01, report.results[0].testId)
        assertEquals(0x08, report.results[1].testId)
        assertEquals(0x1D70, report.results[1].testValueRaw)
        assertEquals(0x1318, report.results[1].minimumRaw)
        assertEquals(0x2290, report.results[1].maximumRaw)
    }

    @Test
    fun ignores_elm_echo_before_positive_service() {
        val response = "06\r46 01 08 0A 1D 70 13 18 22 90\r>"
        val report = Mode06Decoder.decode(response)

        assertNotNull(report)
        assertEquals(1, report!!.results.size)
        assertEquals(0x08, report.results.single().testId)
    }

    @Test
    fun malformed_record_is_rejected() {
        assertNull(Mode06Decoder.decode("46 01 08 0A 1D 70 13 18 22"))
    }

    @Test
    fun no_data_is_not_decoded() {
        assertNull(Mode06Decoder.decode("NO DATA"))
    }
}
