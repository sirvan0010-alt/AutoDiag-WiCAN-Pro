package com.autodiag.core.obd

/** Registry-driven decoder for standardized OBD-II Mode 01 PID responses. */
object Mode01Decoder {
    data class DecodedPid(
        val mode: Int,
        val pid: Int,
        val rawHex: String,
        val value: Double?,
        val unit: String?,
        val labelCs: String
    )

    fun decode(response: String): DecodedPid? {
        val detailed = decodeDetailed(response) ?: return null
        return DecodedPid(detailed.mode, detailed.pid, detailed.rawHex, detailed.value, detailed.unit, detailed.labelCs)
    }

    fun decodeDetailed(response: String): ObdPidResult? {
        val bytes = extractDataBytes(response) ?: return null
        if (bytes.size < 2 || bytes[0] != 0x41) return null
        val pid = bytes[1]
        val data = bytes.drop(2)
        val def = ObdPidRegistry.get(pid)
        if (def == null) {
            return ObdPidResult(0x01, pid, response.trim(), null, null, "PID 0x%02X".format(pid), ObdValueAvailability.UNKNOWN_PID, null)
        }
        val value = if (data.size < def.minimumBytes) null else def.decodeValue(data)
        return ObdPidResult(0x01, pid, response.trim(), value, def.unit, def.labelCs,
            if (value != null) ObdValueAvailability.AVAILABLE else ObdValueAvailability.UNAVAILABLE, def)
    }

    fun extractDataBytes(response: String): List<Int>? {
        val upper = response.uppercase().replace("SEARCHING...", "").trim()
        if (upper.isEmpty()) return null
        if (upper.contains("NO DATA") || upper.contains("NODATA") ||
            upper.contains("UNABLE TO CONNECT") || upper.contains("NOT CONNECTED") ||
            (upper.contains("BUS INIT") && upper.contains("ERROR")) ||
            (upper.contains("?") && upper.replace(Regex("[^0-9A-F?]"), "").length < 8)) return null
        val lines = upper.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith(">") }
        val line = lines.firstOrNull { it.replace(" ", "").startsWith("41") } ?: lines.lastOrNull() ?: return null
        val hex = line.replace(Regex("[^0-9A-F]"), " ").trim().split(Regex("\\s+")).filter { it.length == 2 }
        if (hex.isEmpty()) return null
        return hex.mapNotNull { it.toIntOrNull(16) }
    }
}
