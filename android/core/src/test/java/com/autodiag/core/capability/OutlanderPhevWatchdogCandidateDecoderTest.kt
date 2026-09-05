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
}
