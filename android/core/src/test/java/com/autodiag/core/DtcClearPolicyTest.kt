package com.autodiag.core

import com.autodiag.core.obd.DtcClearDecision
import com.autodiag.core.obd.DtcClearPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class DtcClearPolicyTest {
    @Test
    fun exactScopeIsRequiredFirst() {
        assertEquals(
            DtcClearDecision.REQUIRES_EXACT_SCOPE,
            DtcClearPolicy.evaluate(false, true, false, true, true)
        )
    }

    @Test
    fun securityIsRequiredWhenEcuRequiresIt() {
        assertEquals(
            DtcClearDecision.REQUIRES_SECURITY,
            DtcClearPolicy.evaluate(true, true, true, false, true)
        )
    }

    @Test
    fun explicitConfirmationIsRequired() {
        assertEquals(
            DtcClearDecision.ALLOWED_WITH_CONFIRMATION,
            DtcClearPolicy.evaluate(true, true, false, false, false)
        )
    }
}
