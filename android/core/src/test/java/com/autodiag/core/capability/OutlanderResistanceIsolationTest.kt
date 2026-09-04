package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OutlanderResistanceIsolationTest {
    @Test
    fun `keeps decimal precision and tracks session min max`() {
        val aggregator = OutlanderResistanceIsolationSessionAggregator(
            OutlanderResistanceMeasurement.HV_ISOLATION_RESISTANCE,
            "kOhm"
        )

        aggregator.add(sample(125.4, 1000L))
        val state = aggregator.add(sample(119.75, 2000L))

        assertEquals(119.75, state.current)
        assertEquals(119.75, state.sessionMin)
        assertEquals(125.4, state.sessionMax)
        assertEquals(2, state.sampleCount)
    }

    @Test
    fun `does not invent a safety limit`() {
        val aggregator = OutlanderResistanceIsolationSessionAggregator(
            OutlanderResistanceMeasurement.HV_ISOLATION_RESISTANCE,
            "kOhm"
        )
        val state = aggregator.add(sample(123.456, 1000L))

        assertEquals("MEASURED_LIMIT_UNKNOWN", state.status())
        assertEquals(123.456, state.current)
    }

    @Test
    fun `rejects non matching measurement type`() {
        val aggregator = OutlanderResistanceIsolationSessionAggregator(
            OutlanderResistanceMeasurement.HV_ISOLATION_RESISTANCE,
            "kOhm"
        )

        assertFailsWith<IllegalArgumentException> {
            aggregator.add(
                OutlanderResistanceIsolationSample(
                    OutlanderResistanceMeasurement.INTERNAL_BATTERY_RESISTANCE,
                    1.2,
                    1000L,
                    unit = "MOhm"
                )
            )
        }
    }

    private fun sample(value: Double, timestamp: Long) =
        OutlanderResistanceIsolationSample(
            measurement = OutlanderResistanceMeasurement.HV_ISOLATION_RESISTANCE,
            value = value,
            unit = "kOhm",
            timestampEpochMs = timestamp,
            verification = OutlanderVerification.PARTIALLY_VERIFIED
        )
}
