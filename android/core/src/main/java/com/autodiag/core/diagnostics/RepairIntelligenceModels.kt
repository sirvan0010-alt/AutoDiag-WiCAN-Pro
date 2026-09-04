package com.autodiag.core.diagnostics

/**
 * Stable data model for future DTC -> diagnosis -> repair intelligence.
 * A DTC is evidence, not an automatic part failure verdict.
 */
enum class VerificationState { UNVERIFIED, PARTIALLY_VERIFIED, VERIFIED }
enum class SourceAccess { PUBLIC, LICENSED, OEM_AUTHENTICATED, COMMUNITY, UNKNOWN }
enum class RepairProcedureType { DIAGNOSIS, MECHANICAL_REPAIR, ELECTRICAL_REPAIR, WIRING, CALIBRATION, PROGRAMMING, POST_REPAIR_CHECK }
enum class LaborTimeType { OEM_FLAT_RATE, PROFESSIONAL_STANDARD, INDEPENDENT_ESTIMATE, USER_CONFIGURED }
enum class PriceType { OEM_RETAIL, PUBLISHED_RETAIL, ESTIMATE, USER_CONFIGURED }

/** Normalized DTC knowledge record used by the repair-intelligence layer. */
data class DtcKnowledgeEntry(
    val code: String,
    val explanationCs: String? = null,
    val causesCs: List<String> = emptyList(),
    val checksCs: List<String> = emptyList(),
)

data class VehicleScope(
    val make: String? = null, val model: String? = null, val generation: String? = null,
    val modelYear: Int? = null, val productionDate: String? = null, val engineOrMotor: String? = null,
    val batteryVariant: String? = null, val transmission: String? = null, val drivetrain: String? = null,
    val region: String? = null, val ecu: String? = null, val softwareVersion: String? = null, val vin: String? = null
)

data class RepairSource(
    val id: String, val provider: String, val label: String, val sourceType: String, val access: SourceAccess,
    val url: String? = null, val revision: String? = null, val publishedAt: String? = null,
    val verification: VerificationState = VerificationState.UNVERIFIED, val notes: String? = null
)

data class RepairPart(
    val component: String, val oemPartNumber: String? = null, val manufacturerPartNumber: String? = null,
    val supersedes: List<String> = emptyList(), val quantity: Int = 1, val mandatory: Boolean = true,
    val newPartRequired: Boolean? = null, val sourceId: String? = null, val price: Double? = null,
    val currency: String? = null, val priceType: PriceType? = null,
    val confidence: VerificationState = VerificationState.UNVERIFIED
)

data class LaborEstimate(
    val operation: String, val hours: Double, val timeType: LaborTimeType, val sourceId: String? = null,
    val correctionCode: String? = null, val hourlyRate: Double? = null, val currency: String? = null,
    val confidence: VerificationState = VerificationState.UNVERIFIED
)

data class PriceEstimate(val min: Double, val max: Double, val currency: String, val priceType: PriceType, val sourceId: String? = null) {
    init { require(min >= 0.0); require(max >= min) }
}

data class RepairProcedure(
    val id: String, val title: String, val type: RepairProcedureType, val vehicleScope: VehicleScope, val sourceId: String,
    val tools: List<String> = emptyList(), val prerequisites: List<String> = emptyList(), val safetyNotes: List<String> = emptyList(),
    val steps: List<String> = emptyList(), val torqueNotes: List<String> = emptyList(), val postRepairChecks: List<String> = emptyList(),
    val estimatedLabor: LaborEstimate? = null, val verification: VerificationState = VerificationState.UNVERIFIED
)

data class RepairEstimate(
    val parts: List<RepairPart> = emptyList(), val labor: List<LaborEstimate> = emptyList(), val currency: String = "EUR",
    val totalMin: Double, val totalMax: Double
) { init { require(totalMin >= 0.0); require(totalMax >= totalMin) } }

data class RepairIntelligence(
    val dtcCode: String, val ecu: String? = null, val vehicleScope: VehicleScope = VehicleScope(), val meaning: String? = null,
    val severity: String? = null, val possibleCauses: List<String> = emptyList(), val diagnosticChecks: List<String> = emptyList(),
    val candidateParts: List<RepairPart> = emptyList(), val procedures: List<RepairProcedure> = emptyList(), val estimate: RepairEstimate? = null,
    val sources: List<RepairSource> = emptyList(), val verification: VerificationState = VerificationState.UNVERIFIED
)

fun RepairIntelligence.hasDeterministicPartVerdict(): Boolean =
    candidateParts.size == 1 && candidateParts.single().confidence == VerificationState.VERIFIED &&
        sources.any { it.verification == VerificationState.VERIFIED }
