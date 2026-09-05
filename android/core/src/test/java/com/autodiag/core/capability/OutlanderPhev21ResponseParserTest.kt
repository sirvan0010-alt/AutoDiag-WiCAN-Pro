package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OutlanderPhev21ResponseParserTest {
    @Test
    fun stripsCanHeaderIsoTpAndPositiveResponsePrefix() {
        val response = """
            762 10 37 61 01 82 83 0F 8B
            762 21 24 0F 88 03 0C 6E 52
        """.trimIndent()

        val parsed = OutlanderPhev21ResponseParser.parse(response)

        assertEquals(listOf(0x82, 0x83, 0x0F, 0x8B, 0x24, 0x0F, 0x88, 0x03, 0x0C, 0x6E, 0x52), parsed.toList())
    }

    @Test
    fun acceptsMultilineIsoTpPayload() {
        val response = """
            7E8 10 05 61 01 AA BB CC
            7E8 21 DD
        """.trimIndent()

        val parsed = OutlanderPhev21ResponseParser.parse(response)

        assertEquals(listOf(0xAA, 0xBB, 0xCC, 0xDD), parsed.toList())
    }

    @Test
    fun rejectsIncompleteIsoTpFirstFrame() {
        assertFailsWith<IllegalArgumentException> {
            OutlanderPhev21ResponseParser.parse("762 10 37 61")
        }
    }
}
