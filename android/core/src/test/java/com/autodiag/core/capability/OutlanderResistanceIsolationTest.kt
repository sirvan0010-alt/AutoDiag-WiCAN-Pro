package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OutlanderResistanceIsolationTest {
    @Test
    fun parserKeepsNormalizedDiagnosticPayloadOnly() {
        val parsed = OutlanderPhev21ResponseParser.parse(
            """
            762 10 08 61 01 00 7B 00 00 00
            762 21 00 00
            """.trimIndent()
        )
        assertEquals(0x00, parsed[0])
        assertEquals(0x7B, parsed[1])
        assertEquals(0x00, parsed[2])
    }

    @Test
    fun parserRejectsUnsupportedIsoTpFrameType() {
        assertFailsWith<IllegalArgumentException> {
            OutlanderPhev21ResponseParser.parse("762 40 00 00")
        }
    }
}
