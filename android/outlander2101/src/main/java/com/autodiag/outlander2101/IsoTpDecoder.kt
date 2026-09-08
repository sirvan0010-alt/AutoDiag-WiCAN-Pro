package com.autodiag.outlander2101

/**
 * ISO-TP reassembler for ELM327/WiCAN textual CAN frames.
 *
 * WiCAN is configured for CAN 11-bit / 500 kbit/s and the BMU response is
 * expected on 0x762. The ELM327 flow-control configuration is handled by the
 * transport, so this class only reassembles received frames.
 */
class IsoTpDecoder(private val responseId: String = "762") {
    private var expectedLength = -1
    private val payload = ArrayList<Int>()
    private var nextSequence = 1

    fun reset() {
        expectedLength = -1
        payload.clear()
        nextSequence = 1
    }

    fun accept(line: String): List<Int>? {
        val clean = line
            .trim()
            .replace(" ", "")
            .replace("\t", "")
            .uppercase()

        if (!clean.startsWith(responseId)) return null
        val hex = clean.substring(responseId.length)
        if (hex.length < 2) return null

        val bytes = ArrayList<Int>(hex.length / 2)
        var i = 0
        while (i + 1 < hex.length) {
            val b = hex.substring(i, i + 2).toIntOrNull(16) ?: return null
            bytes.add(b)
            i += 2
        }
        if (bytes.isEmpty()) return null

        val pci = bytes[0]
        return when (pci and 0xF0) {
            // ISO-TP Single Frame
            0x00 -> {
                val len = pci and 0x0F
                if (bytes.size < len + 1) return null
                reset()
                payload.addAll(bytes.drop(1).take(len))
                payload.toList()
            }

            // ISO-TP First Frame
            0x10 -> {
                if (bytes.size < 2) return null
                expectedLength = ((pci and 0x0F) shl 8) or bytes[1]
                payload.clear()
                payload.addAll(bytes.drop(2).take(expectedLength))
                nextSequence = 1
                if (payload.size >= expectedLength) {
                    val result = payload.take(expectedLength)
                    reset()
                    result
                } else null
            }

            // ISO-TP Consecutive Frame
            0x20 -> {
                if (expectedLength < 0) return null
                if ((pci and 0x0F) != nextSequence) {
                    reset()
                    return null
                }
                payload.addAll(bytes.drop(1))
                nextSequence = (nextSequence + 1) and 0x0F
                if (payload.size >= expectedLength) {
                    val result = payload.take(expectedLength)
                    reset()
                    result
                } else null
            }

            else -> null
        }
    }
}
