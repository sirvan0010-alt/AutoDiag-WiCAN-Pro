package com.autodiag.core.obd

/** UDS 0x19 sub-functions used to inspect ECU DTC memory. */
object UdsDtcSubFunction {
    const val REPORT_NUMBER_BY_STATUS_MASK = 0x01
    const val REPORT_DTC_BY_STATUS_MASK = 0x02
    const val REPORT_DTC_SNAPSHOT_BY_DTC = 0x04
    const val REPORT_DTC_EXTENDED_DATA_BY_DTC = 0x06
}

data class UdsDtcStatus(val mask: Int) {
    init { require(mask in 0..0xFF) }
    val testFailed get() = mask and 0x01 != 0
    val testFailedThisOperationCycle get() = mask and 0x02 != 0
    val pendingDtc get() = mask and 0x04 != 0
    val confirmedDtc get() = mask and 0x08 != 0
    val testNotCompletedSinceLastClear get() = mask and 0x10 != 0
    val testFailedSinceLastClear get() = mask and 0x20 != 0
    val testNotCompletedThisOperationCycle get() = mask and 0x40 != 0
    val warningIndicatorRequested get() = mask and 0x80 != 0
}

object UdsDtcRequestBuilder {
    fun reportByStatusMask(statusMask: Int = 0xFF): ByteArray {
        require(statusMask in 0..0xFF)
        return byteArrayOf(0x19, 0x02, statusMask.toByte())
    }
    fun clearRequest(groupOfDtc: Int = 0xFFFFFF): ByteArray {
        require(groupOfDtc in 0..0xFFFFFF)
        return byteArrayOf(0x14, ((groupOfDtc ushr 16) and 0xFF).toByte(), ((groupOfDtc ushr 8) and 0xFF).toByte(), (groupOfDtc and 0xFF).toByte())
    }
}

/** Keeps the ECU's three-byte UDS DTC identifier as raw hex; no false P/C/B/U conversion. */
object UdsDtcDecoder {
    fun decode(response: ByteArray, ecuAddress: Int? = null): List<DiagnosticTroubleCode> {
        if (response.size < 2 || (response[0].toInt() and 0xFF) != 0x59 || (response[1].toInt() and 0xFF) != 0x02) return emptyList()
        val result = mutableListOf<DiagnosticTroubleCode>()
        var i = 2
        while (i + 3 < response.size) {
            val raw = response.copyOfRange(i, i + 3)
            val status = response[i + 3].toInt() and 0xFF
            i += 4
            val hex = raw.joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }.uppercase()
            result += DiagnosticTroubleCode("DTC$hex", DtcMemory.UNKNOWN, DtcProtocol.UDS_19, raw, status, ecuAddress)
        }
        return result
    }
}

enum class UdsDtcClearResult { CLEARED, REJECTED, NO_RESPONSE, INVALID_RESPONSE }
object UdsDtcClearResponse {
    fun classify(response: ByteArray?): UdsDtcClearResult = when {
        response == null || response.isEmpty() -> UdsDtcClearResult.NO_RESPONSE
        (response[0].toInt() and 0xFF) == 0x54 -> UdsDtcClearResult.CLEARED
        (response[0].toInt() and 0xFF) == 0x7F -> UdsDtcClearResult.REJECTED
        else -> UdsDtcClearResult.INVALID_RESPONSE
    }
}
