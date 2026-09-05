package com.autodiag.core.capability

/** Generic byte/token decoder. Vehicle-specific formulas belong in diagnostic-data, not Kotlin. */
data class DataDecoderSpec(
    val kind: Kind,
    val start: Int,
    val end: Int = start,
    val scale: Double = 1.0,
    val offset: Double = 0.0,
    val unit: String? = null
) {
    enum class Kind { UNSIGNED_U8, UNSIGNED_U16_BE, UNSIGNED_U16_LE, SIGNED_U8, SIGNED_I16_BE, SIGNED_I16_LE }
}

object DataDrivenDecoder {
    fun decode(tokens: IntArray, spec: DataDecoderSpec): Double {
        require(spec.start >= 0 && spec.end >= spec.start) { "Invalid decoder range ${spec.start}..${spec.end}" }
        require(tokens.size > spec.end) { "Response has ${tokens.size} tokens; decoder requires index ${spec.end}" }
        val raw: Double = when (spec.kind) {
            DataDecoderSpec.Kind.UNSIGNED_U8 -> u8(tokens[spec.start]).toDouble()
            DataDecoderSpec.Kind.UNSIGNED_U16_BE -> ((u8(tokens[spec.start]) shl 8) or u8(tokens[spec.end])).toDouble()
            DataDecoderSpec.Kind.UNSIGNED_U16_LE -> ((u8(tokens[spec.end]) shl 8) or u8(tokens[spec.start])).toDouble()
            DataDecoderSpec.Kind.SIGNED_U8 -> signed8(tokens[spec.start]).toDouble()
            DataDecoderSpec.Kind.SIGNED_I16_BE -> signed16((u8(tokens[spec.start]) shl 8) or u8(tokens[spec.end])).toDouble()
            DataDecoderSpec.Kind.SIGNED_I16_LE -> signed16((u8(tokens[spec.end]) shl 8) or u8(tokens[spec.start])).toDouble()
        }
        return raw * spec.scale + spec.offset
    }

    private fun u8(value: Int): Int = value and 0xFF
    private fun signed8(value: Int): Int {
        val v = u8(value)
        return if (v and 0x80 != 0) v - 0x100 else v
    }
    private fun signed16(value: Int): Int = if (value and 0x8000 != 0) value - 0x10000 else value
}
