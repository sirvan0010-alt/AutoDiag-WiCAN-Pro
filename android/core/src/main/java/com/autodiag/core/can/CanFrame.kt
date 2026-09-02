package com.autodiag.core.can

/** Immutable classic CAN 2.0 frame used by the diagnostic and analysis pipeline. */
data class CanFrame(
    val id: Long,
    val data: ByteArray = byteArrayOf(),
    val timestampNanos: Long? = null,
    val isExtended: Boolean = false,
    val isRemote: Boolean = false
) {
    init {
        require(id in 0..0x1FFFFFFF) { "CAN identifier out of range" }
        require(data.size <= 8) { "Classic CAN payload must be <= 8 bytes" }
    }

    val dataLength: Int get() = data.size

    fun hex(): String = data.joinToString(" ") { "%02X".format(it.toInt() and 0xFF) }

    override fun equals(other: Any?): Boolean = other is CanFrame &&
        id == other.id && data.contentEquals(other.data) &&
        timestampNanos == other.timestampNanos && isExtended == other.isExtended && isRemote == other.isRemote

    override fun hashCode(): Int = 31 * id.hashCode() + data.contentHashCode()
}
