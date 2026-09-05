package com.autodiag.core.obd

/** Provenance of a live measurement. Values must never be silently promoted from inferred to measured. */
enum class MeasurementOrigin { MEASURED, CALCULATED, INFERRED }

enum class MeasurementAvailability { AVAILABLE, PARTIAL, UNAVAILABLE, UNKNOWN, ERROR }

enum class VerificationState { UNKNOWN, UNVERIFIED, PARTIALLY_VERIFIED, VERIFIED }

data class LiveDataPoint(
    val id: String,
    val label: String,
    val unit: String,
    val value: Double? = null,
    val timestampMs: Long,
    val source: String? = null,
    val ecu: String? = null,
    val origin: MeasurementOrigin = MeasurementOrigin.MEASURED,
    val availability: MeasurementAvailability = MeasurementAvailability.UNKNOWN,
    val verification: VerificationState = VerificationState.UNKNOWN,
    val rawValue: String? = null
)

data class LiveDataSeries(
    val id: String,
    val maxSamples: Int = 600,
    val samples: List<LiveDataPoint> = emptyList()
) {
    init { require(maxSamples > 0) { "maxSamples must be positive" } }

    fun append(point: LiveDataPoint): LiveDataSeries =
        copy(samples = (samples + point).takeLast(maxSamples))

    fun latest(): LiveDataPoint? = samples.lastOrNull()

    fun numericRange(): ClosedFloatingPointRange<Double>? {
        val values = samples.mapNotNull { it.value }
        if (values.isEmpty()) return null
        return values.minOrNull()!!..values.maxOrNull()!!
    }
}

data class LiveDataSelection(
    val selectedIds: List<String> = emptyList(),
    val maxSelected: Int = 16
) {
    fun toggle(id: String): LiveDataSelection {
        if (id in selectedIds) return copy(selectedIds = selectedIds - id)
        if (selectedIds.size >= maxSelected) return this
        return copy(selectedIds = selectedIds + id)
    }
}

data class SamplingTelemetry(
    val targetHz: Int? = null,
    val actualHz: Double? = null,
    val latencyMs: Double? = null,
    val framesPerSecond: Double? = null,
    val timeoutRate: Double = 0.0,
    val droppedFrames: Long = 0,
    val adaptive: Boolean = true
)
