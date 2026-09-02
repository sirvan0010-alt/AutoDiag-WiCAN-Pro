package com.autodiag.core.diagnostics.isotp

import com.autodiag.core.can.CanFrame

/** ISO-TP PCI types carried in a classic CAN frame. */
enum class IsoTpPciType { SINGLE, FIRST, CONSECUTIVE, FLOW_CONTROL }

data class IsoTpSingleFrame(val payload: ByteArray) {
    init { require(payload.size <= 7) { "Classic CAN ISO-TP single frame payload must be <= 7 bytes" } }
}

data class IsoTpFirstFrame(val totalLength: Int, val payload: ByteArray)
data class IsoTpConsecutiveFrame(val sequenceNumber: Int, val payload: ByteArray)
data class IsoTpFlowControlFrame(
    val status: FlowStatus,
    val blockSize: Int,
    val separationTimeByte: Int
)

enum class FlowStatus { CONTINUE_TO_SEND, WAIT, OVERFLOW_ABORT }

/** Read-only decoder for ISO-TP PCI bytes. */
object IsoTpDecoder {
    fun decode(frame: CanFrame): Result<Any> = runCatching {
        require(!frame.isRemote) { "Remote CAN frame has no ISO-TP payload" }
        require(frame.data.isNotEmpty()) { "ISO-TP frame is empty" }
        val pci = frame.data[0].toInt() and 0xFF
        when (pci ushr 4) {
            0x0 -> IsoTpSingleFrame(frame.data.copyOfRange(1, 1 + (pci and 0x0F)))
            0x1 -> {
                require(frame.data.size >= 2)
                val length = ((pci and 0x0F) shl 8) or (frame.data[1].toInt() and 0xFF)
                require(length >= 8) { "Invalid ISO-TP first-frame length" }
                IsoTpFirstFrame(length, frame.data.copyOfRange(2, frame.data.size))
            }
            0x2 -> IsoTpConsecutiveFrame(pci and 0x0F, frame.data.copyOfRange(1, frame.data.size))
            0x3 -> {
                require(frame.data.size >= 3)
                val status = when (pci and 0x0F) {
                    0 -> FlowStatus.CONTINUE_TO_SEND
                    1 -> FlowStatus.WAIT
                    2 -> FlowStatus.OVERFLOW_ABORT
                    else -> error("Unknown ISO-TP flow-control status")
                }
                IsoTpFlowControlFrame(status, frame.data[1].toInt() and 0xFF, frame.data[2].toInt() and 0xFF)
            }
            else -> error("Unknown ISO-TP PCI type")
        }
    }
}
