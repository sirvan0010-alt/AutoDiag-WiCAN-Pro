package com.autodiag.core.obd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Mode06InterpretationTest {
    @Test
    fun decodes_unsigned_voltage_and_limits() {
        val raw = ObdMode06TestResult(0x01, 0x08, 0x0A, 0x1D70, 0x1318, 0x2290, Mode06ResultStatus.UNKNOWN)
        val result = Mode06Interpreter.interpret(raw)

        assertEquals("Max. napětí O2 B1S1", result.labelCs)
        assertEquals("O2 B1S1", result.monitor!!.labelCs)
        assertEquals("V", result.value!!.unit)
        assertEquals(0x1D70 * 0.000122, result.value.value, 0.000001)
        assertEquals(Mode06ResultStatus.WITHIN_LIMITS, result.status)
        assertTrue(result.bandPosition!! > 0.0 && result.bandPosition < 1.0)
    }

    @Test
    fun signed_uasid_is_two_complement_before_scaling() {
        val raw = ObdMode06TestResult(0x31, 0x01, 0x8C, 0xFFFF, 0xFF00, 0x0100, Mode06ResultStatus.UNKNOWN)
        val result = Mode06Interpreter.interpret(raw)

        assertEquals(-0.01, result.value!!.value, 0.000001)
        assertEquals(-2.56, result.minimum!!.value, 0.000001)
        assertEquals(2.56, result.maximum!!.value, 0.000001)
        assertEquals(Mode06ResultStatus.WITHIN_LIMITS, result.status)
    }

    @Test
    fun unknown_uasid_never_guesses_physical_value_or_status() {
        val raw = ObdMode06TestResult(0x31, 0x01, 0xFE, 100, 0, 200, Mode06ResultStatus.UNKNOWN)
        val result = Mode06Interpreter.interpret(raw)

        assertNull(result.scaling)
        assertNull(result.value)
        assertNull(result.minimum)
        assertNull(result.maximum)
        assertEquals(Mode06ResultStatus.UNKNOWN, result.status)
        assertEquals("EGR B1", result.labelCs)
    }

    @Test
    fun unknown_mid_stays_unknown() {
        val raw = ObdMode06TestResult(0xE0, 0x01, 0x0A, 100, 0, 200, Mode06ResultStatus.UNKNOWN)
        val result = Mode06Interpreter.interpret(raw)

        assertNull(result.monitor)
        assertNull(result.test)
        assertEquals("MID 0xE0 TID 0x01", result.labelCs)
    }

    @Test
    fun exact_mid_tid_uasid_definition_has_priority() {
        val definition = Mode06TestRegistry.get(0x01, 0x08, 0x0A)
        assertEquals("Max. napětí O2 B1S1", definition!!.labelCs)
        assertEquals(0x0A, definition.unitAndScalingId!!)
    }
}
