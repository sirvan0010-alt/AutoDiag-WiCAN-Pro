package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OutlanderPhevCapabilityResolverTest {
    private val vehicle = VehicleDataDefinition(
        vin = "JA4TESTPHEV123456",
        make = "Mitsubishi",
        model = "Outlander PHEV",
        verification = VerificationState.UNVERIFIED,
        provenance = "test"
    )

    private class FakeProvider(
        private val vehicle: VehicleDataDefinition? = null,
        private val signals: List<SignalDataDefinition> = emptyList()
    ) : DiagnosticDataProvider {
        override suspend fun findVehicle(vin: String): VehicleDataDefinition? = vehicle
        override suspend fun findEcu(identity: EcuDataIdentity): EcuDataDefinition? =
            if (signals.isNotEmpty()) EcuDataDefinition(identity, "Test ECU") else null
        override suspend fun findSignals(identity: EcuDataIdentity): List<SignalDataDefinition> = signals
        override suspend fun findDtc(code: String): DtcDataDefinition? = null
    }

    @Test
    fun missingVinFailsClosed() = kotlinx.coroutines.runBlocking {
        val snapshot = CapabilitySnapshot(
            vehicleIdentity = VehicleIdentity(vin = null),
            capabilities = emptyMap()
        )

        val result = OutlanderPhevCapabilityResolver(FakeProvider(vehicle)).resolve(snapshot)

        assertEquals(OutlanderPhevCapabilityResolver.ResolutionStatus.NOT_FOUND, result.status)
        assertNull(result.vehicle)
    }

    @Test
    fun nonOutlanderIsNotMatched() = kotlinx.coroutines.runBlocking {
        val other = vehicle.copy(make = "Tesla", model = "Model Y")
        val snapshot = CapabilitySnapshot(
            vehicleIdentity = VehicleIdentity(vin = vehicle.vin),
            capabilities = emptyMap()
        )

        val result = OutlanderPhevCapabilityResolver(FakeProvider(other)).resolve(snapshot)

        assertEquals(OutlanderPhevCapabilityResolver.ResolutionStatus.NOT_FOUND, result.status)
    }

    @Test
    fun knownSignalOnlyRaisesUnknownToPartial() = kotlinx.coroutines.runBlocking {
        val snapshot = CapabilitySnapshot(
            vehicleIdentity = VehicleIdentity(vin = vehicle.vin),
            capabilities = mapOf(
                "battery.soc" to Capability(
                    id = "battery.soc",
                    displayName = "SOC",
                    status = CapabilityStatus.UNKNOWN
                )
            )
        )
        val signal = SignalDataDefinition(
            id = "battery.soc",
            label = "SOC",
            unit = "%",
            verification = VerificationState.UNVERIFIED,
            provenance = "test"
        )
        val ecu = EcuDataIdentity(ecuId = "BMS")

        val result = OutlanderPhevCapabilityResolver(FakeProvider(vehicle, listOf(signal)))
            .resolve(snapshot, listOf(ecu))

        assertEquals(OutlanderPhevCapabilityResolver.ResolutionStatus.MATCHED, result.status)
        assertEquals(CapabilityStatus.PARTIAL, result.capabilities.getValue("battery.soc").status)
        assertTrue(result.ecus.single().signals.any { it.id == "battery.soc" })
    }

    @Test
    fun emptyProviderNeverInventsCapabilities() = kotlinx.coroutines.runBlocking {
        val snapshot = CapabilitySnapshot(
            vehicleIdentity = VehicleIdentity(vin = vehicle.vin),
            capabilities = mapOf(
                "battery.cell_voltage" to Capability(
                    id = "battery.cell_voltage",
                    displayName = "Cell voltage",
                    status = CapabilityStatus.UNKNOWN
                )
            )
        )

        val result = OutlanderPhevCapabilityResolver().resolve(snapshot, listOf(EcuDataIdentity(ecuId = "BMS")))

        assertEquals(OutlanderPhevCapabilityResolver.ResolutionStatus.NOT_FOUND, result.status)
        assertEquals(CapabilityStatus.UNKNOWN, result.capabilities.getValue("battery.cell_voltage").status)
    }
}
