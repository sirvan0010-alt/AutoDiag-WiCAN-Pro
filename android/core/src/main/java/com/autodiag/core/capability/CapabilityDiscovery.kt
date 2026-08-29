package com.autodiag.core.capability

import com.autodiag.core.obd.Elm327Session
import java.util.UUID

class CapabilityDiscovery(private val probes: List<CapabilityProbe> = BuiltinProbes.defaultSequence) {
    suspend fun run(session: Elm327Session, sessionId: String = UUID.randomUUID().toString()): Result<CapabilitySnapshot> = runCatching {
        val results = linkedMapOf<String, Capability>()
        var vin: String? = null
        for (probe in probes) {
            val capability = try {
                probe.probe(session)
            } catch (t: Throwable) {
                Capability("probe.error.${results.size}", "Probe", CapabilityStatus.ERROR, t.message)
            }
            results[capability.id] = capability
            if (capability.id == CapabilityIds.OBD_VIN && capability.status == CapabilityStatus.AVAILABLE) vin = capability.detail
        }
        CapabilitySnapshot(
            vehicleIdentity = VehicleIdentity(vin = vin),
            capabilities = results
        )
    }
}
