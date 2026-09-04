package com.autodiag.core.capability

/**
 * Diagnostic scope catalog for Tesla vehicles.
 *
 * This is deliberately a capability description, not a list of guessed Tesla
 * CAN/UDS commands. Actual operations must be enabled only after the current
 * vehicle/ECU/protocol is discovered and verified.
 */
enum class TeslaDiagnosticDomain {
    POWERTRAIN,
    BRAKE_ELECTRONICS,
    BODY_CONTROL,
    AIRBAG,
    PARKING_BRAKE,
    INSTRUMENT_CLUSTER,
    PARK_ASSIST,
    DOOR_ELECTRONICS,
    STEERING,
    INFOTAINMENT,
    BATTERY_MANAGEMENT
}

enum class DiagnosticOperationSafety {
    READ_ONLY,
    SERVICE_TEST,
    SERVICE_PROCEDURE,
    CONFIGURATION,
    CONTROL
}

data class TeslaDiagnosticFunction(
    val id: String,
    val domain: TeslaDiagnosticDomain,
    val name: String,
    val operations: Set<DiagnosticOperationSafety>,
    val description: String,
    val requiresVerifiedTarget: Boolean = true,
    val requiresVehicleSpecificDefinition: Boolean = true
)

object TeslaDiagnosticCapabilities {
    val all: List<TeslaDiagnosticFunction> = listOf(
        TeslaDiagnosticFunction("tesla.powertrain", TeslaDiagnosticDomain.POWERTRAIN, "Řídicí jednotka pohonu", setOf(DiagnosticOperationSafety.READ_ONLY, DiagnosticOperationSafety.SERVICE_TEST, DiagnosticOperationSafety.SERVICE_PROCEDURE, DiagnosticOperationSafety.CONFIGURATION, DiagnosticOperationSafety.CONTROL), "DTC, live data, aktuátory, servisní procedury a přizpůsobení pohonné jednotky."),
        TeslaDiagnosticFunction("tesla.brake_electronics", TeslaDiagnosticDomain.BRAKE_ELECTRONICS, "Jednotka elektroniky brzd", setOf(DiagnosticOperationSafety.READ_ONLY, DiagnosticOperationSafety.SERVICE_TEST, DiagnosticOperationSafety.SERVICE_PROCEDURE), "ABS/ESP stav, rychlosti kol, tlakové a pedálové hodnoty, testy a servisní sekvence."),
        TeslaDiagnosticFunction("tesla.bcm", TeslaDiagnosticDomain.BODY_CONTROL, "Centrální elektronika (BCM)", setOf(DiagnosticOperationSafety.READ_ONLY, DiagnosticOperationSafety.SERVICE_TEST, DiagnosticOperationSafety.CONFIGURATION, DiagnosticOperationSafety.CONTROL), "Stavy vstupů/výstupů, komfortní funkce, osvětlení, stěrače, zámky a konfigurace."),
        TeslaDiagnosticFunction("tesla.airbag", TeslaDiagnosticDomain.AIRBAG, "Řídicí jednotka airbagu", setOf(DiagnosticOperationSafety.READ_ONLY, DiagnosticOperationSafety.SERVICE_TEST, DiagnosticOperationSafety.SERVICE_PROCEDURE, DiagnosticOperationSafety.CONFIGURATION), "Paměť událostí, stav okruhů a diagnostika bezpečnostního systému. Kritické zásahy zůstávají gated."),
        TeslaDiagnosticFunction("tesla.epb", TeslaDiagnosticDomain.PARKING_BRAKE, "Jednotka parkovací brzdy", setOf(DiagnosticOperationSafety.READ_ONLY, DiagnosticOperationSafety.SERVICE_TEST, DiagnosticOperationSafety.SERVICE_PROCEDURE), "Stav EPB, servisní režim, test aktuátoru a kalibrace po servisním zásahu."),
        TeslaDiagnosticFunction("tesla.instrument_cluster", TeslaDiagnosticDomain.INSTRUMENT_CLUSTER, "Přístrojová deska", setOf(DiagnosticOperationSafety.READ_ONLY, DiagnosticOperationSafety.SERVICE_TEST, DiagnosticOperationSafety.SERVICE_PROCEDURE, DiagnosticOperationSafety.CONFIGURATION), "Provozní hodnoty, chybové stavy, test zobrazovacích prvků a servisní konfigurace."),
        TeslaDiagnosticFunction("tesla.park_assist", TeslaDiagnosticDomain.PARK_ASSIST, "Jednotka asistence parkování", setOf(DiagnosticOperationSafety.READ_ONLY, DiagnosticOperationSafety.SERVICE_TEST, DiagnosticOperationSafety.SERVICE_PROCEDURE, DiagnosticOperationSafety.CONFIGURATION), "Senzory, kamery, stav systému, testy a kalibrace po výměně komponent."),
        TeslaDiagnosticFunction("tesla.door_modules", TeslaDiagnosticDomain.DOOR_ELECTRONICS, "Jednotky elektroniky dveří", setOf(DiagnosticOperationSafety.READ_ONLY, DiagnosticOperationSafety.SERVICE_TEST, DiagnosticOperationSafety.SERVICE_PROCEDURE, DiagnosticOperationSafety.CONFIGURATION, DiagnosticOperationSafety.CONTROL), "Okna, zámky, zrcátka, tlačítka, koncové stavy a komfortní funkce."),
        TeslaDiagnosticFunction("tesla.steering", TeslaDiagnosticDomain.STEERING, "Jednotka posilovače řízení", setOf(DiagnosticOperationSafety.READ_ONLY, DiagnosticOperationSafety.SERVICE_TEST, DiagnosticOperationSafety.SERVICE_PROCEDURE), "Moment, proud, napětí, tepelné stavy, chyby a kalibrace snímačů řízení."),
        TeslaDiagnosticFunction("tesla.infotainment", TeslaDiagnosticDomain.INFOTAINMENT, "Jednotka infotainmentu", setOf(DiagnosticOperationSafety.READ_ONLY, DiagnosticOperationSafety.SERVICE_TEST, DiagnosticOperationSafety.SERVICE_PROCEDURE, DiagnosticOperationSafety.CONFIGURATION), "Stav systému, připojená zařízení, diagnostické testy a konfigurace."),
        TeslaDiagnosticFunction("tesla.bms", TeslaDiagnosticDomain.BATTERY_MANAGEMENT, "Systém řízení baterie (BMS)", setOf(DiagnosticOperationSafety.READ_ONLY, DiagnosticOperationSafety.SERVICE_TEST, DiagnosticOperationSafety.SERVICE_PROCEDURE, DiagnosticOperationSafety.CONFIGURATION, DiagnosticOperationSafety.CONTROL), "SOC/SOH, napětí a teploty článků, limity výkonu, izolace, nabíjení, teplotní management a události.")
    )

    fun forDomain(domain: TeslaDiagnosticDomain): List<TeslaDiagnosticFunction> = all.filter { it.domain == domain }
}
