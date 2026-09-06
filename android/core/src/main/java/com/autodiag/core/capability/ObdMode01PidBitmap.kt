package com.autodiag.core.capability

/**
 * Parses SAE Mode 01 supported-PID bitmap responses (41 00 / 41 20 / ...).
 * The bitmap is evidence of ECU-advertised support; callers still intersect
 * it with the local decoder registry before exposing a value.
 */
object ObdMode01PidBitmap {
    data class Block(val basePid: Int, val supportedPids: Set<Int>, val rawHex: String)

    fun parse(body: String): List<Block> {
        val bytes = Regex("(?i)(?<![0-9A-F])[0-9A-F]{2}(?![0-9A-F])")
            .findAll(body)
            .map { it.value.toInt(16) }
            .toList()
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

    fun advertisesRange(body: String, basePid: Int): Boolean =
        parse(body).any { it.basePid == basePid && basePid + 0x20 in it.supportedPids }
}
