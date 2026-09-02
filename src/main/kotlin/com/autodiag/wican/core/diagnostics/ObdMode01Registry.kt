package com.autodiag.wican.core.diagnostics

/** Registry entry for standard SAE J1979 Mode 01 live-data PIDs. */
data class ObdPidDefinition(
    val pid: Int,
    val name: String,
    val unit: String,
    val min: Double? = null,
    val max: Double? = null,
    val decoder: (ByteArray) -> Double?
)

/**
 * Start with PIDs whose formulas are unambiguous and already supported by the
 * application. More PIDs are added only with a verified formula and test vector.
 */
object ObdMode01Registry {
    val definitions: Map<Int, ObdPidDefinition> = listOf(
        ObdPidDefinition(0x05, "Engine coolant temperature", "°C", -40.0, 215.0) { bytes ->
            bytes.firstOrNull()?.toInt()?.minus(40)?.toDouble()
        },
        ObdPidDefinition(0x0B, "Intake manifold absolute pressure", "kPa", 0.0, 255.0) { bytes ->
            bytes.firstOrNull()?.toInt()?.toDouble()
        },
        ObdPidDefinition(0x0C, "Engine RPM", "rpm", 0.0, 16383.75) { bytes ->
            if (bytes.size < 2) null else ((bytes[0].toInt() and 0xFF) * 256 + (bytes[1].toInt() and 0xFF)) / 4.0
        },
        ObdPidDefinition(0x0D, "Vehicle speed", "km/h", 0.0, 255.0) { bytes ->
            bytes.firstOrNull()?.toInt()?.and(0xFF)?.toDouble()
        }
    ).associateBy { it.pid }

    fun definition(pid: Int): ObdPidDefinition? = definitions[pid and 0xFF]
}

/** Four-byte supported-PID bitmap (PIDs 01-20, 21-40, ...). */
object ObdSupportedPidBitmap {
    fun isSupported(bitmap: ByteArray, pid: Int): Boolean {
        if (pid !in 1..0xE0) return false
        val blockStart = ((pid - 1) / 0x20) * 0x20 + 1
        val index = pid - blockStart
        if (bitmap.size < 4 || index !in 0..31) return false
        val byteIndex = index / 8
        val bit = 7 - (index % 8)
        return ((bitmap[byteIndex].toInt() and 0xFF) and (1 shl bit)) != 0
    }
}
