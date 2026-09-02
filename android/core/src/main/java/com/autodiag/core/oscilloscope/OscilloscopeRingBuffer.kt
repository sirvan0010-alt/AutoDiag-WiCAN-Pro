package com.autodiag.core.oscilloscope

/** Fixed-size chronological ring buffer for high-rate waveform capture. */
class OscilloscopeRingBuffer(private val capacity: Int) {
    private val buffer = arrayOfNulls<OscilloscopeSample>(capacity)
    private var start = 0
    private var size = 0

    init {
        require(capacity > 0)
    }

    val count: Int get() = size
    val isFull: Boolean get() = size == capacity

    fun clear() {
        java.util.Arrays.fill(buffer, null)
        start = 0
        size = 0
    }

    fun add(sample: OscilloscopeSample) {
        val index = (start + size) % capacity
        buffer[index] = sample
        if (size < capacity) {
            size++
        } else {
            start = (start + 1) % capacity
        }
    }

    fun toList(): List<OscilloscopeSample> = List(size) { index ->
        buffer[(start + index) % capacity]!!
    }

    fun takeLast(count: Int): List<OscilloscopeSample> {
        require(count >= 0)
        val actual = minOf(count, size)
        return toList().takeLast(actual)
    }
}
