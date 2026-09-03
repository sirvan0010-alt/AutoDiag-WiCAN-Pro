package com.autodiag.core.obd

/**
 * Discovers standard Mode 01 PIDs without assuming that a vehicle supports
 * the whole SAE list. The supported bitmap tells us which PIDs are worth
 * polling; unsupported PIDs are never presented as available.
 */
class ObdCapabilityScanner(
    private val session: Elm327Session
) {
    private val bitmapPids = intArrayOf(0x00, 0x20, 0x40, 0x60, 0x80, 0xA0, 0xC0)

    suspend fun scan(): Result<Set<Int>> = runCatching {
        val supported = linkedSetOf<Int>()
        for (pid in bitmapPids) {
            val response = session.command("01${pid.toString(16).padStart(2, '0')}")
            val parsed = ObdResponseParser.parseMode01(response)
            val bitmap = parsed.firstOrNull { it.pid == pid } ?: continue
            if (bitmap.data.size < 4) continue
            bitmap.data.take(4).forEachIndexed { byteIndex, byte ->
                val value = byte.toInt() and 0xFF
                for (bit in 0..7) {
                    if ((value and (1 shl (7 - bit))) != 0) {
                        supported += pid + byteIndex * 8 + bit + 1
                    }
                }
            }
        }
        supported
    }
}
