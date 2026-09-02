package com.autodiag.core.diagnostics.isotp

import com.autodiag.core.can.CanFrame

/** ISO-TP PCI types carried in a classic CAN frame. */
enum class IsoTpPciType { SINGLE, FIRST, CONSECUTIVE, FLOW_CONTROL }

enum class FlowStatus { CONTINUE_TO_SEND, WAIT, OVERFLOW_ABORT }

/** Strongly typed representation of a decoded classic-CAN ISO-TP frame. */
sealed interface IsoTpFrame {
    val type: IsoTpPciType
}

data class IsoTpSingleFrame(val payload: ByteArray) : IsoTpFrame {
    override val type: IsoTpPciType = IsoTpPciType.SINGLE

    init {
        require(payload.size <= 7) { "Classic CAN ISO-TP single frame payload must be <= 7 bytes" }
    }
}

data class IsoTpFirstFrame(val totalLength: Int, val payload: ByteArray) : IsoTpFrame {
    override val type: IsoTpPciType = IsoTpPciType.FIRST

    init {
        require(totalLength >= 8) { "ISO-TP first-frame total length must be >= 8 bytes" }
        require(payload.isNotEmpty()) { "ISO-TP first frame must contain payload bytes" }
    }
}

data class IsoTpConsecutiveFrame(val sequenceNumber: Int, val payload: ByteArray) : IsoTpFrame {
    override val type: IsoTpPciType = IsoTpPciType.CONSECUTIVE

    init {
        require(sequenceNumber in 0..0x0F) { "ISO-TP sequence number must be 0..15" }
    }
}

data class IsoTpFlowControlFrame(
    val status: FlowStatus,
    val blockSize: Int,
    val separationTimeByte: Int
) : IsoTpFrame {
    override val type: IsoTpPciType = IsoTpPciType.FLOW_CONTROL

    init {
        require(blockSize in 0..0xFF) { "ISO-TP block size must be 0..255" }
        require(separationTimeByte in 0..0xFF) { "ISO-TP STmin byte must be 0..255" }
    }
}

/** Decoder for ISO-TP PCI bytes in classic CAN frames. */
object IsoTpDecoder {
    fun decode(frame: CanFrame): Result<IsoTpFrame> = runCatching {
        require(!frame.isRemote) { "Remote CAN frame has no ISO-TP payload" }
        require(frame.data.isNotEmpty()) { "ISO-TP frame is empty" }
        val pci = frame.data[0].toInt() and 0xFF
        when (pci ushr 4) {
            0x0 -> {
                val length = pci and 0x0F
                require(frame.data.size >= 1 + length) { "ISO-TP single frame payload is truncated" }
                IsoTpSingleFrame(frame.data.copyOfRange(1, 1 + length))
            }
            0x1 -> {
                require(frame.data.size >= 2) { "ISO-TP first frame is truncated" }
                val length = ((pci and 0x0F) shl 8) or (frame.data[1].toInt() and 0xFF)
                require(length >= 8) { "Invalid ISO-TP first-frame length" }
                IsoTpFirstFrame(length, frame.data.copyOfRange(2, frame.data.size))
            }
            0x2 -> IsoTpConsecutiveFrame(pci and 0x0F, frame.data.copyOfRange(1, frame.data.size))
            0x3 -> {
                require(frame.data.size >= 3) { "ISO-TP flow-control frame is truncated" }
                val status = when (pci and 0x0F) {
                    0 -> FlowStatus.CONTINUE_TO_SEND
                    1 -> FlowStatus.WAIT
                    2 -> FlowStatus.OVERFLOW_ABORT
                    else -> error("Unknown ISO-TP flow-control status")
                }
                IsoTpFlowControlFrame(
                    status = status,
                    blockSize = frame.data[1].toInt() and 0xFF,
                    separationTimeByte = frame.data[2].toInt() and 0xFF,
                )
            }
            else -> error("Unknown ISO-TP PCI type")
        }
    }
}
