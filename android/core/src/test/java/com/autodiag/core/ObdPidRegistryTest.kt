package com.autodiag.core

import com.autodiag.core.obd.Mode01Decoder
import com.autodiag.core.obd.ObdPidRegistry
import com.autodiag.core.obd.ObdValueAvailability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ObdPidRegistryTest {
    @Test fun rpmMatchesPreviousBehavior() {
        assertEquals(0.0, Mode01Decoder.decode("41 0C 00 00")!!.value!!, 0.01)
        assertEquals(1726.0, Mode01Decoder.decode("41 0C 1A F8")!!.value!!, 0.01)
    }
    @Test fun coolantTemperatureUsesMinus40() {
        val d = Mode01Decoder.decode("41 05 5A")!!
        assertEquals(50.0, d.value!!, 0.01)
        assertEquals("°C", d.unit)
    }
    @Test fun throttlePositionScalesToPercent() {
        assertEquals(100.0, Mode01Decoder.decode("41 11 FF")!!.value!!, 0.1)
    }
    @Test fun controlModuleVoltageUsesMillivoltScale() {
        assertEquals(12.786, Mode01Decoder.decode("41 42 31 F2")!!.value!!, 0.001)
    }
    @Test fun unknownPidIsExplicit() {
        val d = Mode01Decoder.decodeDetailed("41 99 AA")!!
        assertNull(d.value)
        assertEquals(ObdValueAvailability.UNKNOWN_PID, d.availability)
    }
    @Test fun insufficientPayloadIsUnavailable() {
        val d = Mode01Decoder.decodeDetailed("41 0C 1A")!!
        assertNull(d.value)
        assertEquals(ObdValueAvailability.UNAVAILABLE, d.availability)
    }
    @Test fun noDataReturnsNull() {
        assertNull(Mode01Decoder.decode("NO DATA"))
        assertNull(Mode01Decoder.decodeDetailed("NO DATA"))
    }
    @Test fun registryContainsExpectedCoverage() {
        assertTrue(ObdPidRegistry.definitions.size >= 16)
        assertTrue(ObdPidRegistry.isSupported(0x0C))
        assertFalse(ObdPidRegistry.isSupported(0xEE))
        assertNotNull(ObdPidRegistry.get(0x42))
    }
}
