package com.autodiag.core.diagnostics.isotp

import com.autodiag.core.can.CanFrame

/**
 * Stateful ISO-TP payload reassembler for one diagnostic conversation.
 *
 * This class only consumes received frames. It deliberately does not emit
 * flow-control frames; transmission belongs to a separate, safety-gated layer.
 */
class IsoTpReassembler {
    private var expectedLength: Int? = null
    private var nextSequence: Int = 1
    private var buffer = ByteArray(0)

    fun reset() {
        expectedLength = null
        nextSequence = 1
        buffer = ByteArray(0)
    }

    fun accept(frame: CanFrame): Result<ByteArray?> = runCatching {
        when (val decoded = IsoTpDecoder.decode(frame).getOrThrow()) {
            is IsoTpSingleFrame -> {
                reset()
                decoded.payload.copyOf()
            }
            is IsoTpFirstFrame -> {
                reset()
                expectedLength = decoded.totalLength
                nextSequence = 1
                buffer = decoded.payload.copyOf()
                if (buffer.size >= decoded.totalLength) finish() else null
            }
            is IsoTpConsecutiveFrame -> {
                val expected = expectedLength ?: error("Consecutive frame without first frame")
                require(decoded.sequenceNumber == nextSequence) {
                    "Unexpected ISO-TP sequence ${decoded.sequenceNumber}, expected $nextSequence"
                }
                buffer += decoded.payload
                nextSequence = (nextSequence + 1) and 0x0F
                if (buffer.size >= expected) finish() else null
            }
            is IsoTpFlowControlFrame -> null
            else -> error("Unsupported ISO-TP frame type")
        }
    }

    private fun finish(): ByteArray {
        val length = expectedLength ?: error("No expected ISO-TP length")
        require(buffer.size >= length) { "Incomplete ISO-TP payload" }
        val result = buffer.copyOf(length)
        reset()
        return result
    }
}
