package com.autodiag.core

import com.autodiag.core.contribution.*
import org.junit.Assert.*
import org.junit.Test

class ContributionConsentManagerTest {
    private fun sampleRecord() = ContributionRecord("r1", 1, 1, null, null, null, "2026-09", emptyList(), emptyList(), "0.1-dev")

    @Test fun defaultsToNotAskedAndInactive() {
        val manager = ContributionConsentManager(InMemoryConsentStore(), InMemoryContributionStore(), ContributionConfig())
        assertEquals(ContributionConsentState.NOT_ASKED, manager.state().state)
        assertFalse(manager.hasActiveConsent())
    }

    @Test fun grantMakesConsentActiveForCurrentVersion() {
        val config = ContributionConfig(currentConsentVersion = 1)
        val manager = ContributionConsentManager(InMemoryConsentStore(), InMemoryContributionStore(), config)
        manager.grant(1L)
        assertTrue(manager.hasActiveConsent())
    }

    @Test fun consentVersionBumpInvalidatesOldGrant() {
        val consentStore = InMemoryConsentStore()
        ContributionConsentManager(consentStore, InMemoryContributionStore(), ContributionConfig(currentConsentVersion = 1)).grant(1L)
        val v2 = ContributionConsentManager(consentStore, InMemoryContributionStore(), ContributionConfig(currentConsentVersion = 2))
        assertFalse(v2.hasActiveConsent())
    }

    @Test fun revokePurgesAnyPendingQueuedData() {
        val store = InMemoryContributionStore().apply { enqueue(sampleRecord()) }
        val manager = ContributionConsentManager(InMemoryConsentStore(), store, ContributionConfig())
        manager.grant(1L)
        manager.revoke(2L)
        assertTrue(store.pending().isEmpty())
        assertFalse(manager.hasActiveConsent())
    }

    @Test fun declinePurgesQueueToo() {
        val store = InMemoryContributionStore().apply { enqueue(sampleRecord()) }
        val manager = ContributionConsentManager(InMemoryConsentStore(), store, ContributionConfig())
        manager.decline(1L)
        assertTrue(store.pending().isEmpty())
    }
}
