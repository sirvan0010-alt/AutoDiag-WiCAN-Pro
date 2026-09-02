package com.autodiag.core.obd

/** UDS 0x19 sub-functions used to inspect ECU DTC memory. */
object UdsDtcSubFunction {
    const val REPORT_NUMBER_BY_STATUS_MASK = 0x02
    const val REPORT_DTC_BY_STATUS_MASK = 0x02
    const val REPORT_DTC_SNAPSHOT_BY_DTC = 0x04
    const val REPORT_DTC_EXTENDED_DATA_BY_DTC = 0x06
}

/** UDS DTC status bits as reported by ReadDTCInformation. */
data class UdsDtcStatus(val mask: Int) {
    init { require(mask in 0..0xFF) }
    val testFailed: Boolean get() = mask and 0x01 != 0
    val testFailedThisOperationCycle: Boolean get() = mask and 0x02 != 0
    val pendingDtc: Boolean get() = mask and 0x04 != 0
    val confirmedDtc: Boolean get() = mask and 0x08 != 0
    val testNotCompletedSinceLastClear: Boolean get() = mask and 0x10 != 0
    val testFailedSinceLastClear: Boolean get() = mask and 0x20 != 0
    val testNotCompletedThisOperationCycle: Boolean get() = mask and 0x40 != 0
    val warningIndicatorRequested: Boolean get() = mask and 0x80 != 0
}

/** Builds UDS 0x19 requests. ECU-specific addressing and transport are supplied by the caller. */
object UdsDtcRequestBuilder {
    fun reportByStatusMask(statusMask: Int = 0xFF): ByteArray {
        require(statusMask in 0..0xFF)
        return byteArrayOf(0x19, UdsDtcSubFunction.REPORT_DTC_BY_STATUS_MASK.toByte(), statusMask.toByte())
    }

    fun clearRequest(groupOfDtc: Int = 0xFFFFFF): ByteArray {
        require(groupOfDtc in 0..0xFFFFFF)
        return byteArrayOf(
            0x14,
            ((groupOfDtc ushr 16) and 0xFF).toByte(),
            ((groupOfDtc ushr 8) and 0xFF).toByte(),
            (groupOfDtc and 0xFF).toByte()
        )
    }
}

/** Decodes the common UDS 0x19 response containing DTC + status records. */
object UdsDtcDecoder {
    fun decode(response: ByteArray, ecuAddress: Int? = null): List<DiagnosticTroubleCode> {
        if (response.size < 3 || (response[0].toInt() and 0xFF) != 0x59) return emptyList()
        val subFunction = response[1].toInt() and 0xFF
        if (subFunction != UdsDtcSubFunction.REPORT_DTC_BY_STATUS_MASK) return emptyList()
        val result = mutableListOf<DiagnosticTroubleCode>()
        var i = 3
        while (i + 3 < response.size) {
            val b1 = response[i].toInt() and 0xFF
            val b2 = response[i + 1].toInt() and 0xFF
            val b3 = response[i + 2].toInt() and 0xFF
            val status = response[i + 3].toInt() and 0xFF
            i += 4
            val code = "U${b1.toString(16).padStart(2, '0')}${b2.toString(16).padStart(2, '0')}${b3.toString(16).padStart(2, '0')}".uppercase()
            result += DiagnosticTroubleCode(code, DtcMemory.UNKNOWN, DtcProtocol.UDS_19, response.copyOfRange(i - 4, i), status, ecuAddress)
        }
        return result
    }
}

/** UDS 0x14 result classification. */
enum class UdsDtcClearResult { CLEARED, REJECTED, NO_RESPONSE, INVALID_RESPONSE }

object UdsDtcClearResponse {
    fun classify(response: ByteArray?): UdsDtcClearResult = when {
        response == null || response.isEmpty() -> UdsDtcClearResult.NO_RESPONSE
        (response[0].toInt() and 0xFF) == 0x54 -> UdsDtcClearResult.CLEARED
        (response[0].toInt() and 0xFF) == 0x7F -> UdsDtcClearResult.REJECTED
        else -> UdsDtcClearResult.INVALID_RESPONSE
    }
}
