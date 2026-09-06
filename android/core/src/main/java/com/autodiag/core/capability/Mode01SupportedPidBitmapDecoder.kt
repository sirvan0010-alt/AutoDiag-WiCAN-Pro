package com.autodiag.core.capability

/** Pure parser for SAE Mode 01 supported-PID bitmap responses. */
object Mode01SupportedPidBitmapDecoder {
    /** Extracts four bitmap bytes from plain or CAN-header-prefixed 41 <PID> output. */
    fun parsePayload(response: String, requestedPid: Int): List<Int>? {
        require(requestedPid in 0..0xE0 && requestedPid % 0x20 == 0) {
            "requestedPid must be a supported-PID page boundary"
        }
        val marker = "41%02X".format(requestedPid)
        response.uppercase()
            .replace("SEARCHING...", "")
            .lineSequence()
            .map(String::trim)
            .filter { it.isNotEmpty() && !it.startsWith(">") }
            .forEach { line ->
                val tokens = line.split(Regex("[^0-9A-F]+"))
                    .filter { it.isNotEmpty() }
                val markerIndex = tokens.indexOfFirst { it == marker }
                if (markerIndex >= 0) {
                    val payload = tokens.drop(markerIndex + 1).take(4).mapNotNull { it.toIntOrNull(16) }
                    if (payload.size == 4) return payload
                }
                val compact = line.replace(Regex("[^0-9A-F]"), "")
                val match = Regex(marker + "([0-9A-F]{8})").find(compact) ?: return@forEach
                return match.groupValues[1].chunked(2).map { it.toInt(16) }
            }
        return null
    }

    fun supportedPids(bitmap: List<Int>, basePid: Int): Set<Int> {
        if (bitmap.size < 4 || basePid !in 0..0xE0 || basePid % 0x20 != 0) return emptySet()
        val value = (bitmap[0].toLong() shl 24) or
            (bitmap[1].toLong() shl 16) or (bitmap[2].toLong() shl 8) or bitmap[3].toLong()
        return (0 until 32)
            .filter { bit -> (value and (1L shl (31 - bit))) != 0L }
            .mapTo(linkedSetOf()) { basePid + bit + 1 }
    }

    fun hasContinuation(bitmap: List<Int>): Boolean =
        bitmap.size >= 4 && (bitmap[3] and 0x01) != 0
}
