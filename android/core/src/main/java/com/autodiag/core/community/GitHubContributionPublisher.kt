package com.autodiag.core.community

import java.util.Locale

/** Sanitized vehicle scope intentionally excludes VIN and direct identifiers. */
data class CommunityVehicleScope(
    val make: String? = null,
    val model: String? = null,
    val generation: String? = null,
    val modelYear: Int? = null,
    val engineOrMotor: String? = null,
    val battery: String? = null,
    val transmission: String? = null,
    val drivetrain: String? = null,
    val region: String? = null
)

data class CommunityRepairContribution(
    val dtcCode: String,
    val ecu: String,
    val memory: String,
    val vehicle: CommunityVehicleScope = CommunityVehicleScope(),
    val category: String,
    val costBucket: String? = null,
    val timeBucket: String? = null,
    val note: String? = null
)

data class GitHubIssuePayload(
    val title: String,
    val body: String,
    val labels: List<String>
)

/**
 * Result of [GitHubContributionPublisher.preparePublish]: either the payload is
 * ready to send, or the free-text note contains something that needs the
 * contributor's attention before this becomes a public, permanently
 * attributed GitHub Issue.
 */
sealed class ContributionPublishResult {
    data class Ready(val payload: GitHubIssuePayload) : ContributionPublishResult()
    data class NeedsReview(val findings: List<CommunityNoteScreening.Finding>) : ContributionPublishResult()
}

/**
 * Builds a sanitized GitHub Issue payload. Authentication and HTTP stay outside core.
 * The Android app should use a GitHub App installation token with Issues: write only
 * on the selected community repository.
 */
class GitHubContributionPublisher(
    private val repositoryDisplayName: String = "AutoDiag community knowledge"
) {
    /**
     * Preferred entry point for the UI layer. Screens [CommunityRepairContribution.note]
     * for accidental PII (see [CommunityNoteScreening]) before building the issue.
     *
     * @param userConfirmedNoteIsSafe set true only after the UI has shown the
     *   flagged findings to the contributor and they explicitly chose to
     *   publish anyway (e.g. a false positive, or they intentionally want it
     *   in the note). Defaults to false so nothing publishes unreviewed.
     */
    fun preparePublish(
        contribution: CommunityRepairContribution,
        userConfirmedNoteIsSafe: Boolean = false
    ): ContributionPublishResult {
        val findings = CommunityNoteScreening.screen(contribution.note)
        return if (findings.isNotEmpty() && !userConfirmedNoteIsSafe) {
            ContributionPublishResult.NeedsReview(findings)
        } else {
            ContributionPublishResult.Ready(buildIssue(contribution))
        }
    }

    /**
     * Low-level builder, unchanged from the original implementation for
     * backward compatibility. Does not screen [CommunityRepairContribution.note]
     * for PII — callers that accept user-authored free text should go through
     * [preparePublish] instead so a note is never published unreviewed.
     */
    fun buildIssue(contribution: CommunityRepairContribution): GitHubIssuePayload {
        val code = contribution.dtcCode.trim().uppercase(Locale.ROOT)
        val ecu = contribution.ecu.trim().ifBlank { "UNKNOWN" }
        val memory = contribution.memory.trim().ifBlank { "UNKNOWN" }
        val body = buildString {
            appendLine("<!-- AutoDiag community contribution; generated from a sanitized app record. -->")
            appendLine("Repository: $repositoryDisplayName")
            appendLine("DTC: $code")
            appendLine("ECU: $ecu")
            appendLine("Memory: $memory")
            val scope = listOfNotNull(
                contribution.vehicle.make, contribution.vehicle.model,
                contribution.vehicle.generation, contribution.vehicle.modelYear?.toString(),
                contribution.vehicle.engineOrMotor, contribution.vehicle.battery,
                contribution.vehicle.transmission, contribution.vehicle.drivetrain,
                contribution.vehicle.region
            ).joinToString(" | ")
            if (scope.isNotBlank()) appendLine("Vehicle scope: $scope")
            appendLine("Category: ${contribution.category}")
            contribution.costBucket?.let { appendLine("Cost bucket: $it") }
            contribution.timeBucket?.let { appendLine("Time bucket: $it") }
            contribution.note?.trim()?.takeIf { it.isNotEmpty() }?.let { appendLine("Note: $it") }
            appendLine("Verification: COMMUNITY_UNVERIFIED")
        }
        return GitHubIssuePayload(
            title = "Repair contribution: $code / $ecu",
            body = body,
            labels = listOf("community-contribution", "unverified")
        )
    }
}
