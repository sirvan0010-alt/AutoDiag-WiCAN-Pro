package com.autodiag.wican.core.capture

/** O(log n) timestamp lookup for replay scrubbing. samples must be sorted by timestamp. */
data class ReplayPoint<T>(val timestampMs: Long, val value: T)

class ReplayIndex<T>(private val samples: List<ReplayPoint<T>>) {
    init { require(samples.zipWithNext().all { it.first.timestampMs <= it.second.timestampMs }) }

    fun nearest(timestampMs: Long): ReplayPoint<T>? {
        if (samples.isEmpty()) return null
        var lo = 0
        var hi = samples.lastIndex
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (samples[mid].timestampMs < timestampMs) lo = mid + 1 else hi = mid
        }
        val right = samples[lo]
        if (lo == 0) return right
        val left = samples[lo - 1]
        return if (timestampMs - left.timestampMs <= right.timestampMs - timestampMs) left else right
    }

    fun range(fromMs: Long, toMs: Long): List<ReplayPoint<T>> {
        if (samples.isEmpty() || fromMs > toMs) return emptyList()
        var start = 0
        var end = samples.size
        while (start < end) {
            val mid = (start + end) ushr 1
            if (samples[mid].timestampMs < fromMs) start = mid + 1 else end = mid
        }
        val first = start
        start = 0; end = samples.size
        while (start < end) {
            val mid = (start + end) ushr 1
            if (samples[mid].timestampMs <= toMs) start = mid + 1 else end = mid
        }
        return samples.subList(first, start)
    }
}
