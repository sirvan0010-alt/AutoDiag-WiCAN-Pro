package com.autodiag.core.capability

/**
 * Converts an ELM327 ISO-TP CAN response into the ordered payload-token shape
 * used by the analysed Watchdog decoder.
 *
 * CAN headers, ISO-TP PCI bytes, and the positive-response service/PID bytes are
 * transport framing and are excluded. Decoder indexes therefore refer to the
 * actual diagnostic payload bytes (d[0], d[1], ...), matching the source model.
 */
object OutlanderPhev21ResponseParser {
    fun parse(normalizedResponse: String): IntArray {
        val lines = normalizedResponse
            .replace('\r', '\n')
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        require(lines.isNotEmpty()) { "Outlander 21 response is empty" }

        val payload = ArrayList<Int>()
        var firstFrame = true

        for (line in lines) {
            val raw = line.split(Regex("\\s+"))
            require(raw.isNotEmpty()) { "Invalid empty Outlander response line" }

            val start = if (raw.first().length > 2) 1 else 0
            val bytes = raw.drop(start).map { token ->
                require(token.matches(Regex("[0-9A-Fa-f]{1,2}"))) {
                    "Invalid hexadecimal byte in Outlander response: $token"
                }
                token.toInt(16)
            }
            require(bytes.isNotEmpty()) { "Outlander response frame has no data" }

            val pci = bytes[0]
            when (pci and 0xF0) {
                0x10 -> {
                    require(firstFrame) { "Unexpected ISO-TP first frame" }
                    require(bytes.size >= 4) { "Incomplete ISO-TP first frame" }
                    payload.addAll(bytes.drop(3))
                    firstFrame = false
                }
                0x00 -> {
                    require(firstFrame) { "Unexpected ISO-TP single frame" }
                    require(bytes.size >= 3) { "Incomplete ISO-TP single frame" }
                    payload.addAll(bytes.drop(3))
                    firstFrame = false
                }
                0x20 -> {
                    require(!firstFrame) { "ISO-TP consecutive frame before first frame" }
                    payload.addAll(bytes.drop(1))
                }
                0x30 -> {
                    // Flow-control frames are adapter/transport traffic, not payload.
                }
                else -> throw IllegalArgumentException("Unsupported ISO-TP PCI byte: ${bytes[0].toString(16)}")
            }
        }

        require(payload.isNotEmpty()) { "Outlander diagnostic payload is empty" }
        return payload.toIntArray()
    }
}
