package com.autodiag.core.can

/** UI-neutral live state for the RAW CAN monitor. */
data class RawCanMonitorState(
    val frames: List<CanFrame> = emptyList(),
    val stats: CanBusStats = CanBusStats(),
    val idFilter: String = "",
    val paused: Boolean = false,
    val maxFrames: Int = DEFAULT_MAX_FRAMES
) {
    fun accepts(frame: CanFrame): Boolean {
        val filter = idFilter.trim()
        if (filter.isEmpty()) return true
        val normalized = filter.removePrefix("0x").removePrefix("0X")
        val id = normalized.toLongOrNull(16) ?: return false
        return frame.id == id
    }

    fun onFrame(frame: CanFrame): RawCanMonitorState {
        val nextStats = stats.onFrame(frame)
        if (paused || !accepts(frame)) return copy(stats = nextStats)
        val nextFrames = (frames + frame).takeLast(maxFrames.coerceAtLeast(1))
        return copy(frames = nextFrames, stats = nextStats)
    }

    fun clear(): RawCanMonitorState = copy(frames = emptyList(), stats = CanBusStats())

    companion object {
        const val DEFAULT_MAX_FRAMES = 500
    }
}
