package com.autodiag.core.phev

/**
 * Measurement *names* we want for PHEV/EV. No CAN ID, no Mode 21 payload
 * copied from PHEV Watchdog. Discovery must fill availability later.
 */
enum class PhevCapabilityId {
    BATTERY_DISPLAYED_SOC,
    BATTERY_CURRENT,
    BATTERY_MAX_IN_POWER,
    BATTERY_MAX_OUT_POWER,
    AUX_BATTERY_VOLTAGE,
    CELL_VOLTAGE_MIN,
    CELL_VOLTAGE_MAX,
    CELL_VOLTAGE_AVG,
    MODULE_STATUS,
    FRONT_MOTOR,
    REAR_MOTOR,
    OBC,
    BATTERY_COOL_FAN_PWM,
    AC_DC_CHARGED_ENERGY,
    AC_DC_DISCHARGED_ENERGY,
    BATTERY_CYCLE_DETER,
}

enum class PhevAvailability { UNKNOWN, UNAVAILABLE, AVAILABLE_VERIFIED }

data class PhevCapability(
    val id: PhevCapabilityId,
    val availability: PhevAvailability = PhevAvailability.UNKNOWN,
    val unit: String,
    val noteCs: String,
)

object PhevCapabilityCatalog {
    val defaults: List<PhevCapability> = listOf(
        PhevCapability(PhevCapabilityId.BATTERY_DISPLAYED_SOC, unit = "%", noteCs = "Zobrazené SOC"),
        PhevCapability(PhevCapabilityId.BATTERY_CURRENT, unit = "A", noteCs = "Proud packu"),
        PhevCapability(PhevCapabilityId.BATTERY_MAX_IN_POWER, unit = "kW", noteCs = "Max příkon"),
        PhevCapability(PhevCapabilityId.BATTERY_MAX_OUT_POWER, unit = "kW", noteCs = "Max výkon"),
        PhevCapability(PhevCapabilityId.AUX_BATTERY_VOLTAGE, unit = "V", noteCs = "12V pomocná"),
        PhevCapability(PhevCapabilityId.CELL_VOLTAGE_MIN, unit = "V", noteCs = "Min článek"),
        PhevCapability(PhevCapabilityId.CELL_VOLTAGE_MAX, unit = "V", noteCs = "Max článek"),
        PhevCapability(PhevCapabilityId.CELL_VOLTAGE_AVG, unit = "V", noteCs = "Průměr článků"),
        PhevCapability(PhevCapabilityId.MODULE_STATUS, unit = "", noteCs = "Stav modulů"),
        PhevCapability(PhevCapabilityId.FRONT_MOTOR, unit = "", noteCs = "Přední motor"),
        PhevCapability(PhevCapabilityId.REAR_MOTOR, unit = "", noteCs = "Zadní motor"),
        PhevCapability(PhevCapabilityId.OBC, unit = "", noteCs = "On-board charger"),
        PhevCapability(PhevCapabilityId.BATTERY_COOL_FAN_PWM, unit = "%", noteCs = "Ventilátor BMS"),
        PhevCapability(PhevCapabilityId.AC_DC_CHARGED_ENERGY, unit = "kWh", noteCs = "Nabito AC/DC"),
        PhevCapability(PhevCapabilityId.AC_DC_DISCHARGED_ENERGY, unit = "kWh", noteCs = "Vybito"),
        PhevCapability(PhevCapabilityId.BATTERY_CYCLE_DETER, unit = "", noteCs = "Degradace cyklů"),
    )
}
