package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OutlanderPhev21ResponseParserTest {
    @Test
    fun stripsCanHeaderIsoTpAndPositiveResponsePrefix() {
        val response = """
            762 10 0D 61 01 82 83 0F 8B
            762 21 24 0F 88 03 0C 6E 52
        """.trimIndent()

        val parsed = OutlanderPhev21ResponseParser.parse(response)

        assertEquals(listOf(0x82, 0x83, 0x0F, 0x8B, 0x24, 0x0F, 0x88, 0x03, 0x0C, 0x6E, 0x52), parsed.toList())
    }

    @Test
    fun acceptsMultilineIsoTpPayloadWhenDeclaredLengthMatches() {
        val response = """
            7E8 10 05 61 01 AA BB CC
        """.trimIndent()

        val parsed = OutlanderPhev21ResponseParser.parse(response)

        assertEquals(listOf(0xAA, 0xBB, 0xCC), parsed.toList())
    }

    @Test
    fun preservesThirtyTwo21_04OutputPositionsAfterTransportAndServiceHeaders() {
        val response = """
            762 10 22 61 04 BE BF C0 C1
            762 21 C2 C3 C4 C5 C6 C7 C8
            762 22 C9 CA CB CC CD CE CF
            762 23 D0 D1 D2 D3 D4 D5 D6
            762 24 D7 D8 D9 DA DB DC DD
        """.trimIndent()

        val parsed = OutlanderPhev21ResponseParser.parse(response)

        assertEquals(32, parsed.size)
        assertEquals(0xBE, parsed[0])
        assertEquals(0xCD, parsed[15])
        assertEquals(0xDD, parsed[31])
    }

    @Test
    fun rejectsIncompleteIsoTpFirstFrame() {
        assertFailsWith<IllegalArgumentException> {
            OutlanderPhev21ResponseParser.parse("762 10 37 61")
        }
    }

    @Test
    fun rejectsTruncatedConsecutiveFrameSequence() {
        val response = """
            762 10 37 61 01 00 01 02 03
            762 21 04 05 06 07 08 09 0A
        """.trimIndent()

        val error = assertFailsWith<OutlanderPhev21ResponseParser.IsoTpLengthMismatchException> {
            OutlanderPhev21ResponseParser.parse(response)
        }

        assertEquals(53, error.declaredPayloadSize)
        assertEquals(10, error.actualPayloadSize)
    }
}
