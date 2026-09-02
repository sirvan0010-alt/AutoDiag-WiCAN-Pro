package com.autodiag.wican.core.diagnostics

/** Normalized OBD response independent of ELM327 formatting. */
data class ObdResponse(
    val mode: Int,
    val pid: Int? = null,
    val payload: ByteArray = byteArrayOf(),
    val negativeResponse: NegativeObdResponse? = null
)

data class NegativeObdResponse(
    val requestedService: Int,
    val responseCode: Int
)

object ObdResponseParser {
    /**
     * Parse already-normalized hexadecimal bytes. ASCII parsing belongs in the
     * ELM transport/normalization layer and is intentionally not mixed here.
     */
    fun parse(bytes: ByteArray): ObdResponse? {
        if (bytes.isEmpty()) return null
        val first = bytes[0].toInt() and 0xFF
        if (first == 0x7F && bytes.size >= 3) {
            return ObdResponse(
                mode = bytes[1].toInt() and 0xFF,
                negativeResponse = NegativeObdResponse(
                    requestedService = bytes[1].toInt() and 0xFF,
                    responseCode = bytes[2].toInt() and 0xFF
                )
            )
        }
        val mode = when {
            first in 0x41..0x4A -> first - 0x40
            else -> first
        }
        val pid = if (bytes.size >= 2 && mode in 1..0x0A) bytes[1].toInt() and 0xFF else null
        val payloadStart = if (pid != null) 2 else 1
        return ObdResponse(mode, pid, bytes.copyOfRange(payloadStart, bytes.size))
    }

    fun parseDtcWords(payload: ByteArray): List<String> {
        val result = mutableListOf<String>()
        var i = 0
        while (i + 1 < payload.size) {
            val word = ((payload[i].toInt() and 0xFF) shl 8) or (payload[i + 1].toInt() and 0xFF)
            i += 2
            if (word == 0) continue
            val prefix = when ((word ushr 14) and 0x03) {
                0 -> 'P'; 1 -> 'C'; 2 -> 'B'; else -> 'U'
            }
            result += prefix.toString() + "%04X".format(word and 0x3FFF)
        }
        return result
    }
}
