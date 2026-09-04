package com.autodiag.core.capability

/**
 * A discovered ECU/module. The topology deliberately stores observations, not
 * guessed CAN addresses or proprietary Tesla identifiers.
 */
data class DiscoveredEcu(
    val ecuId: String,
    val name: String? = null,
    val manufacturer: String? = null,
    val hardwareNumber: String? = null,
    val softwareNumber: String? = null,
    val softwareVersion: String? = null,
    val address: String? = null,
    val protocol: String? = null,
    val verified: Boolean = false
)

data class DiagnosticEcuTopology(
    val vehicle: VehicleIdentity?,
    val ecus: List<DiscoveredEcu> = emptyList()
) {
    fun find(ecuId: String): DiscoveredEcu? = ecus.firstOrNull { it.ecuId == ecuId }
}

data class TeslaDomainObservation(
    val domain: TeslaDiagnosticDomain,
    val ecu: DiscoveredEcu,
    val confidence: VerificationState,
    val reason: String
)

/**
 * Maps only explicit, verified semantic evidence to Tesla domains.
 *
 * Names containing a domain keyword are intentionally NOT enough to create a
 * verified mapping. Callers can pass an explicit verified semantic label from
 * a vehicle-specific definition/profile. This prevents accidental activation
 * of proprietary commands merely because an ECU happens to be present.
 */
object TeslaEcuDomainMapper {
    private val explicitLabels = mapOf(
        "powertrain" to TeslaDiagnosticDomain.POWERTRAIN,
        "drive" to TeslaDiagnosticDomain.POWERTRAIN,
        "brake" to TeslaDiagnosticDomain.BRAKE_ELECTRONICS,
        "abs" to TeslaDiagnosticDomain.BRAKE_ELECTRONICS,
        "bcm" to TeslaDiagnosticDomain.BODY_CONTROL,
        "body" to TeslaDiagnosticDomain.BODY_CONTROL,
        "airbag" to TeslaDiagnosticDomain.AIRBAG,
        "srs" to TeslaDiagnosticDomain.AIRBAG,
        "epb" to TeslaDiagnosticDomain.PARKING_BRAKE,
        "cluster" to TeslaDiagnosticDomain.INSTRUMENT_CLUSTER,
        "park" to TeslaDiagnosticDomain.PARK_ASSIST,
        "door" to TeslaDiagnosticDomain.DOOR_ELECTRONICS,
        "steering" to TeslaDiagnosticDomain.STEERING,
        "infotainment" to TeslaDiagnosticDomain.INFOTAINMENT,
        "bms" to TeslaDiagnosticDomain.BATTERY_MANAGEMENT,
        "battery" to TeslaDiagnosticDomain.BATTERY_MANAGEMENT
    )

    fun map(ecu: DiscoveredEcu, verifiedSemanticLabel: String? = null): TeslaDomainObservation? {
        if (!ecu.verified || verifiedSemanticLabel.isNullOrBlank()) return null
        val domain = explicitLabels[verifiedSemanticLabel.trim().lowercase()] ?: return null
        return TeslaDomainObservation(
            domain = domain,
            ecu = ecu,
            confidence = VerificationState.VERIFIED,
            reason = "Doména byla přiřazena z ověřeného vehicle-specific semantic labelu."
        )
    }
}
