package com.autodiag.core.obd

/**
 * Keeps the last known sample for each PID.
 *
 * A failed poll must not erase a previously valid measurement. Consumers can
 * re-evaluate freshness from the stored timestamp and show it as last-known
 * rather than falsely presenting it as a current value.
 */
class LiveDataStore {
    private val values = mutableMapOf<Int, LiveDataSample>()

    @Synchronized
    fun update(sample: LiveDataSample) {
        values[sample.pid] = sample
    }

    @Synchronized
    fun get(pid: Int): LiveDataSample? = values[pid]

    @Synchronized
    fun snapshot(): List<LiveDataSample> = values.values.sortedBy { it.pid }

    @Synchronized
    fun clear() {
        values.clear()
    }
}
