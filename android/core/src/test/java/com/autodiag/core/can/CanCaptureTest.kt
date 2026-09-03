package com.autodiag.core.can

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CanCaptureTest {
    private fun frame(id: Long = 0x123, value: Int = 1) = CanFrame(id, byteArrayOf(value.toByte()))

    @Test fun does_not_record_before_start() {
        val capture = CanCapture(clockNanos = { 100L })
        assertFalse(capture.record(frame()))
        assertNull(capture.stop())
    }

    @Test fun records_relative_monotonic_timestamps() {
        val capture = CanCapture(clockNanos = { 1_000L })
        capture.start()
        capture.record(frame(), 1_250L)
        capture.record(frame(value = 2), 2_000L)
        val session = capture.stop()!!
        assertEquals(2, session.frameCount)
        assertEquals(250L, session.records[0].timestampNanos)
        assertEquals(1_000L, session.records[1].timestampNanos)
        assertEquals(250L, session.records[0].frame.timestampNanos)
    }

    @Test fun bounded_capture_drops_oldest_and_counts_drops() {
        val capture = CanCapture(maxRecords = 2, clockNanos = { 0L })
        capture.start()
        capture.record(frame(value = 1), 1L)
        capture.record(frame(value = 2), 2L)
        capture.record(frame(value = 3), 3L)
        val session = capture.stop()!!
        assertEquals(2, session.frameCount)
        assertEquals(1L, session.droppedRecords)
        assertEquals(2, session.records[0].frame.data[0].toInt())
        assertEquals(3, session.records[1].frame.data[0].toInt())
    }

    @Test fun stop_is_idempotent_after_capture_ends() {
        val capture = CanCapture(clockNanos = { 0L })
        capture.start()
        capture.record(frame(), 1L)
        assertTrue(capture.stop() != null)
        assertNull(capture.stop())
    }
}
