package com.autodiag.core.obd

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Elm327ResponseClassifierTest {
    @Test
    fun positiveHexResponseIsPositive() {
        val result = Elm327ResponseClassifier.classify("41 0C 1A F8")

        assertEquals(Elm327ResponseKind.POSITIVE, result.kind)
        assertEquals("41 0C 1A F8", result.raw)
        assertNull(result.error)
    }

    @Test
    fun noDataIsNotCommunicationError() {
        val result = Elm327ResponseClassifier.classify("NO DATA")

        assertEquals(Elm327ResponseKind.NO_DATA, result.kind)
        assertNull(result.error)
    }

    @Test
    fun elmErrorIsError() {
        val result = Elm327ResponseClassifier.classify("CAN ERROR")

        assertEquals(Elm327ResponseKind.ERROR, result.kind)
    }

    @Test
    fun udsNegativeResponseIsNegative() {
        val result = Elm327ResponseClassifier.classify("7F 22 11")

        assertEquals(Elm327ResponseKind.NEGATIVE, result.kind)
    }

    @Test
    fun nonHexTextIsMalformed() {
        val result = Elm327ResponseClassifier.classify("SEARCHING...")

        assertEquals(Elm327ResponseKind.MALFORMED, result.kind)
        assertEquals("No hexadecimal diagnostic payload found", result.error)
    }

    @Test
    fun emptyResponseIsMalformed() {
        val result = Elm327ResponseClassifier.classify("  \r\n")

        assertEquals(Elm327ResponseKind.MALFORMED, result.kind)
    }
}
