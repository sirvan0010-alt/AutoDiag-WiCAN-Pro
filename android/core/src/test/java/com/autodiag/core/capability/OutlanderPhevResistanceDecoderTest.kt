package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals

class OutlanderPhevResistanceDecoderTest {
    @Test
    fun isolationResistanceUsesUnsignedBytes78And79InKilohm() {
        val response = IntArray(80)
        response[78] = 0x01
        response[79] = 0xF4

        assertEquals(500.0, OutlanderPhevResistanceDecoder.decodeIsolationResistance(response))
    }

    @Test
    fun internalResistanceUsesWatchdogBytes38And39InMegohm() {
        val response = IntArray(40)
        response[38] = 15
        response[39] = 9

        assertEquals(1.5, OutlanderPhevResistanceDecoder.decodeMaximumInternalResistance(response))
        assertEquals(0.9, OutlanderPhevResistanceDecoder.decodeMinimumInternalResistance(response))
    }

    @Test
    fun isolationMeasurementIsPartiallyVerifiedUntilVehicleEvidenceConfirmsDecoder() {
        val response = IntArray(80)
        response[78] = 0x00
        response[79] = 0x7B

        val measurement = OutlanderPhevResistanceDecoder.decodeIsolationMeasurement(
            response = response,
            timestampEpochMs = 1234L,
            rawResponse = "21 01 ... 00 7B"
        )

        assertEquals(123.0, measurement.value)
        assertEquals("kΩ", measurement.unit)
        assertEquals(OutlanderMeasurementVerification.PARTIALLY_VERIFIED, measurement.verification)
        assertEquals("21 01", measurement.rawRequest)
    }
}
