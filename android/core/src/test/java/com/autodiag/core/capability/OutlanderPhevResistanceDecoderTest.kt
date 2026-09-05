package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals

class OutlanderPhevResistanceDecoderTest {
    @Test
    fun isolationResistanceUsesUnsignedBytes78And79InKilohm() {
        val response = IntArray(80)
        response[78] = 0x01
        response[79] = 0xF4
        val definition = SignalDecoderDefinition(
            signalId = "battery.isolation_resistance",
            label = "HV isolation resistance",
            request = "21 01",
            variantId = "watchdog.lz3a.21_01",
            decoder = DataDecoderSpec(
                kind = DataDecoderSpec.Kind.UNSIGNED_U16_BE,
                start = 78,
                end = 79,
                unit = "kΩ"
            )
        )
        assertEquals(500.0, OutlanderPhevResistanceDecoder.decode(definition, response))
    }

    @Test
    fun internalResistanceCandidateUsesWatchdogBytes38And39InMegohm() {
        val response = IntArray(40)
        response[38] = 15
        response[39] = 9
        val definition = SignalDecoderDefinition(
            signalId = "battery.internal_resistance.max",
            label = "Maximum internal resistance candidate",
            request = "21 01",
            variantId = "watchdog.le4a.21_01",
            decoder = DataDecoderSpec(
                kind = DataDecoderSpec.Kind.UNSIGNED_U8,
                start = 38,
                scale = 0.1,
                unit = "MΩ"
            )
        )
        assertEquals(1.5, OutlanderPhevResistanceDecoder.decode(definition, response))
    }

    @Test
    fun internalResistanceLd4UsesBigEndianPairs() {
        val response = IntArray(72)
        response[12] = 0x01
        response[13] = 0xF4
        response[14] = 0x00
        response[15] = 0xFA
        response[71] = 25

        val max = SignalDecoderDefinition(
            signalId = "battery.internal_resistance.max",
            label = "Maximum internal resistance candidate",
            request = "21 01",
            variantId = "watchdog.ld4a.21_01",
            decoder = DataDecoderSpec(
                kind = DataDecoderSpec.Kind.UNSIGNED_U16_BE,
                start = 12,
                end = 13,
                scale = 0.001,
                unit = "MΩ"
            )
        )
        val min = max.copy(
            signalId = "battery.internal_resistance.min",
            decoder = max.decoder.copy(start = 14, end = 15)
        )
        val diff = max.copy(
            signalId = "battery.max_internal_resistance_difference",
            decoder = DataDecoderSpec(
                kind = DataDecoderSpec.Kind.UNSIGNED_U8,
                start = 71,
                scale = 0.02,
                unit = "MΩ"
            )
        )

        assertEquals(0.5, OutlanderPhevResistanceDecoder.decode(max, response))
        assertEquals(0.25, OutlanderPhevResistanceDecoder.decode(min, response))
        assertEquals(0.5, OutlanderPhevResistanceDecoder.decode(diff, response))
    }
}
