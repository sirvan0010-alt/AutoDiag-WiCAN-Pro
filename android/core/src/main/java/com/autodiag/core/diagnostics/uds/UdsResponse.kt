package com.autodiag.core.diagnostics.uds

/** Typed UDS response classification. This layer is read-only and does not execute services. */
sealed interface UdsResponse {
    val serviceId: Int
}

data class UdsPositiveResponse(
    override val serviceId: Int,
    val payload: ByteArray
) : UdsResponse

data class UdsNegativeResponse(
    override val serviceId: Int,
    val responseCode: Int,
    val additionalBytes: ByteArray = byteArrayOf()
) : UdsResponse

object UdsResponseParser {
    fun parse(data: ByteArray): Result<UdsResponse> = runCatching {
        require(data.isNotEmpty()) { "UDS response is empty" }
        val sid = data[0].toInt() and 0xFF
        if (sid == 0x7F) {
            require(data.size >= 3) { "UDS negative response requires service and NRC" }
            UdsNegativeResponse(
                serviceId = data[1].toInt() and 0xFF,
                responseCode = data[2].toInt() and 0xFF,
                additionalBytes = data.copyOfRange(3, data.size)
            )
        } else {
            UdsPositiveResponse(sid, data.copyOfRange(1, data.size))
        }
    }
}
