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
}
