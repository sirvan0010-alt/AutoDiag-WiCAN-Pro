package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals

class OutlanderPhevWatchdogCandidateDecoderTest {
    @Test
    fun decodesCellVoltageGroupTokenUsingWatchdogScale() {
        val tokens = IntArray(32)
        tokens[0] = 190
        tokens[31] = 205

        assertEquals(
            3.8,
            DataDrivenDecoder.decode(
                tokens,
                DataDecoderSpec(DataDecoderSpec.Kind.UNSIGNED_U8, 0, scale = 0.02)
            )
        )
        assertEquals(
            4.1,
            DataDrivenDecoder.decode(
                tokens,
                DataDecoderSpec(DataDecoderSpec.Kind.UNSIGNED_U8, 31, scale = 0.02)
            )
        )
    }

    @Test
    fun decodesFrontMotorRpmAsLittleEndian() {
        val tokens = IntArray(32)
        tokens[30] = 0x34
        tokens[31] = 0x12

        assertEquals(
            0x1234.toDouble(),
            DataDrivenDecoder.decode(
                tokens,
                DataDecoderSpec(DataDecoderSpec.Kind.UNSIGNED_U16_LE, 30, 31)
            )
        )
    }

    @Test
    fun decodesGeneratorRpmUsingExactNonContiguousWatchdogIndices() {
        val tokens = IntArray(30)
        tokens[29] = 0x12
        tokens[26] = 0x34

        assertEquals(
            0x1234.toDouble(),
            DataDrivenDecoder.decode(
                tokens,
                DataDecoderSpec(
                    kind = DataDecoderSpec.Kind.UNSIGNED_U16_BE,
                    start = 29,
                    end = 26,
                    indices = listOf(29, 26)
                )
            )
        )
    }

    @Test
    fun decodesWatchdog2105La4VariantAsUnsignedU16BeDividedByTen() {
        val tokens = IntArray(52)
        tokens[50] = 0x01
        tokens[51] = 0xF4

        assertEquals(
            50.0,
            DataDrivenDecoder.decode(
                tokens,
                DataDecoderSpec(
                    kind = DataDecoderSpec.Kind.UNSIGNED_U16_BE,
                    start = 50,
                    end = 51,
                    scale = 0.1
                )
            )
        )
    }

    @Test
    fun decodesWatchdog2105Lb4VariantAsUnsignedU16BeDividedByTen() {
        val tokens = IntArray(49)
        tokens[47] = 0x02
        tokens[48] = 0x58

        assertEquals(
            60.0,
            DataDrivenDecoder.decode(
                tokens,
                DataDecoderSpec(
                    kind = DataDecoderSpec.Kind.UNSIGNED_U16_BE,
                    start = 47,
                    end = 48,
                    scale = 0.1
                )
            )
        )
    }
}
