package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals

class OutlanderPhevResistanceTest {
    @Test
    fun isolationResistanceKeepsDecimalPrecisionAndKilohmUnit() {
        val measurement = OutlanderResistanceMeasurement(
            kind = OutlanderResistanceKind.HV_ISOLATION_RESISTANCE,
            value = 123.456,
            timestampEpochMs = 1000L
        )

        assertEquals("kΩ", measurement.unit)
        assertEquals(123.456, measurement.value)
    }

    @Test
    fun internalResistanceUsesMegohmAndRemainsSeparate() {
        val measurement = OutlanderResistanceMeasurement(
            kind = OutlanderResistanceKind.INTERNAL_BATTERY_RESISTANCE,
            value = 1.25,
            timestampEpochMs = 1000L
        )

        assertEquals("MΩ", measurement.unit)
        assertEquals("battery.internal_resistance", measurement.kind.signalId)
    }

    @Test
    fun sessionTracksCurrentMinimumMaximumWithoutIntegerRounding() {
        val kind = OutlanderResistanceKind.HV_ISOLATION_RESISTANCE
        val first = OutlanderResistanceMeasurement(kind, 150.25, 1000L)
        val second = OutlanderResistanceMeasurement(kind, 121.75, 2000L)
        val third = OutlanderResistanceMeasurement(kind, 133.125, 3000L)

        val stats = OutlanderResistanceSessionStats(kind)
            .accept(first)
            .accept(second)
            .accept(third)

        assertEquals(133.125, stats.current)
        assertEquals(121.75, stats.minimum)
        assertEquals(150.25, stats.maximum)
        assertEquals(3, stats.sampleCount)
        assertEquals(3000L, stats.lastTimestampEpochMs)
    }

    @Test
    fun verificationNeverPromotesFromUnverifiedByMeasurementAlone() {
        val kind = OutlanderResistanceKind.HV_ISOLATION_RESISTANCE
        val stats = OutlanderResistanceSessionStats(kind)
            .accept(OutlanderResistanceMeasurement(kind, 120.5, 1000L))

        assertEquals(OutlanderMeasurementVerification.UNVERIFIED, stats.verification)
    }
}
