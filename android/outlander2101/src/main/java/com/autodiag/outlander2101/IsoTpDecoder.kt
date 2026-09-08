package com.autodiag.outlander2101

class IsoTpDecoder {
    private var expectedLength = -1
    private val payload = ArrayList<Int>()
    private var nextSequence = 1

    fun reset() { expectedLength = -1; payload.clear(); nextSequence = 1 }

    fun accept(line: String): List<Int>? {
        val clean = line.trim().replace(" ", "")
        if (!clean.startsWith("762", true)) return null
        val hex = clean.substring(3)
        val bytes = ArrayList<Int>()
        var i = 0
        while (i + 1 < hex.length) {
            bytes.add(hex.substring(i, i + 2).toIntOrNull(16) ?: return null)
            i += 2
        }
        if (bytes.isEmpty()) return null
        val pci = bytes[0]
        return when (pci and 0xF0) {
            0x00 -> {
                val len = pci and 0x0F
                reset(); payload.addAll(bytes.drop(1).take(len)); payload.toList()
            }
            0x10 -> {
                if (bytes.size < 2) return null
                expectedLength = ((pci and 0x0F) shl 8) or bytes[1]
                payload.clear(); payload.addAll(bytes.drop(2).take(expectedLength)); nextSequence = 1
                if (payload.size >= expectedLength) payload.take(expectedLength) else null
            }
            0x20 -> {
                if (expectedLength < 0 || (pci and 0x0F) != nextSequence) { reset(); return null }
                payload.addAll(bytes.drop(1)); nextSequence = (nextSequence + 1) and 0x0F
                if (payload.size >= expectedLength) payload.take(expectedLength) else null
            }
            else -> null
        }
    }
}
