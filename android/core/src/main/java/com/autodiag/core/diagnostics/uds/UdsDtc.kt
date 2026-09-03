package com.autodiag.core.diagnostics.uds

/** Raw UDS ReadDTCInformation request (service 0x19). */
data class UdsReadDtcInformationRequest(val subFunction: Int) {
    init { require(subFunction in 0..0xFF) }
    fun toPayload(): ByteArray = byteArrayOf(0x19, subFunction.toByte())
}

data class UdsDtc(
    val code: Int,
    val statusByte: Int,
    val additionalData: ByteArray = byteArrayOf(),
) {
    init {
        require(code in 0..0xFFFFFF)
        require(statusByte in 0..0xFF)
    }

    fun codeString(): String {
        val prefix = when ((code ushr 22) and 0x03) {
            0 -> "P"
            1 -> "C"
            2 -> "B"
            else -> "U"
        }
        return prefix + "%05X".format(code and 0x3FFFFF)
    }
}

data class UdsDtcReport(
    val subFunction: Int,
    val statusAvailabilityMask: Int?,
    val dtcs: List<UdsDtc>,
    val rawPayload: ByteArray,
)

object UdsReadDtcInformationParser {
    /**
     * Parses the common 0x19 response layout for sub-functions that return
     * DTC/status records: 59 <subFunction> <statusAvailabilityMask>
     * followed by zero or more 3-byte DTC + 1-byte status records.
     * Unknown trailing bytes are preserved and never interpreted as DTCs.
     */
    fun parse(response: UdsResponse): Result<UdsDtcReport> = runCatching {
        val positive = response as? UdsPositiveResponse
            ?: error("Expected positive UDS response")
        require(positive.serviceId == 0x59) { "Expected UDS positive service 0x59" }
        require(positive.payload.size >= 2) { "DTC response payload is truncated" }

        val subFunction = positive.payload[0].toInt() and 0xFF
        val mask = positive.payload[1].toInt() and 0xFF
        val remaining = positive.payload.size - 2
        require(remaining % 4 == 0) { "DTC records are truncated or have unknown layout" }

        val dtcs = buildList {
            var offset = 2
            while (offset < positive.payload.size) {
                val code = ((positive.payload[offset].toInt() and 0xFF) shl 16) or
                    ((positive.payload[offset + 1].toInt() and 0xFF) shl 8) or
                    (positive.payload[offset + 2].toInt() and 0xFF)
                val status = positive.payload[offset + 3].toInt() and 0xFF
                add(UdsDtc(code, status))
                offset += 4
            }
        }

        UdsDtcReport(
            subFunction = subFunction,
            statusAvailabilityMask = mask,
            dtcs = dtcs,
            rawPayload = positive.payload.copyOf(),
        )
    }
}
