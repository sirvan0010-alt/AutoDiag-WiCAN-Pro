package com.autodiag.core.can

/**
 * Decoder/encoder for the ASCII SLCAN frame format used by Lawicel-compatible
 * CAN interfaces. It deliberately handles classic CAN only (0..8 data bytes).
 */
object SlcanCodec {
    fun decode(line: String, timestampNanos: Long? = null): CanFrame? {
        val value = line.trim().removeSuffix("\r").removeSuffix("\n")
        if (value.length < 2) return null

        val type = value[0]
        val extended = type == 'T' || type == 'R'
        val remote = type == 'r' || type == 'R'
        if (type !in charArrayOf('t', 'T', 'r', 'R')) return null

        val idChars = if (extended) 8 else 3
        if (value.length < 1 + idChars + 1) return null
        val id = value.substring(1, 1 + idChars).toLongOrNull(16) ?: return null
        val dlc = value[1 + idChars].digitToIntOrNull(16) ?: return null
        if (dlc > 8) return null

        val payloadStart = 2 + idChars
        if (remote) {
            if (value.length != payloadStart) return null
            return CanFrame(id, timestampNanos = timestampNanos, isExtended = extended, isRemote = true)
        }

        val expectedPayloadChars = dlc * 2
        if (value.length != payloadStart + expectedPayloadChars) return null
        val payload = ByteArray(dlc)
        for (i in 0 until dlc) {
            val byte = value.substring(payloadStart + i * 2, payloadStart + i * 2 + 2).toIntOrNull(16)
                ?: return null
            payload[i] = byte.toByte()
        }
        return CanFrame(id, payload, timestampNanos, extended, false)
    }

    fun encode(frame: CanFrame): String {
        val type = when {
            frame.isExtended && frame.isRemote -> 'R'
            frame.isExtended -> 'T'
            frame.isRemote -> 'r'
            else -> 't'
        }
        val idWidth = if (frame.isExtended) 8 else 3
        val id = frame.id.toString(16).uppercase().padStart(idWidth, '0')
        val payload = if (frame.isRemote) "" else frame.hex().replace(" ", "")
        return "$type$id${frame.dataLength.toString(16).uppercase()}$payload\r"
    }

    /** Parses arbitrary TCP chunks while preserving partial lines between calls. */
    class StreamDecoder {
        private val pending = StringBuilder()

        fun accept(chunk: ByteArray, timestampNanos: Long? = null): List<CanFrame> =
            accept(chunk.toString(Charsets.US_ASCII), timestampNanos)

        fun accept(chunk: String, timestampNanos: Long? = null): List<CanFrame> {
            pending.append(chunk)
            val frames = mutableListOf<CanFrame>()
            while (true) {
                val end = pending.indexOf("\r")
                if (end < 0) break
                val line = pending.substring(0, end)
                pending.delete(0, end + 1)
                decode(line, timestampNanos)?.let(frames::add)
            }
            return frames
        }

        fun reset() = pending.clear()
    }
}
