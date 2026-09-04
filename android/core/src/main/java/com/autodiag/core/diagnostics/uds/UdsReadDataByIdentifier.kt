package com.autodiag.core.diagnostics.uds

/** Read-only UDS ReadDataByIdentifier request (0x22). */
data class UdsReadDataByIdentifierRequest(val did: Int) {
    init { require(did in 0..0xFFFF) { "DID must be 0..0xFFFF" } }

    fun toPayload(): ByteArray = byteArrayOf(
        0x22,
        (did ushr 8).toByte(),
        (did and 0xFF).toByte(),
    )
}

/**
 * Parses a 0x62 response for one explicitly requested DID.
 * Unknown bytes are preserved; no manufacturer-specific interpretation occurs.
 */
object UdsReadDataByIdentifierParser {
    fun parse(requestedDid: Int, response: UdsResponse): Result<UdsDidValue> = runCatching {
        require(requestedDid in 0..0xFFFF) { "DID must be 0..0xFFFF" }
        val positive = response as? UdsPositiveResponse
            ?: error("Expected positive UDS response")
        require(positive.serviceId == 0x62) {
            "Expected 0x62, got 0x%02X".format(positive.serviceId)
        }
        require(positive.payload.size >= 2) { "0x62 response does not contain a DID" }
        val returnedDid = ((positive.payload[0].toInt() and 0xFF) shl 8) or
            (positive.payload[1].toInt() and 0xFF)
        require(returnedDid == requestedDid) {
            "Unexpected DID 0x%04X, expected 0x%04X".format(returnedDid, requestedDid)
        }
        UdsDidValue(returnedDid, positive.payload.copyOfRange(2, positive.payload.size))
    }
}
