package com.autodiag.core.community

import com.autodiag.core.diagnostics.RepairContribution
import java.util.Locale

/**
 * Converts a sanitized community repair contribution into a GitHub Issue payload.
 *
 * The app should publish through a GitHub App installation token with repository
 * Issues: write permission. This class never accepts or stores OAuth tokens.
 */
data class GitHubIssuePayload(
    val title: String,
    val body: String,
    val labels: List<String>
)

class GitHubContributionPublisher(
    private val repositoryDisplayName: String = "AutoDiag community knowledge"
) {
    fun buildIssue(contribution: RepairContribution): GitHubIssuePayload {
        val code = contribution.code.trim().uppercase(Locale.ROOT)
        val ecu = contribution.ecu.trim().ifBlank { "UNKNOWN" }
        val memory = contribution.memory.trim().ifBlank { "UNKNOWN" }

        val title = "Repair contribution: $code / $ecu"
        val body = buildString {
            appendLine("<!-- AutoDiag community contribution; generated from a sanitized app record. -->")
            appendLine("Repository: $repositoryDisplayName")
            appendLine("DTC: $code")
            appendLine("ECU: $ecu")
            appendLine("Memory: $memory")
            contribution.vehicleSummary?.trim()?.takeIf { it.isNotEmpty() }?.let {
                appendLine("Vehicle scope: $it")
            }
            appendLine("Category: ${contribution.category}")
            appendLine("Cost bucket: ${contribution.costBucket}")
            appendLine("Time bucket: ${contribution.timeBucket}")
            contribution.note?.trim()?.takeIf { it.isNotEmpty() }?.let {
                appendLine("Note: $it")
            }
            appendLine("Verification: COMMUNITY_UNVERIFIED")
        }

        return GitHubIssuePayload(
            title = title,
            body = body,
            labels = listOf("community-contribution", "unverified")
        )
    }
}
