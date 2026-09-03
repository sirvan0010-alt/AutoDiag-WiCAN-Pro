package com.autodiag.core.obd

/** Raw OBD-II Mode 06 request for one OBD Monitor ID (OBDMID). */
data class ObdMode06Request(val obdMid: Int) {
    init { require(obdMid in 0..0xFF) }
    fun toCommand(): String = "06%02X".format(obdMid)
}

enum class Mode06ResultStatus {
    WITHIN_LIMITS,
    OUTSIDE_LIMITS,
    UNKNOWN
}

/** One J1979 Mode 06 test record. Scaling is intentionally not guessed. */
data class ObdMode06TestResult(
    val obdMid: Int,
    val testId: Int,
    val unitAndScalingId: Int,
    val testValueRaw: Int,
    val minimumRaw: Int,
    val maximumRaw: Int,
    val status: Mode06ResultStatus,
) {
    init {
        require(obdMid in 0..0xFF)
        require(testId in 0..0xFF)
        require(unitAndScalingId in 0..0xFF)
        require(testValueRaw in 0..0xFFFF)
        require(minimumRaw in 0..0xFFFF)
        require(maximumRaw in 0..0xFFFF)
    }
}

data class ObdMode06Report(
    val obdMid: Int?,
    val results: List<ObdMode06TestResult>,
    val rawPayload: ByteArray,
)

/**
 * Decoder for the CAN/J1979 Mode 06 response record:
 * 46 OBDMID TID UASID TEST(2) MIN(2) MAX(2).
 *
 * A result is marked UNKNOWN by default because UASID scaling/sign rules are
 * required before a physical-value PASS/FAIL can safely be inferred.
 */
object Mode06Decoder {
    fun decode(response: String): ObdMode06Report? {
        val bytes = extractBytes(response) ?: return null
        if (bytes.size < 2 || bytes[0] != 0x46) return null
        val payload = bytes.drop(1)
        val mid = payload[0]
        val data = payload.drop(1)
        require(data.size % 8 == 0) { "Malformed Mode 06 response: incomplete test record" }

        val results = data.chunked(8).map { record ->
            val tid = record[0]
            val uasid = record[1]
            val value = u16(record[2], record[3])
            val min = u16(record[4], record[5])
            val max = u16(record[6], record[7])
            ObdMode06TestResult(mid, tid, uasid, value, min, max, Mode06ResultStatus.UNKNOWN)
        }
        return ObdMode06Report(mid, results, payload.toByteArray())
    }

    private fun u16(high: Int, low: Int): Int = (high shl 8) or low

    fun extractBytes(response: String): List<Int>? {
        val upper = response.uppercase()
        if (upper.contains("NO DATA") || upper.contains("NODATA") ||
            upper.contains("UNABLE TO CONNECT") || upper.contains("CAN ERROR") ||
            upper.contains("BUS INIT") && upper.contains("ERROR")) return null
        val tokens = upper
            .replace(Regex("[^0-9A-F]"), " ")
            .trim().split(Regex("\\s+"))
            .filter { it.length == 2 }
        if (tokens.isEmpty()) return null
        return tokens.mapNotNull { it.toIntOrNull(16) }
    }
}
