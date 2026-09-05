package com.autodiag.core.capability

/** Generic byte/token decoder. Vehicle-specific formulas belong in diagnostic-data, not Kotlin. */
data class DataDecoderSpec(
    val kind: Kind,
    val start: Int,
    val end: Int = start,
    val scale: Double = 1.0,
    val offset: Double = 0.0,
    val unit: String? = null,
    val indices: List<Int>? = null
) {
    enum class Kind { UNSIGNED_U8, UNSIGNED_U16_BE, UNSIGNED_U16_LE, SIGNED_U8, SIGNED_I16_BE, SIGNED_I16_LE }
}

object DataDrivenDecoder {
    fun decode(tokens: IntArray, spec: DataDecoderSpec): Double {
        val positions = spec.indices ?: listOf(spec.start, spec.end)
        require(positions.isNotEmpty() && positions.all { it >= 0 }) {
            "Invalid decoder indices $positions"
        }
        val required = when (spec.kind) {
            DataDecoderSpec.Kind.UNSIGNED_U8, DataDecoderSpec.Kind.SIGNED_U8 -> 1
            else -> 2
        }
        require(positions.size == required) {
            "Decoder ${spec.kind} requires $required byte indices; got ${positions.size}"
        }
        require(positions.max() < tokens.size) {
            "Response has ${tokens.size} tokens; decoder requires index ${positions.max()}"
        }
        val raw: Double = when (spec.kind) {
            DataDecoderSpec.Kind.UNSIGNED_U8 -> u8(tokens[positions[0]]).toDouble()
            DataDecoderSpec.Kind.UNSIGNED_U16_BE -> u16(tokens[positions[0]], tokens[positions[1]]).toDouble()
            DataDecoderSpec.Kind.UNSIGNED_U16_LE -> u16(tokens[positions[1]], tokens[positions[0]]).toDouble()
            DataDecoderSpec.Kind.SIGNED_U8 -> signed8(tokens[positions[0]]).toDouble()
            DataDecoderSpec.Kind.SIGNED_I16_BE -> signed16(u16(tokens[positions[0]], tokens[positions[1]])).toDouble()
            DataDecoderSpec.Kind.SIGNED_I16_LE -> signed16(u16(tokens[positions[1]], tokens[positions[0]])).toDouble()
        }
        return raw * spec.scale + spec.offset
    }

    private fun u8(value: Int): Int = value and 0xFF
    private fun u16(high: Int, low: Int): Int = (u8(high) shl 8) or u8(low)
    private fun signed8(value: Int): Int {
        val v = u8(value)
        return if (v and 0x80 != 0) v - 0x100 else v
    }
    private fun signed16(value: Int): Int = if (value and 0x8000 != 0) value - 0x10000 else value
}
