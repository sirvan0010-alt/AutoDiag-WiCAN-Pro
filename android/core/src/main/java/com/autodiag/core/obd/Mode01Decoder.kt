package com.autodiag.core.obd

/**
 * Decodes standard Mode 01 PID responses (SAE J1979).
 * Never invents values: returns null when payload is missing or malformed.
 */
object Mode01Decoder {

    data class DecodedPid(
        val mode: Int,
        val pid: Int,
        val rawHex: String,
        val value: Double?,
        val unit: String?,
        val labelCs: String
    )

    /**
     * @param response full ELM body, e.g. "41 0C 1A F8" or multi-line with headers
     */
    fun decode(response: String): DecodedPid? {
        val bytes = extractDataBytes(response) ?: return null
        if (bytes.size < 2) return null
        // Expect 41 <pid> <data...>
        if (bytes[0] != 0x41) return null
        val pid = bytes[1]
        val data = bytes.drop(2)
        return when (pid) {
            0x0C -> { // RPM: ((A*256)+B)/4
                if (data.size < 2) DecodedPid(0x01, pid, response.trim(), null, "RPM", "Otáčky motoru")
                else {
                    val rpm = ((data[0] shl 8) + data[1]) / 4.0
                    DecodedPid(0x01, pid, response.trim(), rpm, "RPM", "Otáčky motoru")
                }
            }
            0x0D -> { // Vehicle speed km/h
                if (data.isEmpty()) DecodedPid(0x01, pid, response.trim(), null, "km/h", "Rychlost")
                else DecodedPid(0x01, pid, response.trim(), data[0].toDouble(), "km/h", "Rychlost")
            }
            0x05 -> { // Coolant °C = A - 40
                if (data.isEmpty()) DecodedPid(0x01, pid, response.trim(), null, "°C", "Teplota chladicí kapaliny")
                else DecodedPid(0x01, pid, response.trim(), (data[0] - 40).toDouble(), "°C", "Teplota chladicí kapaliny")
            }
            0x0B -> { // MAP kPa
                if (data.isEmpty()) DecodedPid(0x01, pid, response.trim(), null, "kPa", "Absolutní tlak v sacím potrubí")
                else DecodedPid(0x01, pid, response.trim(), data[0].toDouble(), "kPa", "Absolutní tlak v sacím potrubí")
            }
            else -> DecodedPid(0x01, pid, response.trim(), null, null, "PID 0x%02X".format(pid))
        }
    }

    /** Pull hex data bytes from ELM response; ignores SEARCHING..., headers if ATH1. */
    fun extractDataBytes(response: String): List<Int>? {
        val upper = response.uppercase()
            .replace("SEARCHING...", "")
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith(">") }
        val line = upper.firstOrNull { it.replace(" ", "").startsWith("41") }
            ?: upper.lastOrNull()
            ?: return null
        val hex = line.replace(Regex("[^0-9A-F]"), " ")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.length == 2 }
        if (hex.isEmpty()) return null
        return hex.mapNotNull { it.toIntOrNull(16) }
    }
}
