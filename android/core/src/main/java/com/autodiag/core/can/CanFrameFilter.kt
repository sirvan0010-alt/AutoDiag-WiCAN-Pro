package com.autodiag.core.can

/** Simple deterministic acceptance filter for diagnostic observation. */
data class CanFrameFilter(
    val id: Long,
    val mask: Long = 0x1FFFFFFFL,
    val extended: Boolean? = null
) {
    init {
        require(id in 0..0x1FFFFFFF) { "CAN identifier out of range" }
        require(mask in 0..0x1FFFFFFF) { "CAN mask out of range" }
    }

    fun matches(frame: CanFrame): Boolean =
        ((frame.id xor id) and mask) == 0L &&
            (extended == null || extended == frame.isExtended)
}

object CanFrameFilters {
    fun any(): (CanFrame) -> Boolean = { true }

    fun anyOf(filters: Collection<CanFrameFilter>): (CanFrame) -> Boolean = { frame ->
        filters.any { it.matches(frame) }
    }
}
