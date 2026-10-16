package com.autodiag.core.can

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CanMiningDiffTest {
    private fun record(time: Long, id: Long, vararg bytes: Int, extended: Boolean = false) =
        CanCaptureRecord(time, CanFrame(id = id, data = bytes.map { it.toByte() }.toByteArray(), isExtended = extended, timestampNanos = time))

    @Test
    fun reports_event_only_identifier_without_claiming_signal_semantics() {
        val session = CanCaptureSession(
            startedAtNanos = 0L,
            records = listOf(record(10, 0x100, 0x01), record(20, 0x100, 0x01), record(110, 0x200, 0xA5, 0x5A)),
        )
        val report = CanMiningDiff.analyze(session, CanMiningWindow(0, 50), CanMiningWindow(100, 150))
        assertEquals(2, report.baselineFrameCount)
        assertEquals(1, report.eventFrameCount)
        assertEquals(1, report.candidates.size)
        assertEquals(0x200L, report.candidates.single().canId)
        assertTrue(report.candidates.single().reasons.any { it.contains("only in event") })
    }

    @Test
    fun reports_byte_positions_and_new_payload_variants_for_known_identifier() {
        val session = CanCaptureSession(
            startedAtNanos = 0L,
            records = listOf(record(10, 0x123, 0x00, 0x10), record(20, 0x123, 0x00, 0x10), record(110, 0x123, 0x01, 0x10)),
        )
        val report = CanMiningDiff.analyze(session, CanMiningWindow(0, 50), CanMiningWindow(100, 150))
        val candidate = report.candidates.single()
        assertEquals(listOf(0), candidate.changedBytePositions)
        assertEquals(listOf("0110"), candidate.eventOnlyPayloads)
        assertTrue(candidate.reasons.any { it.contains("byte-value set changed") })
    }

    @Test
    fun ignores_identical_payload_sets_and_excludes_frames_outside_windows() {
        val session = CanCaptureSession(
            startedAtNanos = 0L,
            records = listOf(record(10, 0x123, 0x00), record(110, 0x123, 0x00), record(500, 0x999, 0xFF)),
        )
        val report = CanMiningDiff.analyze(session, CanMiningWindow(0, 50), CanMiningWindow(100, 150))
        assertTrue(report.candidates.isEmpty())
        assertEquals(1, report.eventFrameCount)
        assertFalse(report.candidates.any { it.canId == 0x999L })
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejects_overlapping_windows() {
        CanMiningDiff.analyze(
            CanCaptureSession(0L, emptyList()),
            CanMiningWindow(0, 100),
            CanMiningWindow(50, 150)
        )
    }

    @Test
    fun enriches_candidates_with_timing_class_and_counter_heuristics() {
        val baseline = listOf(
            record(10_000_000, 0x123, 0x00, 0xAA),
            record(20_000_000, 0x123, 0x00, 0xAA),
            record(30_000_000, 0x123, 0x00, 0xAA),
        )
        val event = (0..4).map { index ->
            record(110_000_000L + index * 10_000_000L, 0x123, 0x10 + index, 0xAA)
        }
        val session = CanCaptureSession(startedAtNanos = 0L, records = baseline + event)

        val report = CanMiningDiff.analyze(
            session,
            CanMiningWindow(0L, 50_000_000L),
            CanMiningWindow(100_000_000L, 160_000_000L),
        )

        val candidate = report.candidates.single()
        assertEquals("PERIODIC", candidate.trafficClass)
        assertEquals(listOf(0), candidate.likelyCounterBytePositions)
        assertEquals(listOf(0), candidate.changedBytePositions)
    }

}
