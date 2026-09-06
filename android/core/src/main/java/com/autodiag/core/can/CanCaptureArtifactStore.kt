package com.autodiag.core.can

import java.io.OutputStream

/** Writes a complete capture session as an auditable JSON evidence artifact. */
object CanCaptureArtifactStore {
    const val FILE_EXTENSION = ".json"

    fun write(session: CanCaptureSession, output: OutputStream) {
        output.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(CanCaptureJson.encode(session))
            writer.newLine()
            writer.flush()
        }
    }
}
