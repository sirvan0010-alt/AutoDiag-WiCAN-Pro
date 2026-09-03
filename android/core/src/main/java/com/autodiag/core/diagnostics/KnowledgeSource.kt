package com.autodiag.core.diagnostics

/** Provenance class for diagnostic knowledge. */
enum class KnowledgeSourceType {
    OEM_PUBLIC,
    OEM_LICENSED,
    PROFESSIONAL_DATABASE,
    COMMUNITY,
    USER_EXPERIENCE,
    IMPORTED_REFERENCE,
    DERIVED_ANALYSIS,
    UNKNOWN
}

enum class KnowledgeVerification {
    UNVERIFIED,
    PARTIALLY_VERIFIED,
    VERIFIED
}

data class KnowledgeSource(
    val type: KnowledgeSourceType,
    val name: String,
    val uri: String? = null,
    val license: String? = null,
    val verification: KnowledgeVerification = KnowledgeVerification.UNVERIFIED,
    val notes: String? = null
)
