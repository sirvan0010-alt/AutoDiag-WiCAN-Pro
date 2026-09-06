package com.autodiag.core.can

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.toList
import kotlin.test.Test
import kotlin.test.assertEquals

class CanReplayArtifactTest {
    @Test
    fun persisted_capture_can_be_decoded_and_replayed() = runTest {
        val original = CanCaptureSession(
            startedAtNanos = 1_000L,
            records = listOf(
                CanCaptureRecord(0L, CanFrame(id = 0x762, data = byteArrayOf(0x21, 0x01))),
                CanCaptureRecord(1_000_000L, CanFrame(id = 0x762, data = byteArrayOf(0x61, 0x01)))
            )
        )
        val restored = CanCaptureJson.decode(CanCaptureJson.encode(original))

        val replayed = CanReplay.flow(restored, speed = 1000.0).toList()

        assertEquals(restored.records.map { it.frame }, replayed)
    }
}
