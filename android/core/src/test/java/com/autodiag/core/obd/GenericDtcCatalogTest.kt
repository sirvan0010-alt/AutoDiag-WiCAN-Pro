package com.autodiag.core.obd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenericDtcCatalogTest {
    @Test
    fun misfireKnown() {
        assertEquals("Výpadek zapalování válec 1", GenericDtcCatalog.titleOrUnknown("P0301"))
    }

    @Test
    fun manufacturerSpecificNotInvented() {
        val t = GenericDtcCatalog.titleOrUnknown("P1ABC")
        assertTrue(t.contains("Výrobně specifický"))
    }
}
