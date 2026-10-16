package com.autodiag.core.can.mining

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CanMiningCandidateScorerTest {
    private val eventKey = CanFrameView.Key(0x123, false, false, 4)
    private val periodicKey = CanFrameView.Key(0x200, false, false, 2)

    private fun eventWindow(): List<CanFrameView> {
        val periodic = (0..20).map { i ->
            CanFrameView(i * 10L, periodicKey.canId, false, false, 2,
                byteArrayOf((i % 16).toByte(), 0x00))
        }
        val events = listOf(
            CanFrameView(1_000, eventKey.canId, false, false, 4, byteArrayOf(0, 0, 0, 0)),
            CanFrameView(3_000, eventKey.canId, false, false, 4, byteArrayOf(0, 1, 0, 0)),
        )
        return periodic + events
    }

    private fun candidate(
        key: CanFrameView.Key,
        eventOnly: Boolean,
        changed: Set<Int>,
        novelVariants: Int,
    ) = CanMiningCandidateScorer.DiffCandidate(key, eventOnly, changed, novelVariants, 1, 2, listOf("test://fixture"))

    @Test fun eventDrivenStateChangeOutranksPeriodicCounterNoise() {
        val scored = CanMiningCandidateScorer.score(listOf(
            candidate(periodicKey, false, setOf(0), 0),
            candidate(eventKey, true, setOf(1), 1),
        ), eventWindow())
        assertEquals(eventKey, scored.first().candidate.key)
        assertTrue(scored.first().score > scored.last().score)
    }

    @Test fun counterBytesDoNotCountAsActionEvidence() {
        val scored = CanMiningCandidateScorer.score(
            listOf(candidate(periodicKey, false, setOf(0), 0)), eventWindow()).single()
        assertTrue(scored.explanations.contains("counterBytes=1"))
        assertTrue(scored.explanations.contains("actionBytes=0"))
        assertEquals(0.45 * 0.1, scored.score, 1e-9)
    }

    @Test fun eventOnlyIdentifierReceivesBonus() {
        val scored = CanMiningCandidateScorer.score(
            listOf(candidate(eventKey, true, emptySet(), 0)), eventWindow()).single()
        assertEquals(0.45 + 0.25, scored.score, 1e-9)
        assertTrue(scored.explanations.contains("eventOnlyId"))
    }

    @Test fun noveltyBonusIsCapped() {
        val a = CanMiningCandidateScorer.score(listOf(candidate(eventKey, true, emptySet(), 1)), eventWindow()).single()
        val b = CanMiningCandidateScorer.score(listOf(candidate(eventKey, true, emptySet(), 7)), eventWindow()).single()
        assertEquals(a.score, b.score, 1e-9)
        assertEquals(0.80, a.score, 1e-9)
    }

    @Test fun stateByteAddsPayloadComponent() {
        val scored = CanMiningCandidateScorer.score(
            listOf(candidate(eventKey, true, setOf(1), 1)), eventWindow()).single()
        assertEquals(0.45 + 0.25 + 0.10 + 0.20 * 0.25, scored.score, 1e-9)
    }

    @Test fun noiseFilterKeepsOnlyLowScoreCandidates() {
        val scored = CanMiningCandidateScorer.score(listOf(
            candidate(eventKey, true, setOf(1), 1),
            candidate(periodicKey, false, setOf(0), 0),
        ), eventWindow())
        val noise = CanMiningCandidateScorer.noise(scored)
        assertEquals(1, noise.size)
        assertEquals(periodicKey, noise.single().candidate.key)
    }

    @Test fun scoringIsDeterministicAndSorted() {
        val candidates = listOf(candidate(periodicKey, false, setOf(0), 0), candidate(eventKey, true, setOf(1), 1))
        val first = CanMiningCandidateScorer.score(candidates, eventWindow())
        val second = CanMiningCandidateScorer.score(candidates, eventWindow())
        assertEquals(first, second)
        assertTrue(first.zipWithNext().all { (a, b) -> a.score >= b.score })
    }
}
