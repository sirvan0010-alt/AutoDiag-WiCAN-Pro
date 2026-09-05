package com.autodiag.core.telemetry

import com.autodiag.core.obd.LiveDataSample

/** Transport-neutral MQTT/Home Assistant publication contract. */
data class MqttTelemetryRecord(
    val vehicleScope: String,
    val sample: LiveDataSample,
    val publishedAtEpochMs: Long
)

interface MqttTelemetryPublisher {
    suspend fun publish(record: MqttTelemetryRecord): Result<Unit>
}

/** Keeps remote telemetry read-only: no command/control payloads are modeled here. */
class RateLimitedTelemetryPublisher(
    private val delegate: MqttTelemetryPublisher,
    private val minimumIntervalMs: Long = 1000L,
    private val nowEpochMs: () -> Long = { System.currentTimeMillis() }
) : MqttTelemetryPublisher {
    private val lastPublished = mutableMapOf<String, Long>()

    override suspend fun publish(record: MqttTelemetryRecord): Result<Unit> {
        val key = "${record.vehicleScope}:${record.sample.pid}"
        val now = nowEpochMs()
        synchronized(lastPublished) {
            if (now - (lastPublished[key] ?: Long.MIN_VALUE) < minimumIntervalMs) return Result.success(Unit)
            lastPublished[key] = now
        }
        return delegate.publish(record.copy(publishedAtEpochMs = now))
    }
}
