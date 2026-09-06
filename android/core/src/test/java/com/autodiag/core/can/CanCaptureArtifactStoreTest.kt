package com.autodiag.core.can

import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertContains

class CanCaptureArtifactStoreTest {
    @Test
    fun write_creates_replayable_json_artifact() {
        val session = CanCaptureSession(
            startedAtNanos = 100L,
            records = listOf(
                CanCaptureRecord(
                    timestampNanos = 20L,
                    frame = CanFrame(id = 0x762, data = byteArrayOf(0x21, 0x01))
                )
            )
        )
        val output = ByteArrayOutputStream()

        CanCaptureArtifactStore.write(session, output)
        val artifact = output.toString(Charsets.UTF_8.name())

        assertContains(artifact, "autodiag-can-capture-v1")
        assertContains(artifact, "\"canId\":1890")
        assertContains(artifact, "\"data\":\"2101\"")
    }
}
