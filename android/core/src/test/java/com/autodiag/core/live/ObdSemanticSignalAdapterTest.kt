package com.autodiag.core.live

import com.autodiag.core.obd.LiveDataFreshness
import com.autodiag.core.obd.LiveDataQuality
import com.autodiag.core.obd.LiveDataSample
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ObdSemanticSignalAdapterTest {
    @Test
    fun registeredStandardPidBecomesSemanticSignal() {
        val signal = ObdSemanticSignalAdapter.toSemanticSignal(
            LiveDataSample(
                pid = 0x0C,
                labelCs = "Otáčky motoru",
                value = 1500.0,
                unit = "rpm",
                rawHex = "05DC",
                timestampEpochMs = 100_000L,
                quality = LiveDataQuality.GOOD,
                freshness = LiveDataFreshness.FRESH
            )
        )

        requireNotNull(signal)
        assertEquals("engine.rpm", signal.id)
        assertEquals(1500.0, signal.value)
        assertEquals("rpm", signal.unit)
        assertEquals(100_000L, signal.timestampMs)
        assertEquals("OBD_J1979_MODE_01", signal.source)
    }

    @Test
    fun unknownPidIsNotPromoted() {
        val signal = ObdSemanticSignalAdapter.toSemanticSignal(
            LiveDataSample(
                pid = 0x99,
                labelCs = "unknown",
                value = 1.0,
                unit = null,
                rawHex = "01",
                timestampEpochMs = 100_000L,
                quality = LiveDataQuality.GOOD,
                freshness = LiveDataFreshness.FRESH
            )
        )
        assertNull(signal)
    }

    @Test
    fun invalidQualityOrTimestampIsNotPromoted() {
        val invalidQuality = ObdSemanticSignalAdapter.toSemanticSignal(
            LiveDataSample(0x0C, "RPM", 1500.0, "rpm", "05DC", 100_000L, LiveDataQuality.INVALID, LiveDataFreshness.STALE)
        )
        val invalidTimestamp = ObdSemanticSignalAdapter.toSemanticSignal(
            LiveDataSample(0x0C, "RPM", 1500.0, "rpm", "05DC", 0L, LiveDataQuality.GOOD, LiveDataFreshness.FRESH)
        )

        assertNull(invalidQuality)
        assertNull(invalidTimestamp)
    }
}
