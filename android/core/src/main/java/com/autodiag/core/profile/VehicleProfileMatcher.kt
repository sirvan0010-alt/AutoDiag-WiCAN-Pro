package com.autodiag.core.profile

/** Observed ECU identity used to select vehicle-specific signal profiles. */
data class ObservedEcuIdentity(
    val manufacturer: String? = null,
    val model: String? = null,
    val ecuId: String? = null,
    val workshopId: String? = null,
    val softwareId: String? = null,
)

data class ProfileMatch(
    val profile: VehicleSignalProfile,
    val score: Int,
    val matchedFields: List<String>,
)

/**
 * Conservative profile matching. A profile only wins when at least one
 * identifying field matches; conflicting known fields disqualify it.
 */
object VehicleProfileMatcher {
    fun rank(
        observed: ObservedEcuIdentity,
        profiles: List<VehicleSignalProfile>,
    ): List<ProfileMatch> = profiles.mapNotNull { profile ->
        val id = profile.identity
        val fields = listOf(
            Triple("manufacturer", observed.manufacturer, id.manufacturer),
            Triple("model", observed.model, id.model),
            Triple("ecuId", observed.ecuId, id.ecuId),
            Triple("workshopId", observed.workshopId, id.workshopId),
            Triple("softwareId", observed.softwareId, id.softwareId),
        )
        val known = fields.filter { it.second != null && it.third != null }
        if (known.any { normalize(it.second!!) != normalize(it.third!!) }) return@mapNotNull null

        val matched = known.map { it.first }
        if (matched.isEmpty()) return@mapNotNull null
        ProfileMatch(profile, score(matched), matched)
    }.sortedWith(compareByDescending<ProfileMatch> { it.score }.thenBy { it.profile.identity.id })

    fun best(
        observed: ObservedEcuIdentity,
        profiles: List<VehicleSignalProfile>,
    ): ProfileMatch? = rank(observed, profiles).firstOrNull()

    private fun score(fields: List<String>): Int = fields.fold(0) { acc, item -> acc +
        when (it) {
            "softwareId" -> 100
            "ecuId" -> 80
            "workshopId" -> 60
            "model" -> 40
            "manufacturer" -> 20
            else -> 0
        }
    }

    private fun normalize(value: String): String = value.trim().uppercase()
}
