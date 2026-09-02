package com.autodiag.core.obd

/** Standard OBD service requests used for DTC memory. */
object ObdDtcCommands {
    const val READ_STORED_MODE = 0x03
    const val CLEAR_DTC_MODE = 0x04
    const val READ_PENDING_MODE = 0x07
    const val READ_PERMANENT_MODE = 0x0A

    fun clearRequest(): ByteArray = byteArrayOf(CLEAR_DTC_MODE.toByte())
}

/** Decodes two-byte SAE OBD DTC encoding from Mode 03/07/0A payloads. */
object ObdDtcDecoder {
    fun decode(response: ByteArray, memory: DtcMemory = DtcMemory.STORED): List<DiagnosticTroubleCode> {
        if (response.isEmpty()) return emptyList()
        val expectedMode = when (memory) {
            DtcMemory.STORED -> 0x43
            DtcMemory.PENDING -> 0x47
            DtcMemory.PERMANENT -> 0x4A
            DtcMemory.UNKNOWN -> null
        }
        val offset = if (expectedMode != null && u8(response[0]) == expectedMode) 1 else 0
        if (offset >= response.size) return emptyList()

        val result = mutableListOf<DiagnosticTroubleCode>()
        var i = offset
        while (i + 1 < response.size) {
            val a = u8(response[i])
            val b = u8(response[i + 1])
            i += 2
            if (a == 0 && b == 0) continue
            val code = decodeCode(a, b) ?: continue
            result += DiagnosticTroubleCode(
                code = code,
                memory = memory,
                protocol = when (memory) {
                    DtcMemory.STORED -> DtcProtocol.OBD_MODE_03
                    DtcMemory.PENDING -> DtcProtocol.OBD_MODE_07
                    DtcMemory.PERMANENT -> DtcProtocol.OBD_MODE_0A
                    DtcMemory.UNKNOWN -> DtcProtocol.UNKNOWN
                },
                rawBytes = byteArrayOf(a.toByte(), b.toByte())
            )
        }
        return result
    }

    private fun decodeCode(a: Int, b: Int): String {
        val system = when ((a ushr 6) and 0x03) {
            0 -> 'P'
            1 -> 'C'
            2 -> 'B'
            else -> 'U'
        }
        val digit1 = (a ushr 4) and 0x03
        val digit2 = a and 0x0F
        return "$system$digit1${hex(digit2)}${hex(b ushr 4)}${hex(b and 0x0F)}"
    }

    private fun hex(value: Int): String = value.toString(16).uppercase()
    private fun u8(value: Byte): Int = value.toInt() and 0xFF
}
