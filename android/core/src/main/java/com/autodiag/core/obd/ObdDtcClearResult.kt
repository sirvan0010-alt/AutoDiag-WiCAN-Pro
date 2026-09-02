package com.autodiag.core.obd

enum class DtcClearResult {
    CLEARED,
    NO_RESPONSE,
    REJECTED,
    INVALID_RESPONSE,
    UNKNOWN
}

/** Parses the positive OBD response to Mode 04 without treating a transport failure as success. */
object ObdDtcClearResultParser {
    fun parse(response: ByteArray?): DtcClearResult {
        if (response == null || response.isEmpty()) return DtcClearResult.NO_RESPONSE
        val first = response[0].toInt() and 0xFF
        return when (first) {
            0x44 -> DtcClearResult.CLEARED
            0x7F -> DtcClearResult.REJECTED
            else -> DtcClearResult.INVALID_RESPONSE
        }
    }
}
