package com.autodiag.core.contribution

data class PidObservationSummary(
    val pid: Int,
    val unit: String?,
    val sampleCount: Int,
    val min: Double,
    val max: Double,
    val mean: Double,
    val hadDecodeFailure: Boolean
)

data class DtcObservationSummary(
    val code: String,
    val occurrenceCount: Int,
    val ecuAddressHint: String?
)

data class ContributionRecord(
    val contributionId: String,
    val schemaVersion: Int,
    val consentVersion: Int,
    val vehicleScope: VehicleScope?,
    val ecuSoftwareHint: String?,
    val adapterFirmwareHint: String?,
    val monthBucket: String,
    val pidObservations: List<PidObservationSummary>,
    val dtcObservations: List<DtcObservationSummary>,
    val appVersion: String
)
