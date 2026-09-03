package com.autodiag.core.contribution

interface ContributionTransport {
    suspend fun upload(batch: List<ContributionRecord>): Result<Unit>
}

sealed class ContributionUploadResult {
    data object SkippedNoConsent : ContributionUploadResult()
    data object SkippedDisabled : ContributionUploadResult()
    data object NothingToSend : ContributionUploadResult()
    data class Uploaded(val count: Int) : ContributionUploadResult()
    data class Failed(val reason: String?) : ContributionUploadResult()
}

class ContributionUploader(
    private val consentManager: ContributionConsentManager,
    private val store: ContributionStore,
    private val transport: ContributionTransport,
    private val config: ContributionConfig
) {
    suspend fun flush(): ContributionUploadResult {
        if (!config.isEnabled) return ContributionUploadResult.SkippedDisabled
        if (!consentManager.hasActiveConsent()) return ContributionUploadResult.SkippedNoConsent

        val pending = store.pending()
        if (pending.isEmpty()) return ContributionUploadResult.NothingToSend

        val batch = pending.take(config.maxBatchSize)
        return transport.upload(batch).fold(
            onSuccess = {
                store.clear()
                ContributionUploadResult.Uploaded(batch.size)
            },
            onFailure = { t -> ContributionUploadResult.Failed(t.message) }
        )
    }
}
