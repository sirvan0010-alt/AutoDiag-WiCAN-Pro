package com.autodiag.core.can

import com.autodiag.core.can.mining.CanFrameView
import com.autodiag.core.can.mining.CanMiningCandidateScorer

/**
 * Converts raw diff output to the scorer's stable input contract.
 * Evidence refs identify the analyzed capture windows; callers may add artifact refs.
 */
object CanMiningScorerAdapter {
    fun toDiffCandidates(
        report: CanMiningDiffReport,
        evidenceRefs: List<String> = emptyList(),
    ): List<CanMiningCandidateScorer.DiffCandidate> {
        val windowRefs = evidenceRefs + listOf(
            "capture-window:baseline:${report.baselineWindow.startNanos}-${report.baselineWindow.endNanos}",
            "capture-window:event:${report.eventWindow.startNanos}-${report.eventWindow.endNanos}",
        )
        return report.candidates.map { candidate ->
            CanMiningCandidateScorer.DiffCandidate(
                key = CanFrameView.Key(candidate.canId, candidate.isExtended, candidate.isRemote, candidate.dlc),
                eventOnly = candidate.baselineFrameCount == 0,
                changedBytePositions = candidate.changedBytePositions.toSet(),
                novelPayloadVariantCount = candidate.eventOnlyPayloads.size,
                baselineFrameCount = candidate.baselineFrameCount,
                eventFrameCount = candidate.eventFrameCount,
                evidenceRefs = windowRefs,
            )
        }
    }
}
