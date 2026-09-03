package com.autodiag.core.community

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubCommunityTest {
    @Test
    fun device_flow_transitions_through_user_authorization() {
        val flow = GitHubDeviceAuthFlow()
        flow.beginRequest()
        assertEquals(GitHubDeviceAuthState.RequestingCode, flow.state)
        flow.codeReceived(GitHubDeviceCode("device", "USERCODE", "https://github.com/login/device", 900, 5))
        assertTrue(flow.state is GitHubDeviceAuthState.AwaitingUserAuthorization)
        flow.beginAuthorization()
        assertEquals(GitHubDeviceAuthState.Authorizing, flow.state)
        flow.authorized(GitHubAccessToken("token"))
        assertTrue(flow.state is GitHubDeviceAuthState.Authorized)
    }

    @Test
    fun issue_payload_contains_no_vin_field() {
        val payload = GitHubContributionPublisher().buildIssue(
            CommunityRepairContribution(
                dtcCode = "p0301",
                ecu = "0x7E0",
                memory = "STORED",
                vehicle = CommunityVehicleScope(make = "Škoda", model = "Fabia", modelYear = 2018),
                category = "PART_SENSOR",
                costBucket = "UNDER_500",
                timeBucket = "UNDER_HOUR"
            )
        )
        assertTrue(payload.body.contains("P0301"))
        assertTrue(payload.body.contains("Škoda"))
        assertTrue(!payload.body.contains("VIN"))
        assertEquals(listOf("community-contribution", "unverified"), payload.labels)
    }
}
