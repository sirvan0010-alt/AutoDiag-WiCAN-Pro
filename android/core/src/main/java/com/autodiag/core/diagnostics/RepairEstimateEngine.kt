package com.autodiag.core.diagnostics

/**
 * Transparent estimate calculator. It intentionally returns a range and never invents
 * missing part/labor prices. Callers may provide user-configured rates when permitted.
 */
object RepairEstimateEngine {
    fun calculate(
        parts: List<RepairPart>,
        labor: List<LaborEstimate>,
        currency: String = "EUR",
        fallbackHourlyRate: Double? = null
    ): RepairEstimate? {
        val knownParts = parts.mapNotNull { part ->
            part.price?.takeIf { it >= 0.0 }?.let { it * part.quantity }
        }
        val knownLabor = labor.mapNotNull { item ->
            item.hourlyRate?.takeIf { it >= 0.0 }?.let { it * item.hours }
                ?: fallbackHourlyRate?.takeIf { it >= 0.0 }?.let { it * item.hours }
        }

        if (knownParts.isEmpty() && knownLabor.isEmpty()) return null

        val knownTotal = knownParts.sum() + knownLabor.sum()
        val unknownPartCount = parts.count { it.price == null }
        val unknownLaborCount = labor.count {
            it.hourlyRate == null && fallbackHourlyRate == null
        }

        // Unknown values widen the estimate only as a transparent lower/upper range.
        // No guessed automotive prices are inserted into the model.
        val minimum = knownTotal
        val maximum = if (unknownPartCount == 0 && unknownLaborCount == 0) {
            knownTotal
        } else {
            Double.POSITIVE_INFINITY
        }

        return RepairEstimate(
            parts = parts,
            labor = labor,
            currency = currency,
            totalMin = minimum,
            totalMax = maximum
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
