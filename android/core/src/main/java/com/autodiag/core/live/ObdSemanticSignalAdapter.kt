package com.autodiag.core.live

import com.autodiag.core.automation.SemanticSignal
import com.autodiag.core.obd.LiveDataQuality
import com.autodiag.core.obd.LiveDataSample

/**
 * Boundary adapter from decoded, standardized OBD live data into the generic
 * semantic-signal layer used by read-only automation.
 *
 * Only explicitly registered SAE J1979 PIDs are promoted. Manufacturer-specific
 * identifiers must be supplied by their own verified vehicle-profile adapter.
 */
object ObdSemanticSignalAdapter {
    private val semanticIds = mapOf(
        0x04 to "engine.load",
        0x05 to "engine.coolant_temp",
        0x0C to "engine.rpm",
        0x0D to "vehicle.speed",
        0x0F to "intake.air_temp",
        0x10 to "air.mass_flow",
        0x11 to "throttle.position",
        0x1F to "engine.runtime",
        0x2F to "fuel.level",
        0x33 to "ambient.pressure",
        0x42 to "ecu.voltage",
        0x46 to "ambient.temperature",
        0x5C to "engine.oil_temp",
        0x5E to "engine.fuel_rate",
        0x5F to "engine.torque_demand",
        0x60 to "engine.torque_actual"
    )

    fun toSemanticSignal(sample: LiveDataSample): SemanticSignal? {
        if (sample.quality != LiveDataQuality.GOOD) return null
        val value = sample.value ?: return null
        val id = semanticIds[sample.pid] ?: return null
        if (!value.isFinite() || sample.timestampEpochMs <= 0L) return null
        return SemanticSignal(
            id = id,
            value = value,
            unit = sample.unit,
            timestampMs = sample.timestampEpochMs,
            source = "OBD_J1979_MODE_01"
        )
    }

    fun toSemanticSignals(samples: Iterable<LiveDataSample>): List<SemanticSignal> =
        samples.mapNotNull(::toSemanticSignal)
}
