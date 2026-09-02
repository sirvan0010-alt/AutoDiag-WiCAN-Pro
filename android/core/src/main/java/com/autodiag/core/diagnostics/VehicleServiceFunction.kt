package com.autodiag.core.diagnostics

/**
 * Complete planning catalog of service functions discussed for AutoDiag.
 * Catalog membership does not claim that WiCAN PRO or a vehicle currently supports execution.
 */
enum class ServiceExecutionMode {
    READ_ONLY,
    SIMULATOR_ONLY,
    USER_CONFIRMATION_REQUIRED,
    QUALIFIED_SERVICE_REQUIRED,
    OEM_AUTH_REQUIRED,
    ADDITIONAL_HARDWARE_REQUIRED,
    DISABLED
}

enum class ServiceAvailability {
    AVAILABLE,
    AVAILABLE_WITH_PREREQUISITES,
    REQUIRES_OEM_SECURITY,
    REQUIRES_ADDITIONAL_HARDWARE,
    NOT_SUPPORTED,
    UNKNOWN
}

data class VehicleServiceFunction(
    val id: String,
    val displayName: String,
    val category: String,
    val availability: ServiceAvailability = ServiceAvailability.UNKNOWN,
    val executionMode: ServiceExecutionMode = ServiceExecutionMode.DISABLED,
    val prerequisites: List<String> = emptyList(),
    val notes: String? = null
)

object ServiceFunctionCatalog {
    val all: List<VehicleServiceFunction> = listOf(
        "AC_CALIBRATION" to "AC CALIBRATION",
        "ADBLUE_RESET" to "ADBLUE RESET",
        "AFS_CALIBRATION" to "AFS CALIBRATION",
        "AIRBAG_RESET" to "AIRBAG RESET",
        "AF_ADAPTATION" to "A/F ADAPTATION",
        "BMS_ADAPTATION" to "BMS ADAPTATION",
        "BRAKE_PADS_INDICATOR" to "BRAKE PADS INDICATOR",
        "ABS_BLEEDING" to "ABS BLEEDING",
        "CLUTCH_ADAPTATION" to "CLUTCH ADAPTATION",
        "COOLANT_BLEEDING" to "COOLANT BLEEDING",
        "ECU_CODING" to "ECU CODING",
        "DPF_REGENERATION" to "DPF REGENERATION",
        "EGR_SELF_LEARNING" to "EGR SELF-LEARNING",
        "ENGINE_POWER_BALANCE" to "ENGINE POWER BALANCE",
        "ETS_ADAPTATION" to "ETS ADAPTATION",
        "FRM_RESET" to "FRM RESET (BMW/MINI)",
        "GATEWAY_CALIBRATION" to "GATEWAY CALIBRATION",
        "GPF_REGENERATION" to "GPF REGENERATION",
        "GEAR_ADAPTATION" to "GEAR ADAPTATION",
        "GEARBOX_ADAPTATION" to "GEARBOX ADAPTATION",
        "HIGH_VOLTAGE_BATTERY_TEST" to "HIGH VOLTAGE BATTERY TEST",
        "ICCS_CALIBRATION" to "ICCS CALIBRATION",
        "IMMO" to "IMMO",
        "IMMO_PROG" to "IMMO PROG",
        "INJECTOR_CODE" to "INJECTOR CODE",
        "LANGUAGE" to "LANGUAGE",
        "MOTOR_ANGLE_CALIBRATION" to "MOTOR ANGLE CALIBRATION",
        "NOX_SENSOR_RESET" to "NOx SENSOR RESET",
        "ODOMETER" to "ODOMETER",
        "OIL_RESET" to "OIL RESET",
        "RAIN_LIGHT_SENSOR_ADAPTATION" to "RAIN/LIGHT SENSOR ADAPTATION",
        "START_STOP_SETTINGS" to "START&STOP SETTINGS",
        "SUSPENSION_CALIBRATION" to "SUSPENSION CALIBRATION",
        "SEATS_CALIBRATION" to "SEATS CALIBRATION",
        "SUNROOF_INITIALIZATION" to "SUNROOF INITIALIZATION",
        "SAS_CALIBRATION" to "SAS CALIBRATION",
        "TRANSPORT_MODE" to "TRANSPORT MODE",
        "TURBOCHARGING_CALIBRATION" to "TURBOCHARGING CALIBRATION",
        "TIRE_MODIFICATION" to "TIRE MODIFICATION",
        "TPMS_RESET" to "TPMS RESET",
        "WINDOWS_CALIBRATION" to "WINDOWS CALIBRATION"
    ).map { (id, name) ->
        VehicleServiceFunction(id, name, categoryFor(id))
    }

    private fun categoryFor(id: String): String = when {
        id.contains("BATTERY") || id.contains("BMS") -> "EV / Battery"
        id.contains("ABS") || id.contains("BRAKE") || id.contains("SAS") || id.contains("SUSPENSION") -> "Chassis / Safety"
        id.contains("IMMO") || id.contains("ODOMETER") -> "Security / Restricted"
        id.contains("DPF") || id.contains("GPF") || id.contains("EGR") || id.contains("NOX") || id.contains("ADBLUE") -> "Emissions"
        id.contains("GEAR") || id.contains("CLUTCH") -> "Transmission"
        id.contains("CALIBRATION") || id.contains("ADAPTATION") || id.contains("LEARNING") -> "Adaptation / Calibration"
        else -> "Service / Configuration"
    }

    fun find(id: String): VehicleServiceFunction? = all.firstOrNull { it.id == id }
}
