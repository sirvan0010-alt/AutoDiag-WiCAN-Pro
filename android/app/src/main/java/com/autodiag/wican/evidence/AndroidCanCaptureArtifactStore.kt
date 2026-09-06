package com.autodiag.wican.evidence

import android.content.Context
import com.autodiag.core.can.CanCaptureArtifactStore
import com.autodiag.core.can.CanCaptureSession
import java.io.File
import java.util.UUID

/** Persists capture artifacts inside app-private storage without exposing CAN data externally. */
class AndroidCanCaptureArtifactStore(private val context: Context) {
    private val directory: File
        get() = File(context.filesDir, "can-captures").apply { mkdirs() }

    fun save(session: CanCaptureSession): File {
        val file = File(directory, "capture-${UUID.randomUUID()}${CanCaptureArtifactStore.FILE_EXTENSION}")
        file.outputStream().use { output -> CanCaptureArtifactStore.write(session, output) }
        return file
    }
}
