package com.autodiag.core.diagnostics.uds

/** Typed ReadDataByIdentifier (0x22) response. No vehicle-specific DID is assumed here. */
data class UdsDidValue(
    val did: Int,
    val data: ByteArray,
) {
    init { require(did in 0..0xFFFF) { "DID must be 0..0xFFFF" } }

    fun hex(): String = data.joinToString(" ") { "%02X".format(it.toInt() and 0xFF) }

    override fun equals(other: Any?): Boolean = other is UdsDidValue && did == other.did && data.contentEquals(other.data)
    override fun hashCode(): Int = 31 * did + data.contentHashCode()
}

/** Parser for a positive ReadDataByIdentifier response (0x62). */
object UdsDidParser {
    const val POSITIVE_SERVICE_ID = 0x62

    fun parsePositive(response: UdsPositiveResponse): Result<List<UdsDidValue>> = runCatching {
        require(response.serviceId == POSITIVE_SERVICE_ID) {
            "Expected UDS positive ReadDataByIdentifier response 0x62, got 0x%02X".format(response.serviceId)
        }
        require(response.payload.size >= 2) { "UDS DID response must contain a DID" }

        // A 0x62 response can contain one or more DIDs, but DID value lengths are
        // service/vehicle specific. Without an explicit decoder boundary, preserve
        // the complete remaining bytes as the value of the requested DID.
        val did = ((response.payload[0].toInt() and 0xFF) shl 8) or
            (response.payload[1].toInt() and 0xFF)
        listOf(UdsDidValue(did, response.payload.copyOfRange(2, response.payload.size)))
    }
}

/** Generic ECU identification result. Values are raw unless a verified decoder exists. */
data class EcuIdentification(
    val ecuId: String?,
    val identifiers: List<UdsDidValue>,
    val verification: com.autodiag.core.diagnostic.EvidenceVerification,
)
