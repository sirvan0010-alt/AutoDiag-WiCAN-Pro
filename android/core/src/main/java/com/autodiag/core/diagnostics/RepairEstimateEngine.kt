package com.autodiag.core.diagnostics

/**
 * Transparent estimate calculator. It never invents missing part/labor prices.
 * A result is produced only when every required cost is known or user-configured.
 */
object RepairEstimateEngine {
    fun calculate(
        parts: List<RepairPart>,
        labor: List<LaborEstimate>,
        currency: String = "EUR",
        fallbackHourlyRate: Double? = null
    ): RepairEstimate? {
        val partTotals = parts.map { part ->
            val price = part.price?.takeIf { it >= 0.0 } ?: return null
            price * part.quantity
        }
        val laborTotals = labor.map { item ->
            val rate = item.hourlyRate?.takeIf { it >= 0.0 }
                ?: fallbackHourlyRate?.takeIf { it >= 0.0 }
                ?: return null
            rate * item.hours
        }
        val total = partTotals.sum() + laborTotals.sum()
        return RepairEstimate(
            parts = parts,
            labor = labor,
            currency = currency,
            totalMin = total,
            totalMax = total
        )
    }
}

object RepairIntelligenceResolver {
    fun resolve(
        dtc: DtcKnowledgeEntry,
        vehicleScope: VehicleScope = VehicleScope(),
        sources: List<RepairSource> = emptyList(),
        parts: List<RepairPart> = emptyList(),
        procedures: List<RepairProcedure> = emptyList(),
        estimate: RepairEstimate? = null
    ): RepairIntelligence = RepairIntelligence(
        dtcCode = dtc.code,
        vehicleScope = vehicleScope,
        meaning = dtc.explanationCs,
        possibleCauses = dtc.causesCs,
        diagnosticChecks = dtc.checksCs,
        candidateParts = parts,
        procedures = procedures,
        estimate = estimate,
        sources = sources,
        verification = when {
            sources.any { it.verification == VerificationState.VERIFIED } -> VerificationState.VERIFIED
            sources.any { it.verification == VerificationState.PARTIALLY_VERIFIED } -> VerificationState.PARTIALLY_VERIFIED
            else -> VerificationState.UNVERIFIED
        }
    )
}
