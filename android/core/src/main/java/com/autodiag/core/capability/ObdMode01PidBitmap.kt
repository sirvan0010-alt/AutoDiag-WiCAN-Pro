package com.autodiag.core.capability

/**
 * Parses SAE Mode 01 supported-PID bitmap responses (41 00 / 41 20 / ...).
 * The bitmap is evidence of ECU-advertised support; callers still intersect
 * it with the local decoder registry before exposing a value.
 */
object ObdMode01PidBitmap {
    data class Block(val basePid: Int, val supportedPids: Set<Int>, val rawHex: String)

    fun parse(body: String): List<Block> {
        val bytes = body
            .split(Regex("\\s+"))
            .flatMap { token ->
                val clean = token.trim().uppercase()
                if (clean.isNotEmpty() && clean.length % 2 == 0 && clean.all { it in '0'..'9' || it in 'A'..'F' }) {
                    clean.chunked(2).map { it.toInt(16) }
                } else emptyList()
            }
        if (bytes.size < 6) return emptyList()

        val blocks = mutableListOf<Block>()
        var i = 0
        while (i + 5 < bytes.size) {
            if (bytes[i] == 0x41 && bytes[i + 1] % 0x20 == 0) {
                val base = bytes[i + 1]
                val bitmap = bytes.subList(i + 2, i + 6)
                val supported = buildSet {
                    bitmap.forEachIndexed { byteIndex, value ->
                        for (bit in 0..7) {
                            if ((value and (1 shl (7 - bit))) != 0) {
                                add(base + byteIndex * 8 + bit + 1)
                            }
                        }
                    }
                }
                blocks += Block(base, supported, bytes.subList(i, i + 6).joinToString(" ") { "%02X".format(it) })
                i += 6
            } else {
                i++
            }
        }
        return blocks.distinctBy { it.basePid }
    }

    fun supportedDecoderPids(body: String, decoderPids: Set<Int>): Set<Int> =
        parse(body).flatMap { it.supportedPids }.toSet().intersect(decoderPids)

    fun advertisesRange(body: String, nextBasePid: Int): Boolean =
        parse(body).any { it.basePid == nextBasePid - 0x20 && nextBasePid in it.supportedPids }
}
