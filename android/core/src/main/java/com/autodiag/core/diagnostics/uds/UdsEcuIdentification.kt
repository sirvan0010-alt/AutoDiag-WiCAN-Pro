package com.autodiag.core.diagnostics.uds

import com.autodiag.core.diagnostic.EvidenceVerification

/**
 * Semantic labels are intentionally limited to standardized UDS DIDs.
 * Unknown or manufacturer-specific DIDs remain raw and are never guessed.
 */
enum class StandardEcuIdentifier(val did: Int, val label: String) {
    VIN(0xF190, "VIN"),
    SYSTEM_SUPPLIER_IDENTIFIER(0xF18A, "System supplier identifier"),
    ECU_MANUFACTURING_DATE(0xF18B, "ECU manufacturing date"),
    ECU_SERIAL_NUMBER(0xF18C, "ECU serial number"),
    VEHICLE_MANUFACTURER_ECU_HARDWARE_NUMBER(0xF191, "Vehicle manufacturer ECU hardware number"),
    SYSTEM_SUPPLIER_ECU_HARDWARE_NUMBER(0xF192, "System supplier ECU hardware number"),
    SYSTEM_SUPPLIER_ECU_HARDWARE_VERSION(0xF193, "System supplier ECU hardware version"),
    SYSTEM_SUPPLIER_ECU_SOFTWARE_NUMBER(0xF194, "System supplier ECU software number"),
    SYSTEM_SUPPLIER_ECU_SOFTWARE_VERSION(0xF195, "System supplier ECU software version"),
    VEHICLE_MANUFACTURER_ECU_SOFTWARE_NUMBER(0xF188, "Vehicle manufacturer ECU software number"),
    VEHICLE_MANUFACTURER_ECU_SOFTWARE_VERSION(0xF189, "Vehicle manufacturer ECU software version"),
    SYSTEM_NAME_OR_ENGINE_TYPE(0xF197, "System name or engine type");

    companion object {
        fun fromDid(did: Int): StandardEcuIdentifier? = entries.firstOrNull { it.did == did }
    }
}

data class EcuIdentifierRecord(
    val did: Int,
    val rawData: ByteArray,
    val standardIdentifier: StandardEcuIdentifier?,
    val decodedText: String?,
    val verification: EvidenceVerification,
) {
    init { require(did in 0..0xFFFF) }

    fun rawHex(): String = rawData.joinToString(" ") { "%02X".format(it.toInt() and 0xFF) }
}

data class EcuIdentificationSnapshot(
    val ecuId: String?,
    val identifiers: List<EcuIdentifierRecord>,
    val verification: EvidenceVerification,
) {
    val vin: String? get() = text(StandardEcuIdentifier.VIN)
    val hardwareNumber: String? get() = text(StandardEcuIdentifier.SYSTEM_SUPPLIER_ECU_HARDWARE_NUMBER)
        ?: text(StandardEcuIdentifier.VEHICLE_MANUFACTURER_ECU_HARDWARE_NUMBER)
    val hardwareVersion: String? get() = text(StandardEcuIdentifier.SYSTEM_SUPPLIER_ECU_HARDWARE_VERSION)
    val softwareNumber: String? get() = text(StandardEcuIdentifier.SYSTEM_SUPPLIER_ECU_SOFTWARE_NUMBER)
        ?: text(StandardEcuIdentifier.VEHICLE_MANUFACTURER_ECU_SOFTWARE_NUMBER)
    val softwareVersion: String? get() = text(StandardEcuIdentifier.SYSTEM_SUPPLIER_ECU_SOFTWARE_VERSION)
        ?: text(StandardEcuIdentifier.VEHICLE_MANUFACTURER_ECU_SOFTWARE_VERSION)
    val supplierIdentifier: String? get() = text(StandardEcuIdentifier.SYSTEM_SUPPLIER_IDENTIFIER)

    private fun text(identifier: StandardEcuIdentifier): String? =
        identifiers.firstOrNull { it.standardIdentifier == identifier }?.decodedText
}

object UdsEcuIdentificationDecoder {
    fun decode(
        didValue: UdsDidValue,
        verification: EvidenceVerification = EvidenceVerification.UNVERIFIED,
    ): EcuIdentifierRecord {
        val standard = StandardEcuIdentifier.fromDid(didValue.did)
        val text = didValue.data
            .toString(Charsets.US_ASCII)
            .trim { it <= ' ' || it == '\u0000' }
            .takeIf { it.isNotEmpty() && it.all { ch -> ch.code in 0x20..0x7E } }
        return EcuIdentifierRecord(
            did = didValue.did,
            rawData = didValue.data.copyOf(),
            standardIdentifier = standard,
            decodedText = text,
            verification = verification,
        )
    }

    fun snapshot(
        ecuId: String?,
        values: List<UdsDidValue>,
        verification: EvidenceVerification = EvidenceVerification.UNVERIFIED,
    ): EcuIdentificationSnapshot = EcuIdentificationSnapshot(
        ecuId = ecuId,
        identifiers = values.map { decode(it, verification) },
        verification = verification,
    )
}
