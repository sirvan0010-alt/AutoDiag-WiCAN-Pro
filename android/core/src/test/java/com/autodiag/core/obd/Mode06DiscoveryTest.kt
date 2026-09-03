package com.autodiag.core.obd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Mode06DiscoveryTest {
    @Test
    fun decodes_supported_mids_and_next_window() {
        // Bits 31, 30 and 0 => MIDs 01, 02 and 20; bit 0 announces 20-window.
        val window = Mode06DiscoveryDecoder.decode("06 00\r46 00 C0 00 00 01\r>", 0x00)!!
        assertEquals(listOf(0x01, 0x02, 0x20), window.supportedMids)
        assertTrue(window.hasNextWindow)
        assertEquals(0x20, Mode06DiscoveryDecoder.nextBase(window))
    }

    @Test
    fun next_window_is_planned_only_when_advertised() {
        val window = Mode06DiscoveryDecoder.decode("46 20 00 00 00 00", 0x20)!!
        assertEquals(emptyList<Int>(), window.supportedMids)
        assertNull(Mode06DiscoveryPlanner.nextRequest(window))
    }

    @Test
    fun discovery_ignores_elm_echo_and_prompt() {
        val window = Mode06DiscoveryDecoder.decode("06 40\r46 40 00 00 00 08\r>", 0x40)!!
        assertEquals(listOf(0x5C), window.supportedMids)
    }

    @Test
    fun wrong_base_and_truncated_response_are_rejected() {
        assertNull(Mode06DiscoveryDecoder.decode("46 20 00 00 00 01", 0x00))
        assertNull(Mode06DiscoveryDecoder.decode("46 00 00 00", 0x00))
    }
}
