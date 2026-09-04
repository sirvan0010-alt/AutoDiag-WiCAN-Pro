package com.autodiag.core.capability

/** Diagnostic scope catalog for Tesla vehicles. */
enum class TeslaDiagnosticDomain {
    POWERTRAIN, BRAKE_ELECTRONICS, BODY_CONTROL, AIRBAG, PARKING_BRAKE,
    INSTRUMENT_CLUSTER, PARK_ASSIST, DOOR_ELECTRONICS, STEERING, INFOTAINMENT,
    BATTERY_MANAGEMENT
}

enum class DiagnosticOperationSafety {
    READ_ONLY, SERVICE_TEST, SERVICE_PROCEDURE, CONFIGURATION, CONTROL
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
    private val RO = DiagnosticOperationSafety.READ_ONLY
    private val ST = DiagnosticOperationSafety.SERVICE_TEST
    private val SP = DiagnosticOperationSafety.SERVICE_PROCEDURE
    private val CFG = DiagnosticOperationSafety.CONFIGURATION
    private val CTRL = DiagnosticOperationSafety.CONTROL

    val all: List<TeslaDiagnosticFunction> = listOf(
        TeslaDiagnosticFunction("tesla.powertrain", TeslaDiagnosticDomain.POWERTRAIN, "Řídicí jednotka pohonu", setOf(RO, ST, SP, CFG, CTRL), "DTC, live data, aktuátory, servisní procedury a přizpůsobení pohonné jednotky."),
        TeslaDiagnosticFunction("tesla.brake_electronics", TeslaDiagnosticDomain.BRAKE_ELECTRONICS, "Jednotka elektroniky brzd", setOf(RO, ST, SP), "ABS/ESP stav, rychlosti kol, tlakové a pedálové hodnoty, testy a servisní sekvence."),
        TeslaDiagnosticFunction("tesla.bcm", TeslaDiagnosticDomain.BODY_CONTROL, "Centrální elektronika (BCM)", setOf(RO, ST, CFG, CTRL), "Stavy vstupů/výstupů, komfortní funkce, osvětlení, stěrače, zámky a konfigurace."),
        TeslaDiagnosticFunction("tesla.airbag", TeslaDiagnosticDomain.AIRBAG, "Řídicí jednotka airbagu", setOf(RO, ST, SP, CFG), "Paměť událostí, stav okruhů a diagnostika bezpečnostního systému. Kritické zásahy zůstávají gated."),
        TeslaDiagnosticFunction("tesla.epb", TeslaDiagnosticDomain.PARKING_BRAKE, "Jednotka parkovací brzdy", setOf(RO, ST, SP), "Stav EPB, servisní režim, test aktuátoru a kalibrace po servisním zásahu."),
        TeslaDiagnosticFunction("tesla.instrument_cluster", TeslaDiagnosticDomain.INSTRUMENT_CLUSTER, "Přístrojová deska", setOf(RO, ST, SP, CFG), "Provozní hodnoty, chybové stavy, test zobrazovacích prvků a servisní konfigurace."),
        TeslaDiagnosticFunction("tesla.park_assist", TeslaDiagnosticDomain.PARK_ASSIST, "Jednotka asistence parkování", setOf(RO, ST, SP, CFG), "Senzory, kamery, stav systému, testy a kalibrace po výměně komponent."),
        TeslaDiagnosticFunction("tesla.door_modules", TeslaDiagnosticDomain.DOOR_ELECTRONICS, "Jednotky elektroniky dveří", setOf(RO, ST, SP, CFG, CTRL), "Okna, zámky, zrcátka, tlačítka, koncové stavy a komfortní funkce."),
        TeslaDiagnosticFunction("tesla.steering", TeslaDiagnosticDomain.STEERING, "Jednotka posilovače řízení", setOf(RO, ST, SP), "Moment, proud, napětí, tepelné stavy, chyby a kalibrace snímačů řízení."),
        TeslaDiagnosticFunction("tesla.infotainment", TeslaDiagnosticDomain.INFOTAINMENT, "Jednotka infotainmentu", setOf(RO, ST, SP, CFG), "Stav systému, připojená zařízení, diagnostické testy a konfigurace."),
        TeslaDiagnosticFunction("tesla.bms", TeslaDiagnosticDomain.BATTERY_MANAGEMENT, "Systém řízení baterie (BMS)", setOf(RO, ST, SP, CFG, CTRL), "SOC/SOH, napětí a teploty článků, limity výkonu, izolace, nabíjení, teplotní management a události.")
    )

    fun forDomain(domain: TeslaDiagnosticDomain): List<TeslaDiagnosticFunction> = all.filter { it.domain == domain }
}
