package com.autodiag.core.capability

/**
 * Decoder derived from the analyzed PHEV Watchdog implementation.
 *
 * This decodes only the field identified by the source as ISOLATION_RESISTANCE.
 * It does not claim the physical measurement topology beyond electrical HV
 * isolation relative to the vehicle/chassis reference.
 */
object OutlanderPhevIsolationDecoder {
    const val REQUEST = "21 01"

    data class DecodedIsolation(
        val kiloOhms: Double,
        val rawUnsigned16: Int
    )

    /**
     * The source-derived field is two unsigned bytes, big-endian, with no
     * fractional scaling shown in the analyzed decoder. Therefore the numeric
     * source resolution is 1 kΩ; Double is retained for API consistency/future
     * verified scaling, but no artificial decimal precision is introduced.
     */
    fun decode(responseBytes: ByteArray): DecodedIsolation? {
        if (responseBytes.size < 80) return null
        val raw = ((responseBytes[78].toInt() and 0xFF) shl 8) or
            (responseBytes[79].toInt() and 0xFF)
        return DecodedIsolation(kiloOhms = raw.toDouble(), rawUnsigned16 = raw)
    }
}
