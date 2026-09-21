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

    private val hexRun = Regex("[0-9A-F]+")

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

    /**
     * Extracts a Mode 01 positive response from common ELM327 text layouts.
     *
     * Supports both spaced and contiguous byte streams (e.g. `41 0C 1A F8`
     * and `410C1AF8`) and tolerates CAN/ELM headers before the `41` response.
     * It deliberately does not infer values from arbitrary non-hex text.
     */
    fun extractDataBytes(response: String): List<Int>? {
        val upper = response.uppercase().replace("SEARCHING...", "").trim()
        if (upper.isEmpty()) return null
        if (upper.contains("NO DATA") || upper.contains("NODATA") ||
            upper.contains("UNABLE TO CONNECT") || upper.contains("NOT CONNECTED") ||
            (upper.contains("BUS INIT") && upper.contains("ERROR")) ||
            (upper.contains("?") && upper.replace(Regex("[^0-9A-F?]"), "").length < 8)) return null

        for (line in upper.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith(">") }) {
            val bytes = hexRun.findAll(line)
                .flatMap { run -> run.value.chunked(2).asSequence() }
                .mapNotNull { it.takeIf { byte -> byte.length == 2 }?.toIntOrNull(16) }
                .toList()
            val responseStart = bytes.indexOf(0x41)
            if (responseStart >= 0 && bytes.size > responseStart + 1) {
                return bytes.drop(responseStart)
            }
        }
        return null
    }
}
