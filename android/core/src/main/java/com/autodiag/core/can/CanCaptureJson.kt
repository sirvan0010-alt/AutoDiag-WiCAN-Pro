package com.autodiag.core.can

import org.json.JSONArray
import org.json.JSONObject

/** Deterministic JSON representation of a captured CAN session for evidence artifacts. */
object CanCaptureJson {
    const val FORMAT = "autodiag-can-capture-v1"

    fun encode(session: CanCaptureSession): String {
        val root = JSONObject()
            .put("format", FORMAT)
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

    fun decode(json: String): CanCaptureSession {
        val root = JSONObject(json)
        require(root.optString("format") == FORMAT) { "Unsupported CAN capture format" }
        val recordsJson = root.optJSONArray("records") ?: JSONArray()
        val records = buildList(recordsJson.length()) {
            for (index in 0 until recordsJson.length()) {
                val item = recordsJson.getJSONObject(index)
                val hex = item.optString("data")
                require(hex.length % 2 == 0) { "Invalid CAN payload hex" }
                val data = ByteArray(hex.length / 2) { offset ->
                    hex.substring(offset * 2, offset * 2 + 2).toInt(16).toByte()
                }
                add(
                    CanCaptureRecord(
                        timestampNanos = item.getLong("timestampNanos"),
                        frame = CanFrame(
                            id = item.getLong("canId"),
                            data = data,
                            timestampNanos = item.getLong("timestampNanos"),
                            isExtended = item.optBoolean("extended", false),
                            isRemote = item.optBoolean("remote", false)
                        )
                    )
                )
            }
        }
        require(root.optInt("frameCount", records.size) == records.size) { "Capture frame count mismatch" }
        return CanCaptureSession(
            startedAtNanos = root.getLong("startedAtNanos"),
            records = records,
            droppedRecords = root.optLong("droppedRecords", 0L)
        )
    }
}
