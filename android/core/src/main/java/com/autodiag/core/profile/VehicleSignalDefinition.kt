package com.autodiag.core.profile

import com.autodiag.core.diagnostic.EvidenceVerification

/**
 * Generic vehicle-signal definition inspired by the field model found in the
 * reference diagnostic apps. It is data only: no manufacturer meaning is
 * inferred from a request string.
 */
data class VehicleSignalDefinition(
    val name: String,
    val shortName: String,
    val request: String,
    val unit: String? = null,
    val minValue: Double? = null,
    val maxValue: Double? = null,
    val header: String? = null,
    val obdEquivalentPid: Int? = null,
    val byteOffset: Int = 0,
    val byteLength: Int = 1,
    val bitOffset: Int? = null,
    val bitLength: Int? = null,
    val scale: Double = 1.0,
    val offset: Double = 0.0,
    val signed: Boolean = false,
    val verification: EvidenceVerification = EvidenceVerification.UNVERIFIED,
    val sourceProfile: String? = null,
) {
    init {
        require(name.isNotBlank())
        require(shortName.isNotBlank())
        require(request.isNotBlank())
        require(byteOffset >= 0)
        // Int-backed decoder currently supports up to 32-bit numeric signals.
        require(byteLength in 1..4)
        require(scale.isFinite() && offset.isFinite())
        require(bitOffset == null || bitOffset in 0..7)
        require(bitLength == null || bitLength in 1..8)
        if (bitOffset != null || bitLength != null) {
            require(bitOffset != null && bitLength != null)
            require(bitOffset + bitLength <= 8)
            require(byteLength == 1)
        }
        require(obdEquivalentPid == null || obdEquivalentPid in 0..0xFF)
    }
}

data class VehicleSignalSample(
    val definition: VehicleSignalDefinition,
    val rawBytes: ByteArray,
    val rawValue: Int,
    val value: Double,
)

/** Strict big-endian byte/bit decoder for a single signal definition. */
object VehicleSignalDecoder {
    fun decode(definition: VehicleSignalDefinition, payload: ByteArray): Result<VehicleSignalSample> = runCatching {
        require(definition.byteOffset + definition.byteLength <= payload.size) { "Signal payload is too short" }
        val rawBytes = payload.copyOfRange(
            definition.byteOffset,
            definition.byteOffset + definition.byteLength,
        )
        var raw = 0
        rawBytes.forEach { raw = (raw shl 8) or (it.toInt() and 0xFF) }

        if (definition.bitOffset != null) {
            val length = definition.bitLength!!
            val mask = (1 shl length) - 1
            raw = (raw ushr definition.bitOffset) and mask
        } else if (definition.signed) {
            val bits = definition.byteLength * 8
            val sign = 1 shl (bits - 1)
            if ((raw and sign) != 0) raw -= (1 shl bits)
        }

        val value = raw * definition.scale + definition.offset
        VehicleSignalSample(definition, rawBytes, raw, value)
    }
}

/** Vehicle/profile identity used for matching definitions without guessing. */
data class VehicleProfileIdentity(
    val id: String,
    val manufacturer: String? = null,
    val model: String? = null,
    val ecuId: String? = null,
    val workshopId: String? = null,
    val softwareId: String? = null,
) {
    init { require(id.isNotBlank()) }
}

data class VehicleSignalProfile(
    val identity: VehicleProfileIdentity,
    val definitions: List<VehicleSignalDefinition> = emptyList(),
)
