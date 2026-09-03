package com.autodiag.core.contribution

class ContributionConsentManager(
    private val consentStore: ConsentStore,
    private val contributionStore: ContributionStore,
    private val config: ContributionConfig
) {
    fun state(): ContributionConsentRecord = consentStore.current()

    fun hasActiveConsent(): Boolean = consentStore.current().isActive(config.currentConsentVersion)

    fun grant(nowEpochMs: Long) {
        consentStore.save(
            ContributionConsentRecord(
                state = ContributionConsentState.GRANTED,
                consentVersion = config.currentConsentVersion,
                decidedAtEpochMs = nowEpochMs
            )
        )
    }

    fun decline(nowEpochMs: Long) {
        consentStore.save(
            ContributionConsentRecord(
                state = ContributionConsentState.DECLINED,
                consentVersion = config.currentConsentVersion,
                decidedAtEpochMs = nowEpochMs
            )
        )
        contributionStore.clear()
    }

    fun revoke(nowEpochMs: Long) {
        consentStore.save(
            ContributionConsentRecord(
                state = ContributionConsentState.REVOKED,
                consentVersion = config.currentConsentVersion,
                decidedAtEpochMs = nowEpochMs
            )
        )
        contributionStore.clear()
    }
}
