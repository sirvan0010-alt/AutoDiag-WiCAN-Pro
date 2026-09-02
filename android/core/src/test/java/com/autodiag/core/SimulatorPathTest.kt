package com.autodiag.core

import com.autodiag.core.capability.CapabilityDiscovery
import com.autodiag.core.capability.CapabilityIds
import com.autodiag.core.capability.CapabilityStatus
import com.autodiag.core.obd.Elm327Session
import com.autodiag.core.obd.Mode01Decoder
import com.autodiag.core.transport.SimulatorWiCanTransport
import com.autodiag.core.transport.TransportConfig
import com.autodiag.core.transport.TransportMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Happy-path without hardware: Simulator → Elm327Session → CapabilityDiscovery.
 * Matches docs/SIMULATOR_TEST_SCENARIOS.md core sequence.
 */
class SimulatorPathTest {

    @Test
    fun simulator_connect_initialize_discover() = runBlocking {
        val transport = SimulatorWiCanTransport()
        transport.connect(
            TransportConfig(host = "simulator", port = 0, mode = TransportMode.SIMULATOR)
        ).getOrThrow()

        val session = Elm327Session(transport)
        session.initialize().getOrThrow()
        assertTrue(session.isInitialized)

        val snap = CapabilityDiscovery().run(session)
        val comm = snap.capabilities[CapabilityIds.COMMUNICATION]
        assertNotNull(comm)
        assertEquals(CapabilityStatus.AVAILABLE, comm!!.status)

        val protocol = snap.capabilities[CapabilityIds.OBD_PROTOCOL]
        assertEquals(CapabilityStatus.AVAILABLE, protocol!!.status)

        val vin = snap.capabilities[CapabilityIds.OBD_VIN]
        assertEquals(CapabilityStatus.AVAILABLE, vin!!.status)
        assertEquals("SIMTEST0AUTODIAG01", vin.detail)
        assertEquals("SIMTEST0AUTODIAG01", snap.vehicleIdentity?.vin)

        val mode01 = snap.capabilities[CapabilityIds.OBD_MODE_01]
        assertEquals(CapabilityStatus.AVAILABLE, mode01!!.status)

        val mode03 = snap.capabilities[CapabilityIds.OBD_MODE_03]
        assertEquals(CapabilityStatus.AVAILABLE, mode03!!.status)

        session.close()
    }

    @Test
    fun mode01_decoder_rpm_from_simulator_payload() {
        val d = Mode01Decoder.decode("41 0C 00 00")
        assertNotNull(d)
        assertEquals(0.0, d!!.value!!, 0.01)
        assertEquals("RPM", d.unit)

        val d2 = Mode01Decoder.decode("41 0C 1A F8")
        assertEquals(1726.0, d2!!.value!!, 0.01)
    }

    @Test
    fun mode01_decoder_no_data_returns_null() {
        assertEquals(null, Mode01Decoder.decode("NO DATA"))
        assertEquals(null, Mode01Decoder.decode(""))
    }
}
