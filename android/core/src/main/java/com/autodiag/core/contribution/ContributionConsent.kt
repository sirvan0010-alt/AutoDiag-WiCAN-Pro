package com.autodiag.core.contribution

/**
 * Consent state for contributing anonymized diagnostic data (PID/DTC statistics,
 * never raw personal data) to the shared dataset used to improve PID accuracy,
 * DTC coverage and vehicle-profile detection.
 *
 * GDPR requirement this encodes: consent must be a specific, informed, freely
 * given, unambiguous, opt-in action — never inferred from installing the app.
 * [NOT_ASKED] and [DECLINED] behave identically for upload purposes (nothing is
 * sent); they are kept distinct only so the UI knows whether to show the
 * consent screen or a "you declined, change in settings" state.
 */
enum class ContributionConsentState { NOT_ASKED, GRANTED, DECLINED, REVOKED }

data class ContributionConsentRecord(
    val state: ContributionConsentState = ContributionConsentState.NOT_ASKED,
    val consentVersion: Int = 0,
    val decidedAtEpochMs: Long? = null
) {
    fun isActive(currentConsentVersion: Int): Boolean =
        state == ContributionConsentState.GRANTED && consentVersion == currentConsentVersion
}

interface ConsentStore {
    fun current(): ContributionConsentRecord
    fun save(record: ContributionConsentRecord)
}

class InMemoryConsentStore(
    initial: ContributionConsentRecord = ContributionConsentRecord()
) : ConsentStore {
    private var record = initial
    override fun current(): ContributionConsentRecord = record
    override fun save(record: ContributionConsentRecord) {
        this.record = record
    }
}
