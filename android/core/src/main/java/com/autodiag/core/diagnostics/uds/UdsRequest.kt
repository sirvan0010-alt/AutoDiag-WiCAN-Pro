package com.autodiag.core.diagnostics.uds

/** A normalized UDS request before it reaches a transport implementation. */
data class UdsRequest(
    val service: UdsService,
    val payload: ByteArray = byteArrayOf(),
) {
    init {
        require(service != UdsService.UNKNOWN) { "Unknown UDS service cannot be executed" }
    }

    val serviceId: Int
        get() = service.serviceId
}

/**
 * Capability decision used immediately before a diagnostic operation is sent.
 * UNKNOWN and denied states are deliberately fail-closed.
 */
enum class UdsCapabilityDecision {
    ALLOWED,
    REQUIRES_EXACT_SCOPE,
    REQUIRES_PREREQUISITES,
    REQUIRES_SECURITY,
    REQUIRES_USER_CONFIRMATION,
    NOT_SUPPORTED,
    UNKNOWN,
}

data class UdsCapabilityContext(
    val exactVehicleAndEcuMatch: Boolean = false,
    val prerequisitesSatisfied: Boolean = false,
    val securityEstablished: Boolean = false,
    val userConfirmed: Boolean = false,
    val explicitlySupported: Boolean = false,
)

object UdsCapabilityGate {
    fun evaluate(request: UdsRequest, context: UdsCapabilityContext): UdsCapabilityDecision {
        if (!context.exactVehicleAndEcuMatch) return UdsCapabilityDecision.REQUIRES_EXACT_SCOPE
        if (!context.explicitlySupported) return UdsCapabilityDecision.NOT_SUPPORTED

        return when (request.service.risk) {
            UdsServiceRisk.READ -> UdsCapabilityDecision.ALLOWED
            UdsServiceRisk.SECURITY_CRITICAL -> if (context.securityEstablished && context.userConfirmed) {
                UdsCapabilityDecision.ALLOWED
            } else if (!context.securityEstablished) {
                UdsCapabilityDecision.REQUIRES_SECURITY
            } else {
                UdsCapabilityDecision.REQUIRES_USER_CONFIRMATION
            }
            UdsServiceRisk.PROGRAMMING -> if (context.prerequisitesSatisfied && context.userConfirmed) {
                UdsCapabilityDecision.ALLOWED
            } else if (!context.prerequisitesSatisfied) {
                UdsCapabilityDecision.REQUIRES_PREREQUISITES
            } else {
                UdsCapabilityDecision.REQUIRES_USER_CONFIRMATION
            }
            UdsServiceRisk.SERVICE_WRITE,
            UdsServiceRisk.CONFIG_WRITE,
            UdsServiceRisk.ACTUATOR_CONTROL,
            UdsServiceRisk.STATE_CHANGING,
            UdsServiceRisk.ADVANCED -> if (context.prerequisitesSatisfied && context.userConfirmed) {
                UdsCapabilityDecision.ALLOWED
            } else if (!context.prerequisitesSatisfied) {
                UdsCapabilityDecision.REQUIRES_PREREQUISITES
            } else {
                UdsCapabilityDecision.REQUIRES_USER_CONFIRMATION
            }
            UdsServiceRisk.UNKNOWN -> UdsCapabilityDecision.UNKNOWN
        }
    }
}
