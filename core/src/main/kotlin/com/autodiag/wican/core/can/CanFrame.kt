package com.autodiag.wican.core.can

data class CanFrame(
    val timestampMicros: Long,
    val id: Long,
    val data: ByteArray,
    val extended: Boolean = false,
    val fd: Boolean = false
) {
    init {
        require(id >= 0) { "CAN ID must be non-negative" }
        require(data.size <= if (fd) 64 else 8) { "CAN payload exceeds frame format" }
    }

    fun idHex(): String = id.toString(16).uppercase().padStart(if (extended) 8 else 3, '0')
}
