package com.autodiag.core.diagnostics

import com.autodiag.core.obd.DiagnosticTroubleCode
import com.autodiag.core.obd.DtcMemory
import com.autodiag.core.obd.DtcProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DtcHistoryStoreTest {
    private fun dtc(code: String, memory: DtcMemory = DtcMemory.STORED, ecu: Int? = 0x7E0) =
        DiagnosticTroubleCode(
            code = code,
            memory = memory,
            protocol = DtcProtocol.OBD_MODE_03,
            ecuAddress = ecu
        )

    @Test
    fun new_code_gets_first_and_last_seen_equal_and_is_active() {
        val store = DtcHistoryStore()
        store.ingestScan(0x7E0, DtcMemory.STORED, listOf(dtc("P0301")), 1_000L)
        val record = store.all().single()
        assertEquals(1_000L, record.firstSeenAt)
        assertEquals(1_000L, record.lastSeenAt)
        assertEquals(DtcHistoryStatus.ACTIVE, record.status)
        assertNull(record.resolvedAt)
        assertEquals(1, record.timesObserved)
    }

    @Test
    fun repeated_scan_advances_last_seen_but_keeps_first_seen() {
        val store = DtcHistoryStore()
        store.ingestScan(0x7E0, DtcMemory.STORED, listOf(dtc("P0301")), 1_000L)
        store.ingestScan(0x7E0, DtcMemory.STORED, listOf(dtc("P0301")), 5_000L)
        val record = store.all().single()
        assertEquals(1_000L, record.firstSeenAt)
        assertEquals(5_000L, record.lastSeenAt)
        assertEquals(2, record.timesObserved)
    }

    @Test
    fun absent_on_complete_rescan_becomes_resolved_but_remains_in_history() {
        val store = DtcHistoryStore()
        store.ingestScan(0x7E0, DtcMemory.STORED, listOf(dtc("P0301")), 1_000L)
        store.ingestScan(0x7E0, DtcMemory.STORED, emptyList(), 9_000L)
        val record = store.all().single()
        assertEquals(DtcHistoryStatus.RESOLVED, record.status)
        assertEquals(1_000L, record.firstSeenAt)
        assertEquals(9_000L, record.resolvedAt)
        assertEquals(DtcResolutionReason.ABSENT_ON_RESCAN, record.resolutionReason)
        assertTrue(store.active().isEmpty())
        assertEquals(1, store.resolved().size)
    }

    @Test
    fun explicit_clear_is_distinguished_from_natural_disappearance() {
        val store = DtcHistoryStore()
        store.ingestScan(0x7E0, DtcMemory.STORED, listOf(dtc("P0420")), 1_000L)
        store.recordExplicitClear(0x7E0, DtcMemory.STORED, 2_000L)
        val record = store.all().single()
        assertEquals(DtcHistoryStatus.RESOLVED, record.status)
        assertEquals(2_000L, record.resolvedAt)
        assertEquals(DtcResolutionReason.CLEARED_BY_USER, record.resolutionReason)
    }

    @Test
    fun reappearance_after_resolution_is_reoccurrence_not_new_lineage() {
        val store = DtcHistoryStore()
        store.ingestScan(0x7E0, DtcMemory.STORED, listOf(dtc("P0301")), 1_000L)
        store.ingestScan(0x7E0, DtcMemory.STORED, emptyList(), 2_000L)
        store.ingestScan(0x7E0, DtcMemory.STORED, listOf(dtc("P0301")), 3_000L)
        val record = store.all().single()
        assertEquals(1_000L, record.firstSeenAt)
        assertEquals(3_000L, record.lastSeenAt)
        assertEquals(DtcHistoryStatus.ACTIVE, record.status)
        assertNull(record.resolvedAt)
        assertEquals(1, record.reoccurrenceCount)
        assertEquals(2, record.timesObserved)
    }

    @Test
    fun stored_and_permanent_same_code_are_independent() {
        val store = DtcHistoryStore()
        store.ingestScan(0x7E0, DtcMemory.STORED, listOf(dtc("P0301", DtcMemory.STORED)), 1_000L)
        store.ingestScan(0x7E0, DtcMemory.PERMANENT, listOf(dtc("P0301", DtcMemory.PERMANENT)), 1_000L)
        store.recordExplicitClear(0x7E0, DtcMemory.STORED, 2_000L)
        assertEquals(DtcHistoryStatus.RESOLVED, store.all().first { it.memory == DtcMemory.STORED }.status)
        assertEquals(DtcHistoryStatus.ACTIVE, store.all().first { it.memory == DtcMemory.PERMANENT }.status)
    }

    @Test
    fun same_code_on_different_ecus_is_independent() {
        val store = DtcHistoryStore()
        store.ingestScan(0x7E0, DtcMemory.STORED, listOf(dtc("P0301", ecu = 0x7E0)), 1_000L)
        store.ingestScan(0x7E2, DtcMemory.STORED, listOf(dtc("P0301", ecu = 0x7E2)), 1_000L)
        assertEquals(2, store.all().size)
        assertEquals(1, store.forEcu(0x7E0).size)
        assertEquals(1, store.forEcu(0x7E2).size)
    }

    @Test
    fun empty_scan_for_other_ecu_does_not_resolve_this_ecu() {
        val store = DtcHistoryStore()
        store.ingestScan(0x7E0, DtcMemory.STORED, listOf(dtc("P0301")), 1_000L)
        store.ingestScan(0x7E2, DtcMemory.STORED, emptyList(), 2_000L)
        assertEquals(DtcHistoryStatus.ACTIVE, store.forEcu(0x7E0).single().status)
    }

    @Test
    fun duplicate_code_in_one_scan_counts_once() {
        val store = DtcHistoryStore()
        store.ingestScan(0x7E0, DtcMemory.STORED, listOf(dtc("P0301"), dtc("P0301")), 1_000L)
        assertEquals(1, store.all().single().timesObserved)
    }

    @Test
    fun multiple_codes_are_tracked() {
        val store = DtcHistoryStore()
        store.ingestScan(
            0x7E0,
            DtcMemory.STORED,
            listOf(dtc("P0301"), dtc("P0420"), dtc("C0035")),
            1_000L
        )
        assertEquals(3, store.all().size)
        assertTrue(store.active().map { it.code }.containsAll(listOf("P0301", "P0420", "C0035")))
    }
}
