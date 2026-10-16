package com.autodiag.core.can.mining

/**
 * Ranks baseline/event diff candidates using traffic classification and
 * counter-noise filtering.
 *
 * Scores are deterministic ordering hints for review, never semantic verification.
 * No score can promote a candidate to VEHICLE_VERIFIED.
 */
object CanMiningCandidateScorer {
    data class DiffCandidate(
        val key: CanFrameView.Key,
        val eventOnly: Boolean,
        val changedBytePositions: Set<Int>,
        val novelPayloadVariantCount: Int,
        val baselineFrameCount: Int,
        val eventFrameCount: Int,
        val evidenceRefs: List<String>,
    )

    data class ScoredCandidate(
        val candidate: DiffCandidate,
        val score: Double,
        val explanations: List<String>,
    )

    data class Config(
        val classWeight: Double = 0.45,
        val classEventValue: Double = 1.0,
        val classMixedValue: Double = 0.45,
        val classPeriodicValue: Double = 0.1,
        val eventOnlyBonus: Double = 0.25,
        val noveltyBonus: Double = 0.10,
        val payloadDeltaWeight: Double = 0.20,
        val noiseThreshold: Double = 0.15,
    ) {
        init {
            require(listOf(classWeight, classEventValue, classMixedValue, classPeriodicValue,
                eventOnlyBonus, noveltyBonus, payloadDeltaWeight, noiseThreshold).all { it.isFinite() })
            require(classWeight >= 0.0 && eventOnlyBonus >= 0.0 &&
                noveltyBonus >= 0.0 && payloadDeltaWeight >= 0.0)
            require(classEventValue in 0.0..1.0 && classMixedValue in 0.0..1.0 &&
                classPeriodicValue in 0.0..1.0 && noiseThreshold in 0.0..1.0)
        }
    }

    val defaultConfig = Config()

    fun score(
        candidates: List<DiffCandidate>,
        eventWindowFrames: List<CanFrameView>,
        config: Config = defaultConfig,
    ): List<ScoredCandidate> {
        require(candidates.all { it.novelPayloadVariantCount >= 0 &&
            it.baselineFrameCount >= 0 && it.eventFrameCount >= 0 &&
            it.changedBytePositions.all { index -> index in 0 until it.key.dlc } }) {
            "Candidate counts and changed byte positions must be valid"
        }
        val classes = CanMiningFrameClassifier.classify(eventWindowFrames)
        val counterPatterns = setOf(
            CanMiningCounterFilter.BytePattern.COUNTER_NIBBLE_HI,
            CanMiningCounterFilter.BytePattern.COUNTER_NIBBLE_LO,
            CanMiningCounterFilter.BytePattern.COUNTER_BYTE,
        )
        val counterPositions = CanMiningCounterFilter.analyze(eventWindowFrames)
            .filter { it.pattern in counterPatterns }
            .groupBy({ it.key }, { it.byteIndex })
            .mapValues { (_, positions) -> positions.toSet() }

        return candidates.map { scoreOne(it, classes, counterPositions, config) }
            .sortedWith(compareByDescending<ScoredCandidate> { it.score }
                .thenBy { it.candidate.key.canId }
                .thenBy { it.candidate.key.dlc })
    }

    fun noise(
        scored: List<ScoredCandidate>,
        config: Config = defaultConfig,
    ): List<ScoredCandidate> = scored.filter { it.score <= config.noiseThreshold }

    private fun scoreOne(
        candidate: DiffCandidate,
        classes: Map<CanFrameView.Key, CanMiningFrameClassifier.IdStats>,
        counterPositions: Map<CanFrameView.Key, Set<Int>>,
        config: Config,
    ): ScoredCandidate {
        val explanations = mutableListOf<String>()
        var total = 0.0
        val stats = classes[candidate.key]
        val classValue = when (stats?.frameClass) {
            CanMiningFrameClassifier.FrameClass.EVENT -> config.classEventValue
            CanMiningFrameClassifier.FrameClass.MIXED -> config.classMixedValue
            CanMiningFrameClassifier.FrameClass.PERIODIC -> config.classPeriodicValue
            null -> config.classMixedValue
        }
        total += config.classWeight * classValue
        explanations += "frameClass=${stats?.frameClass ?: "UNKNOWN"}"
        stats?.let { explanations += "medianIntervalMs=${it.medianIntervalMillis}" }

        if (candidate.eventOnly) {
            total += config.eventOnlyBonus
            explanations += "eventOnlyId"
        }
        if (candidate.novelPayloadVariantCount > 0) {
            total += config.noveltyBonus * minOf(1, candidate.novelPayloadVariantCount)
            explanations += "novelPayloadVariants=${candidate.novelPayloadVariantCount}"
        }

        val likelyCounters = counterPositions[candidate.key].orEmpty()
        val actionBytes = candidate.changedBytePositions - likelyCounters
        if (candidate.changedBytePositions.isNotEmpty()) {
            val fraction = if (candidate.key.dlc == 0) 0.0
                else actionBytes.size.toDouble() / candidate.key.dlc.toDouble()
            total += config.payloadDeltaWeight * fraction
            explanations += "changedBytes=${candidate.changedBytePositions.size}"
            explanations += "counterBytes=${candidate.changedBytePositions.intersect(likelyCounters).size}"
            explanations += "actionBytes=${actionBytes.size}"
        }
        return ScoredCandidate(candidate, total.coerceIn(0.0, 1.0), explanations)
    }
}
