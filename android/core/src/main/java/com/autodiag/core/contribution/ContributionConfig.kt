package com.autodiag.core.contribution

data class ContributionConfig(
    val endpointUrl: String? = null,
    val currentConsentVersion: Int = 1,
    val currentSchemaVersion: Int = 1,
    val maxBatchSize: Int = 25
) {
    val isEnabled: Boolean get() = !endpointUrl.isNullOrBlank()
}
