package com.autodiag.core

import com.autodiag.core.obd.DtcClearResult
import com.autodiag.core.obd.ObdDtcClearResultParser
import org.junit.Assert.assertEquals
import org.junit.Test

class ObdDtcClearResultTest {
    @Test
    fun positiveMode04ResponseMeansCleared() {
        assertEquals(DtcClearResult.CLEARED, ObdDtcClearResultParser.parse(byteArrayOf(0x44)))
    }

    @Test
    fun negativeResponseIsRejected() {
        assertEquals(DtcClearResult.REJECTED, ObdDtcClearResultParser.parse(byteArrayOf(0x7F, 0x04, 0x11)))
    }

    @Test
    fun missingResponseIsNotSuccess() {
        assertEquals(DtcClearResult.NO_RESPONSE, ObdDtcClearResultParser.parse(null))
    }
}
