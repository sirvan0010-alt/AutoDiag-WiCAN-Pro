package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals

class OutlanderPhev21ResponseParserTest {
    @Test
    fun retainsThreeCharacterCanHeaderAsOneToken() {
        val response = buildString {
            append("7E8")
            repeat(77) { append(" 00") }
            append(" 01 F4")
        }

        val parsed = OutlanderPhev21ResponseParser.parse(response)

        assertEquals(80, parsed.size)
        assertEquals(0x7E8, parsed[0])
        assertEquals(0x01, parsed[78])
        assertEquals(0xF4, parsed[79])
        assertEquals(500.0, OutlanderPhevResistanceDecoder.decodeIsolationResistance(parsed))
    }

    @Test
    fun acceptsMultilineElmResponse() {
        val response = "7E8 00 01\n02 03 04"

        val parsed = OutlanderPhev21ResponseParser.parse(response)

        assertEquals(listOf(0x7E8, 0, 1, 2, 3, 4), parsed.toList())
    }
}
