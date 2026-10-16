package com.autodiag.core.can.mining

import com.autodiag.core.can.CanCaptureRecord
import com.autodiag.core.can.CanCaptureSession
import com.autodiag.core.can.CanFrame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CanCaptureMiningAdapterTest {
    @Test fun mapsCaptureFramesWithoutAliasingPayload() {
        val originalPayload = byteArrayOf(0x12, 0x34)
        val session = CanCaptureSession(
            startedAtNanos = 0L,
            records = listOf(CanCaptureRecord(12_345_678L, CanFrame(0x123, originalPayload))),
        )
        val view = session.toCanFrameViews().single()
        assertEquals(12L, view.timestampMillis)
        assertEquals(0x123L, view.canId)
        assertEquals(2, view.dlc)
        assertTrue(view.payload.contentEquals(byteArrayOf(0x12, 0x34)))
        assertNotSame(originalPayload, view.payload)
    }
}
