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
 * Builds a sanitized GitHub Issue payload. Authentication and HTTP stay outside core.
 * The Android app should use a GitHub App installation token with Issues: write only
 * on the selected community repository.
 */
class GitHubContributionPublisher(
    private val repositoryDisplayName: String = "AutoDiag community knowledge"
) {
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
