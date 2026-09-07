package com.autodiag.core.can

/**
 * Incremental parser for classic-CAN SLCAN ASCII frames.
 *
 * The parser is stream-oriented: TCP reads may split or concatenate frames.
 * A CR terminates a frame; LF is accepted and ignored when used as a second terminator.
 */
class SlcanParser {
    sealed interface Event {
        data class Frame(val frame: CanFrame) : Event
        data class Error(val raw: String, val reason: String) : Event
    }

    private val line = StringBuilder()

    fun accept(bytes: ByteArray): List<Event> {
        val events = mutableListOf<Event>()
        for (byte in bytes) {
            when (byte.toInt() and 0xFF) {
                0x0D -> {
                    if (line.isNotEmpty()) {
                        val raw = line.toString()
                        line.setLength(0)
                        events += parse(raw)
                    }
                }
                0x0A -> Unit
                else -> {
                    if (line.length >= MAX_LINE_LENGTH) {
                        val raw = line.toString()
                        line.setLength(0)
                        events += Event.Error(raw, "SLCAN frame exceeds maximum length")
                    } else {
                        line.append(byte.toInt().and(0xFF).toChar())
                    }
                }
            }
        }
        return events
    }

    fun reset() { line.setLength(0) }

    private fun parse(raw: String): Event {
        if (raw.isEmpty()) return Event.Error(raw, "Empty SLCAN frame")
        val type = raw[0]
        val extended = type == 'T' || type == 'R'
        val remote = type == 'r' || type == 'R'
        if (type !in charArrayOf('t', 'T', 'r', 'R')) {
            return Event.Error(raw, "Unsupported SLCAN frame type '$type'")
        }
        val idDigits = if (extended) 8 else 3
        val minimum = 1 + idDigits + 1
        if (raw.length < minimum) return Event.Error(raw, "Truncated SLCAN frame")
        val idText = raw.substring(1, 1 + idDigits)
        val dlcText = raw.substring(1 + idDigits, minimum)
        val id = idText.toLongOrNull(16)
            ?: return Event.Error(raw, "Invalid CAN identifier")
        val dlc = dlcText.toIntOrNull(16)
            ?: return Event.Error(raw, "Invalid DLC")
        val maxId = if (extended) 0x1FFFFFFF else 0x7FF
        if (id !in 0..maxId) return Event.Error(raw, "CAN identifier out of range")
        if (dlc !in 0..8) return Event.Error(raw, "DLC out of range: $dlc")
        val expectedLength = minimum + if (remote) 0 else dlc * 2
        if (raw.length != expectedLength) return Event.Error(raw, "SLCAN length mismatch: expected $expectedLength got ${raw.length}")

        val data = if (remote) ByteArray(0) else ByteArray(dlc) { index ->
            raw.substring(minimum + index * 2, minimum + index * 2 + 2).toIntOrNull(16)?.toByte()
                ?: throw IllegalArgumentException("Invalid CAN data")
        }
        return Event.Frame(CanFrame(id = id, data = data, timestampNanos = System.nanoTime(), isExtended = extended, isRemote = remote))
    }

    private companion object { const val MAX_LINE_LENGTH = 32 }
}
