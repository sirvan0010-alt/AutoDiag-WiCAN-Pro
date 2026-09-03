package com.autodiag.core.diagnostic

import com.autodiag.core.obd.LiveDataFreshness
import com.autodiag.core.obd.LiveDataQuality
import com.autodiag.core.obd.LiveDataSample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LiveDataEvidenceFactoryTest {
    @Test
    fun goodSampleBecomesAvailableUnverifiedEvidence() {
        val sample = LiveDataSample(
            pid = 0x0C,
            labelCs = "Otáčky motoru",
            value = 1726.0,
            unit = "rpm",
            rawHex = "1A F8",
            timestampEpochMs = 1000L,
            quality = LiveDataQuality.GOOD,
            freshness = LiveDataFreshness.FRESH
        )

        val evidence = LiveDataEvidenceFactory.fromMode01(sample)

        assertEquals("obd.mode01.pid.0C", evidence.key)
        assertEquals(1726.0, evidence.value)
        assertEquals("rpm", evidence.unit)
        assertEquals(EvidenceAvailability.AVAILABLE, evidence.availability)
        assertEquals(EvidenceVerification.UNVERIFIED, evidence.verification)
        assertEquals(EvidenceSource.OBD_MODE_01, evidence.provenance.source)
        assertEquals("1A F8", evidence.provenance.rawRepresentation)
        assertEquals("GOOD:FRESH", evidence.quality)
        assertNull(evidence.note)
    }

    @Test
    fun unavailableSampleRemainsUnavailable() {
        val sample = LiveDataSample(
            pid = 0x0C,
            labelCs = "Otáčky motoru",
            value = null,
            unit = "rpm",
            rawHex = "NO DATA",
            timestampEpochMs = 2000L,
            quality = LiveDataQuality.UNAVAILABLE,
            freshness = LiveDataFreshness.STALE,
            error = "ECU did not provide the PID"
        )

        val evidence = LiveDataEvidenceFactory.fromMode01(sample)

        assertEquals(EvidenceAvailability.UNAVAILABLE, evidence.availability)
        assertNull(evidence.value)
        assertEquals("NO DATA", evidence.provenance.rawRepresentation)
        assertEquals("ECU did not provide the PID", evidence.note)
    }

    @Test
    fun invalidAndErrorSamplesAreNotReportedAsAvailable() {
        val invalid = LiveDataEvidenceFactory.fromMode01(
            LiveDataSample(0x0C, "Otáčky motoru", null, "rpm", "ZZ", 3000L, LiveDataQuality.INVALID, LiveDataFreshness.FRESH)
        )
        val error = LiveDataEvidenceFactory.fromMode01(
            LiveDataSample(0x0C, "Otáčky motoru", null, "rpm", "CAN ERROR", 4000L, LiveDataQuality.ERROR, LiveDataFreshness.FRESH)
        )

        assertEquals(EvidenceAvailability.ERROR, invalid.availability)
        assertEquals(EvidenceAvailability.ERROR, error.availability)
    }

    @Test
    fun staleDoesNotChangeAvailabilityOfLastKnownGoodMeasurement() {
        val evidence = LiveDataEvidenceFactory.fromMode01(
            LiveDataSample(0x0D, "Rychlost vozidla", 50.0, "km/h", "32", 5000L, LiveDataQuality.GOOD, LiveDataFreshness.STALE),
            verification = EvidenceVerification.PARTIALLY_VERIFIED,
            ecuId = "ECU-01",
            sourceId = "adapter-01"
        )

        assertEquals(EvidenceAvailability.AVAILABLE, evidence.availability)
        assertEquals(EvidenceVerification.PARTIALLY_VERIFIED, evidence.verification)
        assertEquals("ECU-01", evidence.provenance.ecuId)
        assertEquals("adapter-01", evidence.provenance.sourceId)
        assertEquals("GOOD:STALE", evidence.quality)
    }
}
