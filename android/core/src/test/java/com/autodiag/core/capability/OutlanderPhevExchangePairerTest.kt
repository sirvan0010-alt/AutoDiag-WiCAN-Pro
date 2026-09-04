package com.autodiag.core.capability

import com.autodiag.core.can.CanFrame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OutlanderPhevExchangePairerTest {
    @Test
    fun pairsRequestAndResponseForSameEcuWithinWindow() {
        val pairer = OutlanderPhevExchangePairer(responseWindowNanos = 1_000_000)
        val request = observation("bms", OutlanderPhevExchangePairer.Role.REQUEST, 100, "capture-a", 0x100, 0x22, 0x12, 0x34)
        val response = observation("bms", OutlanderPhevExchangePairer.Role.RESPONSE, 500, "capture-a", 0x101, 0x62, 0x12, 0x34, 0x80)

        assertNull(pairer.accept(request))
        val result = pairer.accept(response)
        assertNotNull(result)
        val paired = result as OutlanderPhevExchangePairer.Result.Paired
        assertEquals(request.frame, paired.pair.request)
        assertEquals(response.frame, paired.pair.response)
        assertEquals("capture-a;capture-a", paired.pair.source)
    }

    @Test
    fun doesNotPairDifferentEcus() {
        val pairer = OutlanderPhevExchangePairer()
        pairer.accept(observation("bms", OutlanderPhevExchangePairer.Role.REQUEST, 100, "capture", 0x100, 0x22, 0x12, 0x34))

        val result = pairer.accept(observation("inverter", OutlanderPhevExchangePairer.Role.RESPONSE, 200, "capture", 0x101, 0x62, 0x12, 0x34))
        assertTrue(result is OutlanderPhevExchangePairer.Result.UnmatchedResponse)
    }

    @Test
    fun rejectsResponseOutsideWindowAndRetainsNoExpiredRequestAfterFlush() {
        val pairer = OutlanderPhevExchangePairer(responseWindowNanos = 100)
        val request = observation("bms", OutlanderPhevExchangePairer.Role.REQUEST, 100, "capture", 0x100, 0x22, 0x12, 0x34)
        pairer.accept(request)

        val result = pairer.accept(observation("bms", OutlanderPhevExchangePairer.Role.RESPONSE, 250, "capture", 0x101, 0x62, 0x12, 0x34))
        assertTrue(result is OutlanderPhevExchangePairer.Result.UnmatchedResponse)

        val expired = pairer.flush(300)
        assertEquals(1, expired.size)
        assertEquals(request.frame, expired.single().observation.frame)
    }

    @Test
    fun multipleRequestsAreMatchedInOrder() {
        val pairer = OutlanderPhevExchangePairer(responseWindowNanos = 1_000)
        val first = observation("bms", OutlanderPhevExchangePairer.Role.REQUEST, 100, "capture", 0x100, 0x22, 0x01, 0x00)
        val second = observation("bms", OutlanderPhevExchangePairer.Role.REQUEST, 150, "capture", 0x100, 0x22, 0x02, 0x00)
        pairer.accept(first)
        pairer.accept(second)

        val response = pairer.accept(observation("bms", OutlanderPhevExchangePairer.Role.RESPONSE, 200, "capture", 0x101, 0x62, 0x01, 0x00))
        val paired = response as OutlanderPhevExchangePairer.Result.Paired
        assertEquals(first.frame, paired.pair.request)

        val response2 = pairer.accept(observation("bms", OutlanderPhevExchangePairer.Role.RESPONSE, 250, "capture", 0x101, 0x62, 0x02, 0x00))
        val paired2 = response2 as OutlanderPhevExchangePairer.Result.Paired
        assertEquals(second.frame, paired2.pair.request)
    }

    private fun observation(
        ecuKey: String,
        role: OutlanderPhevExchangePairer.Role,
        timestampNanos: Long,
        source: String,
        id: Long,
        vararg bytes: Int
    ) = OutlanderPhevExchangePairer.Observation(
        ecuKey = ecuKey,
        role = role,
        frame = CanFrame(id = id, data = bytes.map { it.toByte() }.toByteArray(), timestampNanos = timestampNanos),
        source = source
    )
}
