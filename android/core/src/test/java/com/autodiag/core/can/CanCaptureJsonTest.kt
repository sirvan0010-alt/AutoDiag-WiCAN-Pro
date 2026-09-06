package com.autodiag.core.can

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class CanCaptureJsonTest {
    @Test
    fun encode_preserves_capture_metadata_and_frame_evidence() {
        val frame = CanFrame(
            id = 0x123,
            data = byteArrayOf(0x2A, 0x55),
            isExtended = false,
            isRemote = false,
            timestampNanos = 10_000L
        )
        val session = CanCaptureSession(
            startedAtNanos = 9_000L,
            records = listOf(CanCaptureRecord(1_000L, frame)),
            droppedRecords = 2
        )

        val json = CanCaptureJson.encode(session)

        assertContains(json, "autodiag-can-capture-v1")
        assertContains(json, "\"startedAtNanos\":9000")
        assertContains(json, "\"droppedRecords\":2")
        assertContains(json, "\"canId\":291")
        assertContains(json, "\"data\":\"2A55\"")
        assertEquals(1, session.frameCount)
    }
}
