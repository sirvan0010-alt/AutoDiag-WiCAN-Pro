package com.autodiag.core

import com.autodiag.core.contribution.VehicleDataAnonymizer
import org.junit.Assert.*
import org.junit.Test

class VehicleDataAnonymizerTest {
    @Test fun keepsOnlyNonIdentifyingVinPrefix() {
        val scope = VehicleDataAnonymizer.vehicleScopeFrom("WVWZZZ1JZXW000001")
        assertEquals("WVWZZZ1JZX", scope!!.wmiVdsModelYear)
        assertEquals(10, scope.wmiVdsModelYear.length)
        assertFalse(scope.wmiVdsModelYear.contains("W000001"))
    }

    @Test fun rejectsMalformedVinInsteadOfGuessing() {
        assertNull(VehicleDataAnonymizer.vehicleScopeFrom("TOOSHORT"))
        assertNull(VehicleDataAnonymizer.vehicleScopeFrom(null))
        assertNull(VehicleDataAnonymizer.vehicleScopeFrom(""))
    }

    @Test fun freeTextIsAlwaysDropped() {
        assertNull(VehicleDataAnonymizer.redactFreeText("moje SPZ 1AB2345 a jméno Jan Novák"))
        assertNull(VehicleDataAnonymizer.redactFreeText(null))
    }

    @Test fun bucketsTimestampToMonthOnly() {
        val instant = java.time.ZonedDateTime.of(2026, 9, 2, 12, 0, 0, 0, java.time.ZoneOffset.UTC).toInstant()
        assertEquals("2026-09", VehicleDataAnonymizer.monthBucket(instant.toEpochMilli()))
    }
}
