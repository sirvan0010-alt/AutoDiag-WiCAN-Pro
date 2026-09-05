package com.autodiag.core.capability

/** Resolves source-derived Outlander decoder candidates without guessing a vehicle variant. */
object OutlanderPhevDecoderResolver {
    sealed interface Resolution {
        data object NotFound : Resolution
        data class Ambiguous(val signalId: String, val variantIds: List<String>) : Resolution
        data class Resolved(val definition: SignalDecoderDefinition) : Resolution
    }

    fun resolve(
        candidates: List<SignalDecoderDefinition>,
        signalId: String
    ): Resolution {
        val matches = candidates.filter { it.signalId == signalId }
        if (matches.isEmpty()) return Resolution.NotFound

        val variants = matches.map { it.variantId }.distinct()
        if (matches.size != 1 || variants.size != 1) {
            return Resolution.Ambiguous(signalId, variants)
        }
        return Resolution.Resolved(matches.single())
    }
}
