package com.autodiag.core.can

/** Canonical, dependency-free CSV export for classic CAN capture sessions. */
object CanCaptureCsv {
    fun encode(session: CanCaptureSession): String = buildString {
        appendLine("timestamp_nanos,id_hex,extended,remote,data_hex")
        session.records.forEach { record ->
            val frame = record.frame
            append(record.timestampNanos)
            append(',')
            append("0x")
            append(frame.id.toString(16).uppercase())
            append(',')
            append(frame.isExtended)
            append(',')
            append(frame.isRemote)
            append(',')
            append(frame.hex().replace(" ", ""))
            appendLine()
        }
    }
}
