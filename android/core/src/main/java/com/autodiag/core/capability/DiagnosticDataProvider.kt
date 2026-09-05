package com.autodiag.core.capability

/**
 * Optional diagnostic knowledge provider. Implementations may use local cache,
 * bundled data or a remote database. The diagnostic engine must remain usable
 * when no provider is available.
 */
interface DiagnosticDataProvider {
    suspend fun findVehicle(vin: String): VehicleDataDefinition?
    suspend fun findEcu(identity: EcuDataIdentity): EcuDataDefinition?
    suspend fun findSignals(identity: EcuDataIdentity): List<SignalDataDefinition>
    suspend fun findDtc(code: String): DtcDataDefinition?

    /**
     * Returns source-defined decoder candidates for a request.
     * A null variantId deliberately returns all matching variants so callers
     * cannot silently guess when the same request has multiple layouts.
     */
    suspend fun findDecoderCandidates(request: String, variantId: String? = null): List<SignalDecoderDefinition> = emptyList()
}

data class EcuDataIdentity(
    val ecuId: String? = null,
    val manufacturer: String? = null,
    val hardwareNumber: String? = null,
    val softwareNumber: String? = null,
    val softwareVersion: String? = null
)

data class VehicleDataDefinition(
    val vin: String,
    val make: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val verification: VerificationState = VerificationState.UNVERIFIED,
    val provenance: String = "diagnostic-data"
)

data class EcuDataDefinition(
    val identity: EcuDataIdentity,
    val displayName: String,
    val verification: VerificationState = VerificationState.UNVERIFIED,
    val provenance: String = "diagnostic-data"
)

data class SignalDataDefinition(
    val id: String,
    val label: String,
    val unit: String? = null,
    val request: String? = null,
    val scale: Double = 1.0,
    val offset: Double = 0.0,
    val verification: VerificationState = VerificationState.UNVERIFIED,
    val provenance: String = "diagnostic-data"
)

/** A decoder definition kept in diagnostic-data rather than vehicle-specific Kotlin. */
data class SignalDecoderDefinition(
    val signalId: String,
    val label: String,
    val request: String,
    val variantId: String,
    val decoder: DataDecoderSpec,
    val verification: VerificationState = VerificationState.UNVERIFIED,
    val provenance: String = "diagnostic-data",
    /** Optional diagnostic CAN request/response IDs when the source proves them. */
    val requestCanId: Int? = null,
    val responseCanId: Int? = null
)

data class DtcDataDefinition(
    val code: String,
    val description: String? = null,
    val system: String? = null,
    val verification: VerificationState = VerificationState.UNVERIFIED,
    val provenance: String = "diagnostic-data"
)

/** No-data implementation: absence of a database never becomes an error. */
object EmptyDiagnosticDataProvider : DiagnosticDataProvider {
    override suspend fun findVehicle(vin: String): VehicleDataDefinition? = null
    override suspend fun findEcu(identity: EcuDataIdentity): EcuDataDefinition? = null
    override suspend fun findSignals(identity: EcuDataIdentity): List<SignalDataDefinition> = emptyList()
    override suspend fun findDtc(code: String): DtcDataDefinition? = null
}
