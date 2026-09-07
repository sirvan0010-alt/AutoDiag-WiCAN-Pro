package com.autodiag.core.diagnostic

import com.autodiag.core.can.CanFrame
import com.autodiag.core.can.IsoTp
import kotlinx.coroutines.delay

/**
 * Address-bound ISO-TP session. It deliberately has no socket dependency:
 * callers provide CAN TX and feed received frames back through accept().
 */
class IsoTpSession(
    val txId: Long,
    val rxId: Long,
    private val sendFrame: suspend (CanFrame) -> Unit,
    private val blockSize: Int = 0,
    private val stMinUs: Long = 0L
) {
    private val reassembler = IsoTp.Reassembler()

    init {
        require(txId in 0..0x7FF) { "Classic ISO-TP session currently supports standard 11-bit IDs" }
        require(rxId in 0..0x7FF)
        require(blockSize in 0..255)
        require(stMinUs >= 0)
    }

    suspend fun send(payload: ByteArray): Result<Unit> = runCatching {
        val segmented = IsoTp.segment(payload)
        if (!segmented.flowControlRequired) {
            sendFrame(CanFrame(txId, segmented.frames.single()))
            return@runCatching
        }
        // The first frame is sent here. Consecutive frames are sent only after
        // a valid Flow Control frame has been accepted by accept().
        pendingFrames = segmented.frames.drop(1)
        nextPendingIndex = 0
        sentSinceFlowControl = 0
        sendFrame(CanFrame(txId, segmented.frames.first()))
    }

    suspend fun accept(frame: CanFrame): Result<ByteArray?> = runCatching {
        if (frame.id != rxId || frame.isRemote) return@runCatching null
        val data = frame.data
        if (data.isEmpty()) return@runCatching null
        val pci = data[0].toInt() and 0xF0
        if (pci == 0x30) {
            val fc = IsoTp.parseFlowControl(data)
            when (fc.status) {
                IsoTp.FlowControl.Status.OVERFLOW -> error("ISO-TP receiver reported overflow")
                IsoTp.FlowControl.Status.WAIT -> return@runCatching null
                IsoTp.FlowControl.Status.CONTINUE -> sendPending(fc)
            }
            return@runCatching null
        }
        reassembler.accept(frame).let { result ->
            if (result.error != null) error(result.error)
            result.payload
        }
    }

    private suspend fun sendPending(fc: IsoTp.FlowControl) {
        if (pendingFrames.isEmpty()) return
        val limit = if (fc.blockSize == 0) Int.MAX_VALUE else fc.blockSize
        var sent = 0
        while (nextPendingIndex < pendingFrames.size && sent < limit) {
            val frame = pendingFrames[nextPendingIndex++]
            sendFrame(CanFrame(txId, frame))
            sent++
            if (fc.separationTimeUs > 0 && nextPendingIndex < pendingFrames.size) delay((fc.separationTimeUs / 1000L).coerceAtLeast(1L))
        }
        sentSinceFlowControl += sent
    }

    fun reset() {
        reassembler.reset()
        pendingFrames = emptyList()
        nextPendingIndex = 0
        sentSinceFlowControl = 0
    }

    private var pendingFrames: List<ByteArray> = emptyList()
    private var nextPendingIndex = 0
    private var sentSinceFlowControl = 0
}
