package com.autodiag.wican.core.knowledge

enum class KnowledgeSourceType {
    OEM_SERVICE,
    REGULATORY,
    ENGINEERING,
    COMMUNITY_VERIFIED,
    GENERATED_EXPLANATION
}

enum class KnowledgeVerification { UNVERIFIED, PARTIALLY_VERIFIED, VERIFIED }
enum class KnowledgeStatus { ACTIVE, NEEDS_REVIEW, BROKEN, UNAVAILABLE }

data class KnowledgeSource(
    val type: KnowledgeSourceType,
    val title: String,
    val url: String,
    val verification: KnowledgeVerification,
    val scope: String? = null,
    val status: KnowledgeStatus = KnowledgeStatus.ACTIVE,
    val lastCheckedEpochMs: Long? = null
)

data class DiagnosticFindingExplanation(
    val code: String,
    val title: String,
    val meaning: String,
    val impact: String? = null,
    val whyTriggered: String? = null,
    val affectedSystem: String? = null,
    val sources: List<KnowledgeSource> = emptyList()
) {
    fun officialSources(): List<KnowledgeSource> = sources.filter {
        it.type == KnowledgeSourceType.OEM_SERVICE && it.status == KnowledgeStatus.ACTIVE
    }
}
