package com.autodiag.core.diagnostics

import com.autodiag.core.diagnostic.EvidenceVerification
import com.autodiag.core.diagnostics.uds.StandardEcuIdentifier
import com.autodiag.core.diagnostics.uds.UdsDidValue
import com.autodiag.core.diagnostics.uds.UdsEcuIdentificationDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class UdsEcuIdentificationTest {
    @Test
    fun standardizedDidMappingMatchesUdsDefinitions() {
        assertEquals(StandardEcuIdentifier.VIN, StandardEcuIdentifier.fromDid(0xF190))
        assertEquals(StandardEcuIdentifier.VEHICLE_MANUFACTURER_ECU_HARDWARE_NUMBER, StandardEcuIdentifier.fromDid(0xF191))
        assertEquals(StandardEcuIdentifier.SYSTEM_SUPPLIER_ECU_HARDWARE_NUMBER, StandardEcuIdentifier.fromDid(0xF192))
        assertEquals(StandardEcuIdentifier.SYSTEM_SUPPLIER_ECU_HARDWARE_VERSION, StandardEcuIdentifier.fromDid(0xF193))
        assertEquals(StandardEcuIdentifier.SYSTEM_SUPPLIER_ECU_SOFTWARE_NUMBER, StandardEcuIdentifier.fromDid(0xF194))
        assertEquals(StandardEcuIdentifier.SYSTEM_SUPPLIER_ECU_SOFTWARE_VERSION, StandardEcuIdentifier.fromDid(0xF195))
    }

    @Test
    fun vinIsDecodedWithoutDiscardingRawBytes() {
        val data = "TMBTEST1234567890".toByteArray(Charsets.US_ASCII)
        val record = UdsEcuIdentificationDecoder.decode(
            UdsDidValue(0xF190, data),
            EvidenceVerification.PARTIALLY_VERIFIED,
        )
        assertEquals(StandardEcuIdentifier.VIN, record.standardIdentifier)
        assertEquals("TMBTEST1234567890", record.decodedText)
        assertArrayEquals(data, record.rawData)
        assertEquals(EvidenceVerification.PARTIALLY_VERIFIED, record.verification)
    }

    @Test
    fun unknownDidRemainsRawAndUnlabeled() {
        val data = byteArrayOf(0x01, 0x02, 0x03)
        val record = UdsEcuIdentificationDecoder.decode(UdsDidValue(0xF1AA, data))
        assertNull(record.standardIdentifier)
        assertNull(record.decodedText)
        assertEquals("01 02 03", record.rawHex())
    }

    @Test
    fun snapshotExposesSupplierAndSoftwareHardwareFields() {
        val snapshot = UdsEcuIdentificationDecoder.snapshot(
            ecuId = "engine",
            values = listOf(
                UdsDidValue(0xF190, "VIN123".toByteArray()),
                UdsDidValue(0xF18A, "SUPPLIER".toByteArray()),
                UdsDidValue(0xF192, "HW-123".toByteArray()),
                UdsDidValue(0xF193, "1.2".toByteArray()),
                UdsDidValue(0xF194, "SW-456".toByteArray()),
                UdsDidValue(0xF195, "9.8".toByteArray()),
            ),
            verification = EvidenceVerification.VERIFIED,
        )
        assertEquals("engine", snapshot.ecuId)
        assertEquals("VIN123", snapshot.vin)
        assertEquals("SUPPLIER", snapshot.supplierIdentifier)
        assertEquals("HW-123", snapshot.hardwareNumber)
        assertEquals("1.2", snapshot.hardwareVersion)
        assertEquals("SW-456", snapshot.softwareNumber)
        assertEquals("9.8", snapshot.softwareVersion)
        assertEquals(EvidenceVerification.VERIFIED, snapshot.verification)
    }
}
