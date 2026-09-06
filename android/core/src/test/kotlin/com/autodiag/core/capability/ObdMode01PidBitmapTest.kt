package com.autodiag.core.capability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObdMode01PidBitmapTest {
    @Test
    fun parsesAdvertisedPidsFromSpacedResponse() {
        val blocks = ObdMode01PidBitmap.parse("41 00 18 08 00 01")

        assertEquals(1, blocks.size)
        assertEquals(0, blocks.single().basePid)
        assertTrue(blocks.single().supportedPids.containsAll(setOf(4, 5, 12, 32)))
    }

    @Test
    fun parsesCompactResponse() {
        val blocks = ObdMode01PidBitmap.parse("41001808000001")

        assertEquals(setOf(4, 5, 12, 32), blocks.single().supportedPids)
    }

    @Test
    fun onlyAdvertisedRegistryPidsArePromoted() {
        val advertised = ObdMode01PidBitmap.supportedDecoderPids(
            "41 00 18 08 00 01",
            setOf(4, 5, 12, 13, 32)
        )

        assertEquals(setOf(4, 5, 12, 32), advertised)
    }

    @Test
    fun continuationIsRequiredToAdvertiseNextRange() {
        val first = "41 00 00 00 00 01"
        assertTrue(ObdMode01PidBitmap.advertisesRange(first, 0x20))
        assertTrue(!ObdMode01PidBitmap.advertisesRange(first, 0x40))
    }
}
