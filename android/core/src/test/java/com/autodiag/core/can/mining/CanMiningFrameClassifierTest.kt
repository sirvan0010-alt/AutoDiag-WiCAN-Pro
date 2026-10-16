package com.autodiag.core.can.mining

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CanMiningFrameClassifierTest {
    private val key = CanFrameView.Key(0x123, false, false, 2)

    private fun periodicFrames(intervalMs: Long, count: Int, startMs: Long = 0): List<CanFrameView> =
        (0 until count).map { i ->
            CanFrameView(startMs + i * intervalMs, key.canId, key.extended, key.remote, key.dlc, byteArrayOf(0, 0))
        }

    @Test fun regularFastStreamIsPeriodic() {
        val stats = CanMiningFrameClassifier.classify(periodicFrames(10, 20))
        assertEquals(CanMiningFrameClassifier.FrameClass.PERIODIC, stats.getValue(key).frameClass)
    }

    @Test fun twoFramesFarApartAreEvent() {
        val frames = listOf(
            CanFrameView(0, key.canId, false, false, 2, byteArrayOf(0, 0)),
            CanFrameView(2_000, key.canId, false, false, 2, byteArrayOf(0, 0)),
        )
        assertEquals(CanMiningFrameClassifier.FrameClass.EVENT, CanMiningFrameClassifier.classify(frames).getValue(key).frameClass)
    }

    @Test fun singleFrameIsEvent() {
        val frames = listOf(CanFrameView(0, key.canId, false, false, 2, byteArrayOf(0, 0)))
        assertEquals(CanMiningFrameClassifier.FrameClass.EVENT, CanMiningFrameClassifier.classify(frames).getValue(key).frameClass)
    }

    @Test fun captureLossGapDoesNotBreakPeriodicClassification() {
        val frames = periodicFrames(10, 20) + CanFrameView(10_000, key.canId, false, false, 2, byteArrayOf(0, 0))
        assertEquals(CanMiningFrameClassifier.FrameClass.PERIODIC, CanMiningFrameClassifier.classify(frames).getValue(key).frameClass)
    }

    @Test fun insufficientFastFramesAreMixed() {
        assertEquals(CanMiningFrameClassifier.FrameClass.MIXED, CanMiningFrameClassifier.classify(periodicFrames(10, 4)).getValue(key).frameClass)
    }

    @Test fun irregularMediumRateIsMixed() {
        val offsets = listOf(0L, 80, 210, 260, 480, 530, 700, 860, 900, 1200)
        val frames = offsets.map { CanFrameView(it, key.canId, false, false, 2, byteArrayOf(0, 0)) }
        assertEquals(CanMiningFrameClassifier.FrameClass.MIXED, CanMiningFrameClassifier.classify(frames).getValue(key).frameClass)
    }

    @Test fun identifiersAreClassifiedIndependently() {
        val other = CanFrameView.Key(0x456, false, false, 1)
        val frames = periodicFrames(10, 20) + CanFrameView(0, other.canId, false, false, 1, byteArrayOf(0xFF.toByte()))
        val stats = CanMiningFrameClassifier.classify(frames)
        assertEquals(CanMiningFrameClassifier.FrameClass.PERIODIC, stats.getValue(key).frameClass)
        assertEquals(CanMiningFrameClassifier.FrameClass.EVENT, stats.getValue(other).frameClass)
    }

    @Test fun unsortedInputIsDeterministic() {
        val sorted = periodicFrames(10, 20)
        val a = CanMiningFrameClassifier.classify(sorted).getValue(key)
        val b = CanMiningFrameClassifier.classify(sorted.shuffled(kotlin.random.Random(42))).getValue(key)
        assertEquals(a.frameClass, b.frameClass)
        assertEquals(a.medianIntervalMillis, b.medianIntervalMillis, 0.0)
    }

    @Test fun exposesStatisticsForScorer() {
        val stats = CanMiningFrameClassifier.classify(periodicFrames(10, 20)).getValue(key)
        assertEquals(20, stats.frameCount)
        assertTrue(stats.intervalCv.isFinite())
    }
}
