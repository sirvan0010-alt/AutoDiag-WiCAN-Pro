package com.autodiag.wican.core.diagnostics

/** Source-linked knowledge entry used by the DTC/alert UI. URL is data, not hard-coded into the UI. */
data class KnowledgeSource(
    val labelCs: String,
    val url: String,
    val sourceType: SourceType,
    val verified: Boolean
)

enum class SourceType { OEM_SERVICE, REGULATORY, COMMUNITY_VERIFIED }

data class DtcKnowledgeEntry(
    val code: String,
    val titleCs: String,
    val explanationCs: String,
    val causesCs: List<String> = emptyList(),
    val checksCs: List<String> = emptyList(),
    val repairNotesCs: List<String> = emptyList(),
    val sources: List<KnowledgeSource> = emptyList()
)

object DtcKnowledgeRepository {
    private val entries = mutableMapOf<String, DtcKnowledgeEntry>()

    fun register(entry: DtcKnowledgeEntry) { entries[entry.code.uppercase()] = entry }
    fun find(code: String): DtcKnowledgeEntry? = entries[code.uppercase()]
}
