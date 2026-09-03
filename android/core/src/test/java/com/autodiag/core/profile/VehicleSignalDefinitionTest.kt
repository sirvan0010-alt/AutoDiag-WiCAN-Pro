package com.autodiag.core.profile

import com.autodiag.core.diagnostic.EvidenceVerification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleSignalDefinitionTest {
    @Test
    fun decodes_big_endian_scaled_signal() {
        val definition = VehicleSignalDefinition(
            name = "Pack voltage",
            shortName = "Vpack",
            request = "22F00D",
            unit = "V",
            byteLength = 2,
            scale = 0.1,
            verification = EvidenceVerification.PARTIALLY_VERIFIED,
        )
        val sample = VehicleSignalDecoder.decode(definition, byteArrayOf(0x01, 0xF4.toByte())).getOrThrow()
        assertEquals(500, sample.rawValue)
        assertEquals(50.0, sample.value, 0.000001)
    }

    @Test
    fun decodes_signed_signal() {
        val definition = VehicleSignalDefinition(
            name = "Signed value",
            shortName = "Signed",
            request = "220001",
            byteLength = 2,
            signed = true,
            scale = 0.01,
        )
        val sample = VehicleSignalDecoder.decode(definition, byteArrayOf(0xFF.toByte(), 0x9C.toByte())).getOrThrow()
        assertEquals(-100, sample.rawValue)
        assertEquals(-1.0, sample.value, 0.000001)
    }

    @Test
    fun decodes_bit_field_without_guessing_other_bits() {
        val definition = VehicleSignalDefinition(
            name = "Drive mode",
            shortName = "Mode",
            request = "22F004",
            bitOffset = 2,
            bitLength = 3,
        )
        val sample = VehicleSignalDecoder.decode(definition, byteArrayOf(0b00111000)).getOrThrow()
        assertEquals(6, sample.rawValue)
        assertTrue(sample.value == 6.0)
    }

    @Test
    fun rejects_short_payload() {
        val definition = VehicleSignalDefinition("x", "x", "22", byteLength = 2)
        assertTrue(VehicleSignalDecoder.decode(definition, byteArrayOf(1)).isFailure)
    }
}
