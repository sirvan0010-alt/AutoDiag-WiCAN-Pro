package com.autodiag.core.obd

import kotlin.test.Test
import kotlin.test.assertEquals

class LiveDataFreshnessPolicyTest {
    @Test fun evaluatesFreshAgingAndStale() {
        val policy = LiveDataFreshnessPolicy(freshAfterMs = 1_500, staleAfterMs = 4_000)
        assertEquals(LiveDataFreshness.FRESH, policy.evaluate(10_000, 11_500))
        assertEquals(LiveDataFreshness.AGING, policy.evaluate(10_000, 11_501))
        assertEquals(LiveDataFreshness.AGING, policy.evaluate(10_000, 14_000))
        assertEquals(LiveDataFreshness.STALE, policy.evaluate(10_000, 14_001))
    }
}
