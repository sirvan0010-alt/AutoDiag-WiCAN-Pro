package com.autodiag.core.community

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityNoteScreeningTest {

    @Test
    fun cleanNotePassesThrough() {
        assertTrue(CommunityNoteScreening.isClean("Vyměnil jsem kyslíkový senzor, kód zmizel."))
        assertTrue(CommunityNoteScreening.screen(null).isEmpty())
        assertTrue(CommunityNoteScreening.screen("").isEmpty())
    }

    @Test
    fun flagsLikelyVin() {
        val findings = CommunityNoteScreening.screen("VIN mého auta je WVWZZZ1JZXW000001, kdyby to pomohlo")
        assertTrue(findings.any { it.kind == CommunityNoteScreening.Kind.LIKELY_VIN })
    }

    @Test
    fun flagsEmailAddress() {
        val findings = CommunityNoteScreening.screen("napište mi na jan.novak@example.com pro detaily")
        assertTrue(findings.any { it.kind == CommunityNoteScreening.Kind.EMAIL_ADDRESS })
    }

    @Test
    fun flagsPhoneNumber() {
        val findings = CommunityNoteScreening.screen("zavolejte na +420 777 123 456")
        assertTrue(findings.any { it.kind == CommunityNoteScreening.Kind.PHONE_NUMBER })
    }

    @Test
    fun doesNotFlagShortTechnicalNumbers() {
        val findings = CommunityNoteScreening.screen("dotažení 25 Nm, kód dílu 1234567")
        assertTrue(findings.none { it.kind == CommunityNoteScreening.Kind.PHONE_NUMBER })
        assertTrue(findings.none { it.kind == CommunityNoteScreening.Kind.LONG_DIGIT_SEQUENCE })
    }
}

class GitHubContributionPublisherPreparePublishTest {

    private fun sampleContribution(note: String?) = CommunityRepairContribution(
        dtcCode = "p0301",
        ecu = "0x7E0",
        memory = "STORED",
        vehicle = CommunityVehicleScope(make = "Škoda", model = "Fabia", modelYear = 2018),
        category = "PART_SENSOR",
        note = note
    )

    @Test
    fun readyWhenNoteIsClean() {
        val result = GitHubContributionPublisher().preparePublish(sampleContribution("Vyměnil jsem zapalovací cívku."))
        assertTrue(result is ContributionPublishResult.Ready)
    }

    @Test
    fun needsReviewWhenNoteContainsLikelyPiiAndNotConfirmed() {
        val result = GitHubContributionPublisher().preparePublish(
            sampleContribution("VIN WVWZZZ1JZXW000001, volejte na +420 777 123 456")
        )
        assertTrue(result is ContributionPublishResult.NeedsReview)
        val findings = (result as ContributionPublishResult.NeedsReview).findings
        assertTrue(findings.any { it.kind == CommunityNoteScreening.Kind.LIKELY_VIN })
        assertTrue(findings.any { it.kind == CommunityNoteScreening.Kind.PHONE_NUMBER })
    }

    @Test
    fun publishesAnywayWhenUserExplicitlyConfirmsAfterReview() {
        val result = GitHubContributionPublisher().preparePublish(
            sampleContribution("VIN WVWZZZ1JZXW000001"),
            userConfirmedNoteIsSafe = true
        )
        assertTrue(result is ContributionPublishResult.Ready)
    }

    @Test
    fun readyResultBuildsSamePayloadAsBuildIssue() {
        val contribution = sampleContribution("Vyměnil jsem zapalovací cívku.")
        val direct = GitHubContributionPublisher().buildIssue(contribution)
        val prepared = GitHubContributionPublisher().preparePublish(contribution)
        assertEquals(direct, (prepared as ContributionPublishResult.Ready).payload)
    }
}
