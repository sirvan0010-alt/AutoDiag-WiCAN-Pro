package com.autodiag.core.capability

/** Safe metadata extracted from an opaque diagnostic database record. */
enum class RodFeatureKind {
    DTC, MEASURED_VALUES, ADJUSTMENT, SERVICE, FUNCTION_MULTIPLEX,
    EXPLORATION, IDENTIFICATION, COMPONENT, UNKNOWN
}

data class RodMetadataEntry(
    val fileName: String,
    val sha256: String? = null,
    val sections: Set<String> = emptySet(),
    val featureKinds: Set<RodFeatureKind> = emptySet(),
    val familyHint: String? = null,
    val variantTokens: Set<String> = emptySet(),
    val verification: VerificationState = VerificationState.UNVERIFIED
)

object RodMetadataParser {
    private val featureMap = mapOf(
        "DTC" to RodFeatureKind.DTC,
        "MWB" to RodFeatureKind.MEASURED_VALUES,
        "GES" to RodFeatureKind.ADJUSTMENT,
        "SLV" to RodFeatureKind.SERVICE,
        "FFMUX" to RodFeatureKind.FUNCTION_MULTIPLEX,
        "XPL" to RodFeatureKind.EXPLORATION,
        "SRI" to RodFeatureKind.IDENTIFICATION,
        "CMP" to RodFeatureKind.COMPONENT
    )

    fun parse(fileName: String, sections: Iterable<String>, sha256: String? = null): RodMetadataEntry {
        val normalized = sections.map { it.trim().uppercase() }.filter { it.isNotEmpty() }.toSet()
        val featureKinds = normalized.mapNotNull { featureMap[it] }.toSet()
        val stem = fileName.substringBeforeLast('.', fileName)
        val tokens = stem.split('_').drop(1).filter { it.length >= 2 }.toSet()
        return RodMetadataEntry(
            fileName = fileName,
            sha256 = sha256,
            sections = normalized,
            featureKinds = featureKinds,
            familyHint = tokens.firstOrNull(),
            variantTokens = tokens,
            verification = VerificationState.PARTIALLY_VERIFIED
        )
    }
}

/** Database evidence is intentionally weaker than live vehicle evidence. */
object RodCapabilityEvidence {
    fun capabilityStatus(entry: RodMetadataEntry, feature: RodFeatureKind): Capability {
        val present = feature in entry.featureKinds
        return Capability(
            id = "database.rod.${feature.name.lowercase()}",
            displayName = feature.name,
            status = if (present) CapabilityStatus.PARTIAL else CapabilityStatus.UNKNOWN,
            detail = if (present) "ROD metadata contains section evidence only." else "No ROD section evidence.",
            userMessage = if (present) "Reference database evidence found; vehicle support is not yet proven." else null,
            verification = VerificationState.PARTIALLY_VERIFIED
        )
    }
}
