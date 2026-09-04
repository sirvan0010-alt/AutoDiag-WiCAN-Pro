package com.autodiag.core.capability

/**
 * Resolves Mitsubishi Outlander PHEV diagnostic-data knowledge without
 * guessing protocol identifiers or decoder layouts.
 *
 * The resolver is intentionally independent from transport. It consumes the
 * already discovered vehicle/ECU identity and only promotes a capability when
 * the diagnostic-data provider contains a corresponding definition.
 */
class OutlanderPhevCapabilityResolver(
    private val provider: DiagnosticDataProvider = EmptyDiagnosticDataProvider
) {
    data class Result(
        val vehicle: VehicleDataDefinition?,
        val ecus: List<EcuResolution>,
        val capabilities: Map<String, Capability>,
        val status: ResolutionStatus
    )

    data class EcuResolution(
        val identity: EcuDataIdentity,
        val definition: EcuDataDefinition?,
        val signals: List<SignalDataDefinition>
    )

    enum class ResolutionStatus {
        MATCHED,
        PARTIAL,
        NOT_FOUND,
        ERROR
    }

    suspend fun resolve(
        snapshot: CapabilitySnapshot,
        ecuIdentities: List<EcuDataIdentity> = emptyList()
    ): Result {
        val identity = snapshot.vehicleIdentity
        val vin = identity?.vin?.trim()
        if (vin.isNullOrEmpty()) {
            return Result(
                vehicle = null,
                ecus = emptyList(),
                capabilities = snapshot.capabilities,
                status = ResolutionStatus.NOT_FOUND
            )
        }

        return try {
            val vehicle = provider.findVehicle(vin)
            val isOutlander = vehicle?.make.equals("Mitsubishi", ignoreCase = true) &&
                vehicle.model?.contains("Outlander", ignoreCase = true) == true

            if (!isOutlander) {
                return Result(vehicle, emptyList(), snapshot.capabilities, ResolutionStatus.NOT_FOUND)
            }

            val ecuResults = ecuIdentities.map { ecu ->
                EcuResolution(
                    identity = ecu,
                    definition = provider.findEcu(ecu),
                    signals = provider.findSignals(ecu)
                )
            }

            val knownSignalIds = ecuResults
                .flatMap { it.signals }
                .map { it.id }
                .toSet()

            val resolvedCapabilities = snapshot.capabilities.mapValues { (id, capability) ->
                if (id in OUTLANDER_SIGNAL_TO_CAPABILITY &&
                    OUTLANDER_SIGNAL_TO_CAPABILITY[id] in knownSignalIds &&
                    capability.status == CapabilityStatus.UNKNOWN
                ) {
                    capability.copy(
                        status = CapabilityStatus.PARTIAL,
                        detail = "Databázová definice existuje; konkrétní vozidlová odpověď ještě nebyla ověřena.",
                        userMessage = "Definice je známá, ale hodnota nebyla ověřena na tomto vozidle."
                    )
                } else capability
            }

            val matchedEcuCount = ecuResults.count { it.definition != null }
            val signalCount = ecuResults.sumOf { it.signals.size }
            val status = when {
                ecuIdentities.isEmpty() && vehicle != null -> ResolutionStatus.PARTIAL
                matchedEcuCount == ecuIdentities.size && signalCount > 0 -> ResolutionStatus.MATCHED
                matchedEcuCount > 0 || signalCount > 0 -> ResolutionStatus.PARTIAL
                else -> ResolutionStatus.NOT_FOUND
            }

            Result(vehicle, ecuResults, resolvedCapabilities, status)
        } catch (_: Throwable) {
            Result(null, emptyList(), snapshot.capabilities, ResolutionStatus.ERROR)
        }
    }

    companion object {
        /**
         * These are capability IDs, not protocol IDs. A mapping here never
         * creates a CAN/UDS/Mode-22 request; that requires independent evidence.
         */
        private val OUTLANDER_SIGNAL_TO_CAPABILITY = mapOf(
            "battery.soc" to "battery.soc",
            "battery.soh" to "battery.soh",
            "battery.capacity" to "battery.capacity",
            "battery.pack_voltage" to "battery.pack_voltage",
            "battery.pack_current" to "battery.pack_current",
            "battery.max_charge_power" to "battery.max_charge_power",
            "battery.max_discharge_power" to "battery.max_discharge_power",
            "battery.cell_voltage" to "battery.cell_voltage",
            "battery.cell_voltage_min" to "battery.cell_voltage_min",
            "battery.cell_voltage_max" to "battery.cell_voltage_max",
            "battery.cell_voltage_delta" to "battery.cell_voltage_delta",
            "battery.module_temperature" to "battery.module_temperature",
            "battery.cooling_fan_pwm" to "battery.cooling_fan_pwm",
            "battery.internal_resistance" to "battery.internal_resistance",
            "battery.cycle_or_degradation" to "battery.cycle_or_degradation",
            "battery.trip_energy" to "battery.trip_energy",
            "battery.target_voltage" to "battery.target_voltage"
        )
    }
}
