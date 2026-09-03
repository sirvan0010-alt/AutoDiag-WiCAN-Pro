package com.autodiag.core.live

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LiveDataHistoryTest {
    @Test
    fun keeps_bounded_time_order_and_extrema() {
        val history = LiveDataHistory("rpm", capacity = 3)
        history.append(1000, 1200.0)
        history.append(1100, 1800.0)
        history.append(1200, 1500.0)
        history.append(1300, 2100.0)

        val window = history.snapshot()
        assertEquals(listOf(1100L, 1200L, 1300L), window.points.map { it.timestampEpochMs })
        assertEquals(1500.0, window.minimum)
        assertEquals(2100.0, window.maximum)
    }

    @Test
    fun empty_store_has_no_fake_value() {
        val window = LiveDataHistoryStore().snapshot("unknown")
        assertEquals(emptyList<LiveDataHistoryPoint>(), window.points)
        assertNull(window.minimum)
        assertNull(window.maximum)
    }
}
