package com.autodiag.core.can.mining

/**
 * Minimal immutable view of one classic CAN frame for offline mining.
 * This package deliberately exposes no transport or transmit capability.
 */
data class CanFrameView(
    /** Capture-relative monotonic timestamp in milliseconds. */
    val timestampMillis: Long,
    val canId: Long,
    val extended: Boolean,
    val remote: Boolean,
    val dlc: Int,
    val payload: ByteArray,
) {
    data class Key(val canId: Long, val extended: Boolean, val remote: Boolean, val dlc: Int)

    val key: Key get() = Key(canId, extended, remote, dlc)

    init {
        require(timestampMillis >= 0L) { "timestampMillis must be non-negative" }
        require(canId in 0..0x1FFFFFFF) { "CAN identifier out of range" }
        require(!extended || canId <= 0x1FFFFFFF) { "extended CAN identifier out of range" }
        require(extended || canId <= 0x7FF) { "standard CAN identifier exceeds 11 bits" }
        require(dlc in 0..8) { "DLC out of classic CAN range: $dlc" }
        require(payload.size == dlc) { "Payload size ${payload.size} does not match DLC $dlc" }
    }

    override fun equals(other: Any?): Boolean =
        this === other || (other is CanFrameView &&
            timestampMillis == other.timestampMillis &&
            canId == other.canId &&
            extended == other.extended &&
            remote == other.remote &&
            dlc == other.dlc &&
            payload.contentEquals(other.payload))

    override fun hashCode(): Int {
        var result = timestampMillis.hashCode()
        result = 31 * result + canId.hashCode()
        result = 31 * result + extended.hashCode()
        result = 31 * result + remote.hashCode()
        result = 31 * result + dlc
        result = 31 * result + payload.contentHashCode()
        return result
    }
}
