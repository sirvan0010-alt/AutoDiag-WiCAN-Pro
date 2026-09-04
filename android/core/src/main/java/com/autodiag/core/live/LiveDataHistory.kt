package com.autodiag.core.live

/** Immutable point used by live-data charts and logs. */
data class LiveDataHistoryPoint(
    val timestampEpochMs: Long,
    val value: Double,
)

data class LiveDataHistoryWindow(
    val key: String,
    val points: List<LiveDataHistoryPoint>,
    val minimum: Double?,
    val maximum: Double?,
) {
    val firstTimestampEpochMs: Long? get() = points.firstOrNull()?.timestampEpochMs
    val lastTimestampEpochMs: Long? get() = points.lastOrNull()?.timestampEpochMs
}

/**
 * Bounded in-memory history for a single signal. The oldest point is discarded
 * first, so a high-rate graph cannot grow the process indefinitely.
 */
class LiveDataHistory(
    private val key: String,
    private val capacity: Int = 4096,
) {
    init { require(key.isNotBlank()); require(capacity > 0) }

    private val points = ArrayDeque<LiveDataHistoryPoint>(capacity)

    @Synchronized
    fun append(timestampEpochMs: Long, value: Double) {
        require(value.isFinite())
        if (points.size == capacity) points.removeFirst()
        points.addLast(LiveDataHistoryPoint(timestampEpochMs, value))
    }

    @Synchronized
    fun snapshot(): LiveDataHistoryWindow {
        val copy = points.toList()
        return LiveDataHistoryWindow(
            key = key,
            points = copy,
            minimum = copy.minOfOrNull { it.value },
            maximum = copy.maxOfOrNull { it.value },
        )
    }

    @Synchronized
    fun clear() = points.clear()
}

/** Multi-signal bounded history used by graph/dashboard layers. */
class LiveDataHistoryStore(private val perSignalCapacity: Int = 4096) {
    private val histories = mutableMapOf<String, LiveDataHistory>()

    @Synchronized
    fun append(key: String, timestampEpochMs: Long, value: Double) {
        histories.getOrPut(key) { LiveDataHistory(key, perSignalCapacity) }
            .append(timestampEpochMs, value)
    }

    @Synchronized
    fun snapshot(key: String): LiveDataHistoryWindow =
        histories[key]?.snapshot() ?: LiveDataHistoryWindow(key, emptyList(), null, null)

    @Synchronized
    fun snapshotAll(): List<LiveDataHistoryWindow> = histories.values.map { it.snapshot() }

    @Synchronized
    fun clear() = histories.values.forEach(LiveDataHistory::clear)
}
