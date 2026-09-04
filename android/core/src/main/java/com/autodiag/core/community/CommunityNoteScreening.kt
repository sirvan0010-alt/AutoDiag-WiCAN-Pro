package com.autodiag.core.community

/**
 * Best-effort screening for accidental personal data in the free-text `note`
 * field of a community contribution, before it becomes a public, permanently
 * attributed GitHub Issue tied to the contributor's own account.
 *
 * This is deliberately not silent redaction. Silently stripping suspected PII
 * would let the submission through while hiding the problem from the user —
 * false confidence is worse than no screening at all. Instead this flags the
 * note so the caller (UI layer) can show the user what was found and ask them
 * to edit or explicitly confirm before publishing, per
 * docs/GITHUB_COMMUNITY_CONTRIBUTION_ARCHITECTURE.md: "Never upload VINs,
 * owner identity, addresses, phone numbers, credentials or raw private logs."
 *
 * This is a heuristic safety net, not a guarantee. It catches common,
 * mechanically-detectable patterns; it does not replace the user
 * understanding that this note becomes public under their own identity.
 */
object CommunityNoteScreening {

    enum class Kind { LIKELY_VIN, EMAIL_ADDRESS, PHONE_NUMBER, LONG_DIGIT_SEQUENCE }

    data class Finding(val kind: Kind, val snippet: String)

    private val VIN_PATTERN = Regex("\\b[A-HJ-NPR-Z0-9]{17}\\b")
    private val EMAIL_PATTERN = Regex("\\b[\\w.+-]+@[\\w-]+\\.[\\w.-]+\\b")

    // Deliberately conservative: 8+ digits, optionally separated by
    // spaces/dashes/dots/parentheses/plus, so an ordinary short number (a
    // torque spec, a part number, a DTC-adjacent value) doesn't false-positive.
    private val PHONE_LIKE_PATTERN = Regex("(?:\\+?\\d[\\s().-]?){8,}\\d")
    private val LONG_DIGIT_SEQUENCE_PATTERN = Regex("\\d{9,}")

    fun screen(note: String?): List<Finding> {
        if (note.isNullOrBlank()) return emptyList()
        val findings = mutableListOf<Finding>()

        VIN_PATTERN.findAll(note).forEach { findings += Finding(Kind.LIKELY_VIN, it.value) }
        EMAIL_PATTERN.findAll(note).forEach { findings += Finding(Kind.EMAIL_ADDRESS, it.value) }

        val phoneMatches = PHONE_LIKE_PATTERN.findAll(note).map { it.value.trim() }.toList()
        phoneMatches.forEach { findings += Finding(Kind.PHONE_NUMBER, it) }

        LONG_DIGIT_SEQUENCE_PATTERN.findAll(note).forEach { match ->
            // Don't double-report a digit run already caught as phone-like.
            val alreadyCovered = phoneMatches.any { it.contains(match.value) }
            if (!alreadyCovered) findings += Finding(Kind.LONG_DIGIT_SEQUENCE, match.value)
        }

        return findings
    }

    fun isClean(note: String?): Boolean = screen(note).isEmpty()
}
