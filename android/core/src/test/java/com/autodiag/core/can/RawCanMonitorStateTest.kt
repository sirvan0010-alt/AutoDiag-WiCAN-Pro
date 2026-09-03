package com.autodiag.core.can

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RawCanMonitorStateTest {
    private fun frame(id: Long) = CanFrame(id, byteArrayOf(0x01, 0x02))

    @Test fun filtersExactHexId() {
        val state = RawCanMonitorState(idFilter = "0x123")
        assertTrue(state.accepts(frame(0x123)))
        assertFalse(state.accepts(frame(0x124)))
    }

    @Test fun retainsRollingWindow() {
        var state = RawCanMonitorState(maxFrames = 2)
        state = state.onFrame(frame(1)).onFrame(frame(2)).onFrame(frame(3))
        assertEquals(listOf(2L, 3L), state.frames.map { it.id })
        assertEquals(3, state.stats.receivedFrames)
    }

    @Test fun pauseStopsDisplayButKeepsCounters() {
        var state = RawCanMonitorState(paused = true)
        state = state.onFrame(frame(1))
        assertTrue(state.frames.isEmpty())
        assertEquals(1, state.stats.receivedFrames)
    }
}
