package com.autodiag.core.can

/** Pure ISO-TP codec for classic CAN. No UDS/ECU knowledge belongs here. */
object IsoTp {
    data class FlowControl(val status: Status, val blockSize: Int, val separationTimeUs: Long) {
        enum class Status { CONTINUE, WAIT, OVERFLOW }
    }

    data class SegmentResult(val frames: List<ByteArray>, val flowControlRequired: Boolean)
    data class ReassemblyResult(val payload: ByteArray?, val nextSequenceNumber: Int? = null, val flowControl: FlowControl? = null, val error: String? = null)

    fun segment(payload: ByteArray): SegmentResult {
        require(payload.isNotEmpty()) { "ISO-TP payload must not be empty" }
        require(payload.size <= 4095) { "Classic ISO-TP payload must be <= 4095 bytes" }
        if (payload.size <= 7) return SegmentResult(listOf(byteArrayOf(payload.size.toByte()) + payload), false)
        val first = ByteArray(8)
        first[0] = (0x10 or ((payload.size shr 8) and 0x0F)).toByte()
        first[1] = payload.size.toByte()
        payload.copyInto(first, 2, 0, 6)
        val frames = mutableListOf(first)
        var offset = 6
        var seq = 1
        while (offset < payload.size) {
            val count = minOf(7, payload.size - offset)
            frames += (byteArrayOf((0x20 or seq).toByte()) + payload.copyOfRange(offset, offset + count))
            offset += count
            seq = (seq + 1) and 0x0F
        }
        return SegmentResult(frames, true)
    }

    fun parseFlowControl(data: ByteArray): FlowControl {
        require(data.size >= 3 && (data[0].toInt() and 0xF0) == 0x30) { "Not an ISO-TP Flow Control frame" }
        val status = when (data[0].toInt() and 0x0F) {
            0 -> FlowControl.Status.CONTINUE
            1 -> FlowControl.Status.WAIT
            2 -> FlowControl.Status.OVERFLOW
            else -> error("Invalid ISO-TP Flow Status")
        }
        val stMin = data[2].toInt() and 0xFF
        val separationUs = when (stMin) {
            in 0x00..0x7F -> stMin * 1000L
            in 0xF1..0xF9 -> (stMin - 0xF0) * 100L
            else -> 0L
        }
        return FlowControl(status, data[1].toInt() and 0xFF, separationUs)
    }

    class Reassembler {
        private var expected = -1
        private var received = 0
        private var nextSeq = 1
        private var buffer = ByteArray(0)

        fun reset() { expected = -1; received = 0; nextSeq = 1; buffer = ByteArray(0) }

        fun accept(frame: CanFrame): ReassemblyResult {
            if (frame.isRemote || frame.data.isEmpty()) return ReassemblyResult(null, error = "Not a data frame")
            val d = frame.data
            return when (d[0].toInt() and 0xF0) {
                0x00 -> {
                    val n = d[0].toInt() and 0x0F
                    if (n > 7 || n > d.size - 1) ReassemblyResult(null, error = "Invalid Single Frame length")
                    else ReassemblyResult(d.copyOfRange(1, n + 1)).also { reset() }
                }
                0x10 -> {
                    if (d.size < 2) return ReassemblyResult(null, error = "Short First Frame")
                    val n = ((d[0].toInt() and 0x0F) shl 8) or (d[1].toInt() and 0xFF)
                    if (n <= 7 || n > 4095) return ReassemblyResult(null, error = "Invalid First Frame length: $n")
                    expected = n; received = minOf(6, n, d.size - 2); nextSeq = 1; buffer = ByteArray(n)
                    d.copyInto(buffer, 0, 2, 2 + received)
                    ReassemblyResult(if (received == expected) buffer.copyOf() else null, nextSeq, FlowControl(FlowControl.Status.CONTINUE, 0, 0))
                }
                0x20 -> {
                    if (expected < 0) return ReassemblyResult(null, error = "Unexpected Consecutive Frame")
                    val seq = d[0].toInt() and 0x0F
                    if (seq != nextSeq) { reset(); return ReassemblyResult(null, error = "ISO-TP sequence mismatch: expected $nextSeq got $seq") }
                    val n = minOf(7, d.size - 1, expected - received)
                    if (n <= 0) { reset(); return ReassemblyResult(null, error = "ISO-TP payload overflow") }
                    d.copyInto(buffer, received, 1, n + 1); received += n; nextSeq = (nextSeq + 1) and 0x0F
                    if (received == expected) { val result = buffer.copyOf(); reset(); ReassemblyResult(result) }
                    else ReassemblyResult(null, nextSeq)
                }
                0x30 -> ReassemblyResult(null, error = "Flow Control is not a payload frame")
                else -> ReassemblyResult(null, error = "Unsupported ISO-TP PCI type")
            }
        }
    }
}
