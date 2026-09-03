package com.autodiag.core.obd

/** Quality of a decoded live-data measurement. */
enum class LiveDataQuality {
    GOOD,
    INVALID,
    UNAVAILABLE,
    ERROR
}

/** Age of a measurement relative to the current observation time. */
enum class LiveDataFreshness {
    FRESH,
    AGING,
    STALE
}

/** A decoded Mode 01 measurement together with transport-independent state. */
data class LiveDataSample(
    val pid: Int,
    val labelCs: String,
    val value: Double?,
    val unit: String?,
    val rawHex: String,
    val timestampEpochMs: Long,
    val quality: LiveDataQuality,
    val freshness: LiveDataFreshness,
    val error: String? = null
)
