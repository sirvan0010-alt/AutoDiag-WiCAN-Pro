package com.autodiag.wican.core.capture

import com.autodiag.wican.core.can.CanFrame

data class CaptureSample(
    val sequence: Long,
    val timestampMicros: Long,
    val frame: CanFrame
)

/** Stable timestamp lookup for replay/scrubbing. */
class CaptureIndex(samples: List<CaptureSample>) {
    private val sorted = samples.sortedBy { it.timestampMicros }

    fun nearest(timestampMicros: Long): CaptureSample? {
        if (sorted.isEmpty()) return null
        var lo = 0
        var hi = sorted.lastIndex
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (sorted[mid].timestampMicros < timestampMicros) lo = mid + 1 else hi = mid
        }
        if (lo == 0) return sorted[0]
        val a = sorted[lo - 1]
        val b = sorted[lo]
        return if (timestampMicros - a.timestampMicros <= b.timestampMicros - timestampMicros) a else b
    }

    fun range(startMicros: Long, endMicros: Long): List<CaptureSample> {
        if (startMicros > endMicros) return emptyList()
        val start = lowerBound(startMicros)
        val end = lowerBound(endMicros + 1)
        return sorted.subList(start, end.coerceAtMost(sorted.size))
    }

    private fun lowerBound(value: Long): Int {
        var lo = 0
        var hi = sorted.size
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (sorted[mid].timestampMicros < value) lo = mid + 1 else hi = mid
        }
        return lo
    }
}
