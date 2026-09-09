package com.autodiag.outlander2101

/**
 * Decoder rules extracted from PHEV Watchdog 21 01 evidence.
 *
 * The transport is deliberately separate from the decoder: Watchdog proves
 * the request/response path and these exact byte formulas, while the vehicle
 * must still supply a payload long enough for the selected variant.
 */
object Watchdog2101Decoder {
    data class Result(
        val variant: String,
        val signal: String,
        val value: Float,
        val unit: String,
        val note: String
    )

    fun decode(bytes: List<Int>): Result? {
        // LZ3A: HV isolation resistance, UInt16 BE at response indices 78..79,
        // scale 1.0 kOhm. This is the only extracted Watchdog rule that is
        // explicitly an isolation-resistance signal.
        if (bytes.size > 79) {
            val raw = u16be(bytes, 78)
            if (raw != null) {
                return Result(
                    variant = "watchdog.lz3a.21_01",
                    signal = "battery.isolation_resistance",
                    value = raw.toFloat(),
                    unit = "kΩ",
                    note = "Watchdog LZ3A: UInt16 BE [78..79], scale 1.0"
                )
            }
        }

        return null
    }

    /**
     * Other Watchdog 21 01 layouts are intentionally exposed as separate
     * helpers because they represent internal-resistance signals, not HV
     * isolation. They must never be silently displayed as isolation.
     */
    fun decodeInternalResistance(bytes: List<Int>): List<Result> {
        val out = mutableListOf<Result>()

        // LE4A: UInt8 [38], [39], scale 0.1 MΩ.
        if (bytes.size > 39) {
            out += Result(
                "watchdog.le4a.21_01",
                "battery.max_internal_resistance",
                bytes[38] * 0.1f,
                "MΩ",
                "Watchdog LE4A: UInt8 [38], scale 0.1 MΩ"
            )
            out += Result(
                "watchdog.le4a.21_01",
                "battery.min_internal_resistance",
                bytes[39] * 0.1f,
                "MΩ",
                "Watchdog LE4A: UInt8 [39], scale 0.1 MΩ"
            )
        }

        // LD4A: UInt16 BE [12..13]/[14..15], scale 0.001 MΩ;
        // difference UInt8 [71], scale 0.02 MΩ.
        if (bytes.size > 15) {
            u16be(bytes, 12)?.let {
                out += Result(
                    "watchdog.ld4a.21_01",
                    "battery.max_internal_resistance",
                    it * 0.001f,
                    "MΩ",
                    "Watchdog LD4A: UInt16 BE [12..13], scale 0.001 MΩ"
                )
            }
            u16be(bytes, 14)?.let {
                out += Result(
                    "watchdog.ld4a.21_01",
                    "battery.min_internal_resistance",
                    it * 0.001f,
                    "MΩ",
                    "Watchdog LD4A: UInt16 BE [14..15], scale 0.001 MΩ"
                )
            }
        }
        if (bytes.size > 71) {
            out += Result(
                "watchdog.ld4a.21_01",
                "battery.max_internal_resistance_difference",
                bytes[71] * 0.02f,
                "MΩ",
                "Watchdog LD4A: UInt8 [71], scale 0.02 MΩ"
            )
        }

        return out
    }

    private fun u16be(bytes: List<Int>, index: Int): Int? {
        if (index < 0 || index + 1 >= bytes.size) return null
        return (bytes[index] shl 8) or bytes[index + 1]
    }
}
