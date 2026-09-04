package com.autodiag.core.obd

/**
 * Mode 06 discovery uses 32-bit supported-ID windows: 06 00, 06 20, 06 40, ...
 * The discovery layer reports availability only; it never guesses monitor meaning.
 */
data class Mode06DiscoveryRequest(val baseMid: Int) {
    init {
        require(baseMid in 0..0xFF)
        require(baseMid % 0x20 == 0)
    }
    fun toCommand(): String = "06%02X".format(baseMid)
}

data class Mode06DiscoveryWindow(
    val baseMid: Int,
    val supportedMids: List<Int>,
    val hasNextWindow: Boolean,
    val rawPayload: ByteArray,
) {
    init {
        require(baseMid in 0..0xE0 && baseMid % 0x20 == 0)
        require(supportedMids.all { it in baseMid + 1..baseMid + 0x20 })
    }
}

data class Mode06DiscoveryResult(val windows: List<Mode06DiscoveryWindow>) {
    val supportedMids: List<Int> = windows.flatMap { it.supportedMids }.distinct().sorted()
}

object Mode06DiscoveryDecoder {
    /** Decode 46 BASE MASK[4]. */
    fun decode(response: String, requestedBaseMid: Int): Mode06DiscoveryWindow? {
        val base = requestedBaseMid and 0xFF
        if (base % 0x20 != 0 || base > 0xE0) return null
        val bytes = Mode06Decoder.extractBytes(response) ?: return null
        val serviceIndex = bytes.indexOf(0x46)
        if (serviceIndex < 0 || bytes.size < serviceIndex + 6) return null
        val payload = bytes.subList(serviceIndex + 1, serviceIndex + 6)
        if (payload[0] != base) return null

        val mask = (payload[1] shl 24) or (payload[2] shl 16) or
            (payload[3] shl 8) or payload[4]
        val supported = buildList {
            for (bit in 31 downTo 0) {
                if ((mask and (1 shl bit)) != 0) add(base + (32 - bit))
            }
        }
        return Mode06DiscoveryWindow(base, supported, (mask and 1) != 0, payload.map { it.toByte() }.toByteArray())
    }

    fun nextBase(window: Mode06DiscoveryWindow): Int? =
        if (window.hasNextWindow && window.baseMid < 0xE0) window.baseMid + 0x20 else null
}

object Mode06DiscoveryPlanner {
    fun firstRequest(): Mode06DiscoveryRequest = Mode06DiscoveryRequest(0x00)
    fun nextRequest(window: Mode06DiscoveryWindow): Mode06DiscoveryRequest? =
        Mode06DiscoveryDecoder.nextBase(window)?.let(::Mode06DiscoveryRequest)
}
