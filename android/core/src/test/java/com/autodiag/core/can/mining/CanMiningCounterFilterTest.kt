package com.autodiag.core.can.mining

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CanMiningCounterFilterTest {
    private val key = CanFrameView.Key(0x123, false, false, 4)

    private fun frames(payloads: List<ByteArray>, startMs: Long = 0, stepMs: Long = 10): List<CanFrameView> =
        payloads.mapIndexed { i, payload ->
            CanFrameView(startMs + i * stepMs, key.canId, key.extended, key.remote, key.dlc, payload)
        }

    private fun observation(frames: List<CanFrameView>, index: Int) =
        CanMiningCounterFilter.analyze(frames).first { it.byteIndex == index }

    @Test fun lowNibbleCounterIsDetected() {
        val payloads = (0 until 32).map { i -> byteArrayOf((0xA0 or (i % 16)).toByte(), 0, 0, 0) }
        val obs = observation(frames(payloads), 0)
        assertEquals(CanMiningCounterFilter.BytePattern.COUNTER_NIBBLE_LO, obs.pattern)
        assertTrue(obs.coverage >= 0.9)
    }

    @Test fun highNibbleCounterIsDetected() {
        val payloads = (0 until 32).map { i -> byteArrayOf((((i % 16) shl 4) or 0x05).toByte(), 0, 0, 0) }
        assertEquals(CanMiningCounterFilter.BytePattern.COUNTER_NIBBLE_HI, observation(frames(payloads), 0).pattern)
    }

    @Test fun constantByteIsConstant() {
        val payloads = (0 until 10).map { byteArrayOf(0x42, 0x11, 0, 0) }
        assertEquals(CanMiningCounterFilter.BytePattern.CONSTANT, observation(frames(payloads), 0).pattern)
        assertEquals(CanMiningCounterFilter.BytePattern.CONSTANT, observation(frames(payloads), 1).pattern)
    }

    @Test fun twoValueFlipIsToggle() {
        val payloads = (0 until 10).map { i -> byteArrayOf((i % 2).toByte(), 0, 0, 0) }
        assertEquals(CanMiningCounterFilter.BytePattern.TOGGLE, observation(frames(payloads), 0).pattern)
    }

    @Test fun randomBytesAreVarying() {
        val random = kotlin.random.Random(7)
        val payloads = (0 until 12).map { byteArrayOf(random.nextInt(256).toByte(), 0, 0, 0) }
        assertEquals(CanMiningCounterFilter.BytePattern.VARYING, observation(frames(payloads), 0).pattern)
    }

    @Test fun stateByteSurvivesCounterExclusion() {
        val payloads = (0 until 12).map { i ->
            byteArrayOf((i % 16).toByte(), (if (i < 10) 0 else 1).toByte(), 0, 0)
        }
        val filtered = CanMiningCounterFilter.excludeLikelyCounterBytes(mapOf(key to setOf(0, 1)), frames(payloads))
        assertFalse(filtered.getValue(key).contains(0))
        assertTrue(filtered.getValue(key).contains(1))
    }

    @Test fun candidateWithOnlyCounterByteIsDropped() {
        val payloads = (0 until 12).map { i -> byteArrayOf((i % 16).toByte(), 0, 0, 0) }
        assertTrue(CanMiningCounterFilter.excludeLikelyCounterBytes(mapOf(key to setOf(0)), frames(payloads)).isEmpty())
    }

    @Test fun tooFewPairsAreNotLabelledCounter() {
        val payloads = listOf(byteArrayOf(0, 0, 0, 0), byteArrayOf(1, 0, 0, 0))
        assertEquals(CanMiningCounterFilter.BytePattern.VARYING, observation(frames(payloads), 0).pattern)
    }

    @Test fun sparseDropsStillAllowCounterHeuristic() {
        val values = mutableListOf<Int>()
        var value = 0
        repeat(20) { i ->
            values.add(value)
            value = (value + 1) % 16
            if (i % 4 == 3) value = (value + 1) % 16
        }
        val payloads = values.map { byteArrayOf(it.toByte(), 0, 0, 0) }
        assertEquals(CanMiningCounterFilter.BytePattern.COUNTER_NIBBLE_LO, observation(frames(payloads), 0).pattern)
    }
}
