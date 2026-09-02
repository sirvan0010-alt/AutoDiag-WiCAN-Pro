package com.autodiag.core.diagnostics

import com.autodiag.core.diagnostics.uds.UdsCapabilityContext
import com.autodiag.core.diagnostics.uds.UdsCapabilityDecision
import com.autodiag.core.diagnostics.uds.UdsCapabilityGate
import com.autodiag.core.diagnostics.uds.UdsRequest
import com.autodiag.core.diagnostics.uds.UdsService
import org.junit.Assert.assertEquals
import org.junit.Test

class UdsCapabilityGateTest {
    @Test fun unknownScopeIsRejectedBeforeSupportCheck() {
        val result = UdsCapabilityGate.evaluate(
            UdsRequest(UdsService.WRITE_DATA_BY_IDENTIFIER),
            UdsCapabilityContext(explicitlySupported = true),
        )
        assertEquals(UdsCapabilityDecision.REQUIRES_EXACT_SCOPE, result)
    }

    @Test fun unsupportedWriteDoesNotBecomeAllowed() {
        val result = UdsCapabilityGate.evaluate(
            UdsRequest(UdsService.WRITE_DATA_BY_IDENTIFIER),
            UdsCapabilityContext(exactVehicleAndEcuMatch = true),
        )
        assertEquals(UdsCapabilityDecision.NOT_SUPPORTED, result)
    }

    @Test fun writeRequiresPrerequisitesAndConfirmation() {
        val base = UdsRequest(UdsService.WRITE_DATA_BY_IDENTIFIER)
        val supported = UdsCapabilityContext(exactVehicleAndEcuMatch = true, explicitlySupported = true)

        assertEquals(UdsCapabilityDecision.REQUIRES_PREREQUISITES, UdsCapabilityGate.evaluate(base, supported))
        assertEquals(
            UdsCapabilityDecision.REQUIRES_USER_CONFIRMATION,
            UdsCapabilityGate.evaluate(base, supported.copy(prerequisitesSatisfied = true)),
        )
        assertEquals(
            UdsCapabilityDecision.ALLOWED,
            UdsCapabilityGate.evaluate(base, supported.copy(prerequisitesSatisfied = true, userConfirmed = true)),
        )
    }

    @Test fun securityServiceRequiresSecurityAndConfirmation() {
        val request = UdsRequest(UdsService.SECURITY_ACCESS)
        val supported = UdsCapabilityContext(exactVehicleAndEcuMatch = true, explicitlySupported = true)

        assertEquals(UdsCapabilityDecision.REQUIRES_SECURITY, UdsCapabilityGate.evaluate(request, supported))
        assertEquals(
            UdsCapabilityDecision.REQUIRES_USER_CONFIRMATION,
            UdsCapabilityGate.evaluate(request, supported.copy(securityEstablished = true)),
        )
        assertEquals(
            UdsCapabilityDecision.ALLOWED,
            UdsCapabilityGate.evaluate(request, supported.copy(securityEstablished = true, userConfirmed = true)),
        )
    }
}
