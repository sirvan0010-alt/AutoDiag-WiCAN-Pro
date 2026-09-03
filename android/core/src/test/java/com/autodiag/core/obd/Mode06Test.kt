package com.autodiag.core.obd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class Mode06Test {
    @Test
    fun request_encodes_obd_monitor_id() {
        assertEquals("0601", ObdMode06Request(0x01).toCommand())
        assertEquals("0600", ObdMode06Request(0x00).toCommand())
    }

    @Test
    fun decodes_multiple_fixed_size_test_records_and_preserves_raw() {
        val report = Mode06Decoder.decode(
            "46 01 07 90 00 0A 00 00 28 00 64 01 8B 12 34 00 20 00 40"
        )
        assertNotNull(report)
        assertEquals(0x01, report!!.obdMid)
        assertEquals(2, report.results.size)
        assertEquals(0x07, report.results[0].testId)
        assertEquals(0x90, report.results[0].unitAndScalingId)
        assertEquals(10, report.results[0].testValueRaw)
        assertEquals(0, report.results[0].minimumRaw)
        assertEquals(40, report.results[0].maximumRaw)
        assertEquals(0x8B, report.results[1].testId)
        assertEquals(0x1234, report.results[1].testValueRaw)
        assertEquals("01 07 90 00 0A 00 00 28 00 64 01 8B 12 34 00 20 00 40", report.rawPayload.joinToString(" ") { "%02X".format(it) })
    }

    @Test
    fun does_not_guess_physical_scaling_or_pass_fail() {
        val report = Mode06Decoder.decode("46 01 07 90 00 0A 00 00 28 00 64")
        assertEquals(Mode06ResultStatus.UNKNOWN, report!!.results.single().status)
    }

    @Test
    fun rejects_incomplete_record() {
        val result = runCatching { Mode06Decoder.decode("46 01 07 90 00 0A") }
        assertEquals(true, result.isFailure)
    }

    @Test
    fun ignores_no_data_and_transport_errors() {
        assertNull(Mode06Decoder.decode("NO DATA"))
        assertNull(Mode06Decoder.decode("CAN ERROR"))
    }
}
