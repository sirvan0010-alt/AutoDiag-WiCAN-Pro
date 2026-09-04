package com.autodiag.core.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VehicleProfileMatcherTest {
    private fun profile(id: String, ecu: String? = null, software: String? = null) =
        VehicleSignalProfile(
            identity = VehicleProfileIdentity(
                id = id,
                manufacturer = "VW",
                model = "Fabia",
                ecuId = ecu,
                softwareId = software,
            ),
        )

    @Test
    fun exact_software_match_beats_generic_ecu_match() {
        val generic = profile("generic", ecu = "MED17")
        val exact = profile("exact", ecu = "MED17", software = "SW123")
        val result = VehicleProfileMatcher.best(
            ObservedEcuIdentity("VW", "Fabia", "MED17", softwareId = "SW123"),
            listOf(generic, exact),
        )
        assertEquals("exact", result!!.profile.identity.id)
        assertEquals(listOf("manufacturer", "model", "ecuId", "softwareId"), result.matchedFields)
    }

    @Test
    fun conflicting_known_identity_disqualifies_profile() {
        val result = VehicleProfileMatcher.best(
            ObservedEcuIdentity("VW", "Fabia", "MED17", softwareId = "SW999"),
            listOf(profile("wrong", ecu = "MED17", software = "SW123")),
        )
        assertNull(result)
    }

    @Test
    fun unknown_observed_fields_do_not_create_a_match() {
        val result = VehicleProfileMatcher.best(
            ObservedEcuIdentity(),
            listOf(profile("profile", ecu = "MED17")),
        )
        assertNull(result)
    }
}
