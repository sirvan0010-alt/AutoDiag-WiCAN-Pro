package com.autodiag.core.can.mining

/**
 * Heuristic byte-pattern analysis for reducing rolling-counter noise in offline
 * candidate lists. This labels likely patterns only; it does not verify counters.
 */
object CanMiningCounterFilter {
    enum class BytePattern { CONSTANT, TOGGLE, COUNTER_NIBBLE_HI, COUNTER_NIBBLE_LO, COUNTER_BYTE, VARYING }

    data class ByteObservation(
        val key: CanFrameView.Key,
        val byteIndex: Int,
        val pattern: BytePattern,
        val coverage: Double,
    )

    data class Config(
        val minPairs: Int = 3,
        val counterCoverageThreshold: Double = 0.6,
        val toggleCoverageThreshold: Double = 0.9,
    ) {
        init {
            require(minPairs >= 1) { "minPairs must be >= 1" }
            require(counterCoverageThreshold in 0.0..1.0) { "counterCoverageThreshold must be in [0,1]" }
            require(toggleCoverageThreshold in 0.0..1.0) { "toggleCoverageThreshold must be in [0,1]" }
        }
    }

    val defaultConfig = Config()

    /** Frames should belong to one capture window and one vehicle state. */
    fun analyze(
        frames: List<CanFrameView>,
        config: Config = defaultConfig,
    ): List<ByteObservation> =
        frames.groupBy { it.key }.flatMap { (key, group) ->
            analyzeKey(key, group.sortedBy { it.timestampMillis }, config)
        }

    /** Removes only byte positions labelled as likely counters; empty candidates are dropped. */
    fun excludeLikelyCounterBytes(
        candidates: Map<CanFrameView.Key, Set<Int>>,
        frames: List<CanFrameView>,
        config: Config = defaultConfig,
    ): Map<CanFrameView.Key, Set<Int>> {
        val counterPatterns = setOf(
            BytePattern.COUNTER_NIBBLE_HI,
            BytePattern.COUNTER_NIBBLE_LO,
            BytePattern.COUNTER_BYTE,
        )
        val noisy = analyze(frames, config)
            .filter { it.pattern in counterPatterns }
            .groupBy({ it.key }, { it.byteIndex })
        return candidates.mapValues { (key, positions) ->
            positions - (noisy[key]?.toSet() ?: emptySet())
        }.filterValues { it.isNotEmpty() }
    }

    private fun analyzeKey(
        key: CanFrameView.Key,
        sorted: List<CanFrameView>,
        config: Config,
    ): List<ByteObservation> {
        if (sorted.isEmpty()) return emptyList()
        val pairs = sorted.zipWithNext()
        return (0 until key.dlc).map { index -> observe(key, index, sorted, pairs, config) }
    }

    private fun observe(
        key: CanFrameView.Key,
        index: Int,
        sorted: List<CanFrameView>,
        pairs: List<Pair<CanFrameView, CanFrameView>>,
        config: Config,
    ): ByteObservation {
        val values = sorted.map { it.payload[index].toInt() and 0xFF }
        if (values.toSet().size == 1) return ByteObservation(key, index, BytePattern.CONSTANT, 1.0)
        if (pairs.size < config.minPairs) return ByteObservation(key, index, BytePattern.VARYING, 0.0)

        val hi = pairs.count { (a, b) -> nibbleSteps(a.payload[index], b.payload[index], high = true) }
        val lo = pairs.count { (a, b) -> nibbleSteps(a.payload[index], b.payload[index], high = false) }
        val byte = pairs.count { (a, b) -> stepsByOneMod256(a.payload[index], b.payload[index]) }
        val total = pairs.size.toDouble()

        return when {
            hi / total >= config.counterCoverageThreshold ->
                ByteObservation(key, index, BytePattern.COUNTER_NIBBLE_HI, hi / total)
            lo / total >= config.counterCoverageThreshold ->
                ByteObservation(key, index, BytePattern.COUNTER_NIBBLE_LO, lo / total)
            byte / total >= config.counterCoverageThreshold ->
                ByteObservation(key, index, BytePattern.COUNTER_BYTE, byte / total)
            isToggle(values) && togglePairs(values) / total >= config.toggleCoverageThreshold ->
                ByteObservation(key, index, BytePattern.TOGGLE, togglePairs(values) / total)
            else -> ByteObservation(key, index, BytePattern.VARYING, 0.0)
        }
    }

    private fun nibbleSteps(a: Byte, b: Byte, high: Boolean): Boolean {
        val av = a.toInt() and 0xFF
        val bv = b.toInt() and 0xFF
        val aNibble = if (high) av ushr 4 else av and 0x0F
        val bNibble = if (high) bv ushr 4 else bv and 0x0F
        val aOther = if (high) av and 0x0F else av ushr 4
        val bOther = if (high) bv and 0x0F else bv ushr 4
        return ((aNibble + 1) and 0x0F) == bNibble && aOther == bOther
    }

    private fun stepsByOneMod256(a: Byte, b: Byte): Boolean =
        (((a.toInt() and 0xFF) + 1) and 0xFF) == (b.toInt() and 0xFF)

    private fun isToggle(values: List<Int>): Boolean = values.toSet().size == 2

    private fun togglePairs(values: List<Int>): Int =
        values.zipWithNext().count { (a, b) -> a != b }
}
