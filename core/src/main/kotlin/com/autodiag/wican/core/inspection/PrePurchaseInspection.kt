package com.autodiag.wican.core.inspection

/** Read-only stages used by the pre-purchase wizard. */
enum class InspectionStage {
    SAFETY_GATE,
    CAPABILITY_DISCOVERY,
    VEHICLE_IDENTITY,
    MARKET_CHECK,
    ECU_TOPOLOGY,
    DTC_ALERTS,
    ODOMETER_CROSS_CHECK,
    BATTERY_HEALTH,
    HV_ISOLATION,
    THERMAL,
    CHARGING,
    DRIVE_UNIT,
    ICE_HEALTH,
    BUS_HEALTH,
    ANALYSIS,
    REPORT,
    REPLAY
}

enum class InspectionResultState { PASS, OBSERVATION, WARNING, NOT_AVAILABLE, ERROR }

data class InspectionFinding(
    val stage: InspectionStage,
    val state: InspectionResultState,
    val title: String,
    val detail: String,
    val evidenceIds: List<String> = emptyList(),
    val confidence: Double? = null
)

data class InspectionReportDigest(
    val reportId: String,
    val schema: String,
    val datasetSha256: String
)

/**
 * Unsupported capability is deliberately represented as NOT_AVAILABLE rather than FAIL.
 */
fun unsupportedFinding(stage: InspectionStage, title: String): InspectionFinding =
    InspectionFinding(
        stage = stage,
        state = InspectionResultState.NOT_AVAILABLE,
        title = title,
        detail = "Vehicle/interface does not expose verified data for this check."
    )
