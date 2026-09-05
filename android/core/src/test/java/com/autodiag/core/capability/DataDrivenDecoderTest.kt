package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals

class DataDrivenDecoderTest {
    @Test
    fun decodesUnsignedU16BigEndianWithScale() {
        val tokens = IntArray(80)
        tokens[78] = 0x01
        tokens[79] = 0xF4
        val value = DataDrivenDecoder.decode(
            tokens,
            DataDecoderSpec(DataDecoderSpec.Kind.UNSIGNED_U16_BE, 78, 79, 1.0)
        )
        assertEquals(500.0, value)
    }

    @Test
    fun decodesUnsignedByteWithScaleAndOffset() {
        val tokens = IntArray(40)
        tokens[38] = 25
        val value = DataDrivenDecoder.decode(
            tokens,
            DataDecoderSpec(DataDecoderSpec.Kind.UNSIGNED_U8, 38, scale = 0.1, offset = 1.0)
        )
        assertEquals(3.5, value)
    }

    @Test
    fun decodesSignedLittleEndian16Bit() {
        val tokens = intArrayOf(0xFE, 0xFF)
        val value = DataDrivenDecoder.decode(
            tokens,
            DataDecoderSpec(DataDecoderSpec.Kind.SIGNED_I16_LE, 0, 1)
        )
        assertEquals(-2.0, value)
    }
}
