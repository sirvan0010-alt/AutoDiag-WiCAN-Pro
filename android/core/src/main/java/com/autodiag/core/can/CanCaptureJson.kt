package com.autodiag.core.can

import org.json.JSONArray
import org.json.JSONObject

/** Deterministic JSON representation of a captured CAN session for evidence artifacts. */
object CanCaptureJson {
    fun encode(session: CanCaptureSession): String {
        val root = JSONObject()
            .put("format", "autodiag-can-capture-v1")
            .put("startedAtNanos", session.startedAtNanos)
            .put("droppedRecords", session.droppedRecords)
            .put("frameCount", session.frameCount)
        val records = JSONArray()
        session.records.forEach { record ->
            val frame = record.frame
            records.put(
                JSONObject()
                    .put("timestampNanos", record.timestampNanos)
                    .put("canId", frame.id)
                    .put("extended", frame.isExtended)
                    .put("remote", frame.isRemote)
                    .put("data", frame.data.joinToString("") { "%02X".format(it.toInt() and 0xFF) })
            )
        }
        return root.put("records", records).toString()
    }
}
