package com.autodiag.core.diagnostic

/** UDS request/response primitives. Correlation and transport stay above this codec. */
object Uds {
    data class Request(
        val service: Int,
        val payload: ByteArray = byteArrayOf(),
        val createdAtNanos: Long = System.nanoTime()
    ) {
        init { require(service in 0..0xFF) }
        fun bytes(): ByteArray = byteArrayOf(service.toByte()) + payload
    }

    data class Response(
        val request: Request,
        val rawPayload: ByteArray,
        val positive: Boolean,
        val responseService: Int,
        val negativeResponseCode: Int? = null,
        val pending: Boolean = false,
        val receivedAtNanos: Long = System.nanoTime()
    ) {
        val roundTripNanos: Long get() = receivedAtNanos - request.createdAtNanos
    }

    fun parse(request: Request, payload: ByteArray): Response {
        require(payload.isNotEmpty()) { "UDS response must not be empty" }
        val service = payload[0].toInt() and 0xFF
        if (service == NEGATIVE_RESPONSE) {
            require(payload.size >= 3) { "UDS negative response is truncated" }
            val originalService = payload[1].toInt() and 0xFF
            val nrc = payload[2].toInt() and 0xFF
            require(originalService == request.service) {
                "UDS response service mismatch: expected ${request.service.toString(16)} got ${originalService.toString(16)}"
            }
            return Response(request, payload.copyOf(), false, originalService, nrc, nrc == RESPONSE_PENDING)
        }
        require(service == (request.service + 0x40).and(0xFF)) {
            "UDS positive response mismatch: request=0x${request.service.toString(16)} response=0x${service.toString(16)}"
        }
        return Response(request, payload.copyOf(), true, service)
    }

    const val NEGATIVE_RESPONSE = 0x7F
    const val RESPONSE_PENDING = 0x78
}
