package com.autodiag.core.obd

/**
 * Discovers standard Mode 01 PIDs using the SAE 32-PID bitmap ranges.
 *
 * The next bitmap is queried only when the current bitmap advertises its
 * boundary PID. This keeps discovery fast on vehicles that expose only the
 * lower part of the standard PID space and avoids blindly issuing seven
 * requests to every ECU.
 */
class ObdCapabilityScanner(
    private val session: Elm327Session
) {
    private val bitmapPids = intArrayOf(0x00, 0x20, 0x40, 0x60, 0x80, 0xA0, 0xC0)

    suspend fun scan(): Result<Set<Int>> = runCatching {
        val supported = linkedSetOf<Int>()

        for (index in bitmapPids.indices) {
            val bitmapPid = bitmapPids[index]
            val response = session.command("01${bitmapPid.toString(16).padStart(2, '0')}")
            val bitmap = ObdResponseParser.parseMode01(response)
                .firstOrNull { it.pid == bitmapPid }
                ?: break

            if (bitmap.data.size < 4) break

            bitmap.data.take(4).forEachIndexed { byteIndex, byte ->
                val value = byte.toInt() and 0xFF
                for (bit in 0..7) {
                    if ((value and (1 shl (7 - bit))) != 0) {
                        val pid = bitmapPid + byteIndex * 8 + bit + 1
                        if (pid <= 0xE0) supported += pid
                    }
                }
            }

            // The least-significant bit of the final byte advertises the
            // boundary PID for the next 32-PID range. If it is not set,
            // standards-compliant discovery can stop here.
            val boundarySupported = (bitmap.data[3].toInt() and 0x01) != 0
            if (!boundarySupported || index == bitmapPids.lastIndex) break
        }

        supported
    }
}
