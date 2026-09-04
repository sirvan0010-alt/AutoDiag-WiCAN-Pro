package com.autodiag.core.obd

/** Raw OBD-II Mode 06 request for one OBD Monitor ID (OBDMID). */
data class ObdMode06Request(val obdMid: Int) {
    init { require(obdMid in 0..0xFF) }
    fun toCommand(): String = "06%02X".format(obdMid)
}

enum class Mode06ResultStatus { WITHIN_LIMITS, OUTSIDE_LIMITS, UNKNOWN }

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

/** Decoder for J1979 Mode 06: 46 [MID TID UASID TEST(2) MIN(2) MAX(2)]*. */
object Mode06Decoder {
    fun decode(response: String): ObdMode06Report? {
        val bytes = extractBytes(response) ?: return null
        val serviceIndex = bytes.indexOf(0x46)
        if (serviceIndex < 0) return null
        val payload = bytes.drop(serviceIndex + 1)
        if (payload.isEmpty() || payload.size % 9 != 0) return null

        val results = payload.chunked(9).map { record ->
            val mid = record[0]
            val tid = record[1]
            val uasid = record[2]
            val value = u16(record[3], record[4])
            val min = u16(record[5], record[6])
            val max = u16(record[7], record[8])
            ObdMode06TestResult(mid, tid, uasid, value, min, max, Mode06ResultStatus.UNKNOWN)
        }
        return ObdMode06Report(results.first().obdMid, results, payload.map { it.toByte() }.toByteArray())
    }

    private fun u16(high: Int, low: Int): Int = (high shl 8) or low

    fun extractBytes(response: String): List<Int>? {
        val upper = response.uppercase()
        if (upper.contains("NO DATA") || upper.contains("NODATA") ||
            upper.contains("UNABLE TO CONNECT") || upper.contains("CAN ERROR") ||
            (upper.contains("BUS INIT") && upper.contains("ERROR"))) return null
        val tokens = upper
            .replace(Regex("[^0-9A-F]"), " ")
            .trim().split(Regex("\\s+"))
            .filter { it.length == 2 }
        if (tokens.isEmpty()) return null
        return tokens.mapNotNull { it.toIntOrNull(16) }
    }
}
