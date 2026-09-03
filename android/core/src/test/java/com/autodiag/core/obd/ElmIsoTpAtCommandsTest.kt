package com.autodiag.core.obd

import org.junit.Assert.assertEquals
import org.junit.Test

class ElmIsoTpAtCommandsTest {
    @Test
    fun setupSequenceForStandardObdFunctional() {
        val seq = ElmIsoTpAtCommands.setupSequence(0x7E0)
        assertEquals(
            listOf("ATSH7E0", "ATFCSH7E0", "ATFCSM1", "ATFCSD300000"),
            seq,
        )
    }
}
