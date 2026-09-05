package com.autodiag.core.capability

import com.autodiag.core.obd.Elm327ResponseClassifier
import com.autodiag.core.obd.Elm327ResponseKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * P1 regression matrix for the 21 01 live-measurement contract.
 *
 * The first seven cases exercise the same per-signal runCatching contract used
 * by OutlanderPhev21LiveMeasurementRunner.decode(). The remaining cases cover
 * parser, resolver and adapter-status boundaries that must remain fail-closed.
 */
class OutlanderPhev21IndependentDecodeContractTest {
    private val isolation = SignalDecoderDefinition(
        signalId = "battery.isolation_resistance",
        label = "HV isolation resistance",
        request = "21 01",
        variantId = "watchdog.lz3a.21_01",
        decoder = DataDecoderSpec(DataDecoderSpec.Kind.UNSIGNED_U16_BE, 78, 79, unit = "kΩ")
    )

    private val max = SignalDecoderDefinition(
        signalId = "battery.internal_resistance.max",
        label = "Maximum internal resistance candidate",
        request = "21 01",
        variantId = "watchdog.le4a.21_01",
        decoder = DataDecoderSpec(DataDecoderSpec.Kind.UNSIGNED_U8, 38, scale = 0.1, unit = "MΩ")
    )

    private val min = max.copy(
        signalId = "battery.internal_resistance.min",
        decoder = max.decoder.copy(start = 39)
    )

    private fun decodeIndependently(
        response: IntArray,
        isolationDefinition: SignalDecoderDefinition? = isolation,
        maxDefinition: SignalDecoderDefinition? = max,
        minDefinition: SignalDecoderDefinition? = min
    ): Triple<Double?, Double?, Double?> = Triple(
        isolationDefinition?.let { runCatching { OutlanderPhevResistanceDecoder.decode(it, response) }.getOrNull() },
        maxDefinition?.let { runCatching { OutlanderPhevResistanceDecoder.decode(it, response) }.getOrNull() },
        minDefinition?.let { runCatching { OutlanderPhevResistanceDecoder.decode(it, response) }.getOrNull() }
    )

    private fun fullResponse(): IntArray = IntArray(80).also {
        it[38] = 15
        it[39] = 9
        it[78] = 0x01
        it[79] = 0xF4
    }

    @Test
    fun allThreeSignalsDecode() {
        val result = decodeIndependently(fullResponse())
        assertEquals(500.0, result.first)
        assertEquals(1.5, result.second)
        assertEquals(0.9, result.third)
    }

    @Test
    fun onlyIsolationIsAvailable() {
        val result = decodeIndependently(fullResponse(), maxDefinition = null, minDefinition = null)
        assertEquals(500.0, result.first)
        assertNull(result.second)
        assertNull(result.third)
    }

    @Test
    fun onlyMaximumIsAvailable() {
        val result = decodeIndependently(fullResponse(), isolationDefinition = null, minDefinition = null)
        assertNull(result.first)
        assertEquals(1.5, result.second)
        assertNull(result.third)
    }

    @Test
    fun onlyMinimumIsAvailable() {
        val result = decodeIndependently(fullResponse(), isolationDefinition = null, maxDefinition = null)
        assertNull(result.first)
        assertNull(result.second)
        assertEquals(0.9, result.third)
    }

    @Test
    fun isolationAndMaximumSurviveMissingMinimum() {
        val result = decodeIndependently(fullResponse(), minDefinition = null)
        assertEquals(500.0, result.first)
        assertEquals(1.5, result.second)
        assertNull(result.third)
    }

    @Test
    fun isolationAndMinimumSurviveMissingMaximum() {
        val result = decodeIndependently(fullResponse(), maxDefinition = null)
        assertEquals(500.0, result.first)
        assertNull(result.second)
        assertEquals(0.9, result.third)
    }

    @Test
    fun maximumAndMinimumSurviveShortIsolationPayload() {
        val response = IntArray(40).also {
            it[38] = 15
            it[39] = 9
        }
        val result = decodeIndependently(response)
        assertNull(result.first)
        assertEquals(1.5, result.second)
        assertEquals(0.9, result.third)
    }

    @Test
    fun shortParserInputFailsClosed() {
        val exception = runCatching { OutlanderPhev21ResponseParser.parse("00 21 01") }.exceptionOrNull()
        assertIs<IllegalArgumentException>(exception)
    }

    @Test
    fun malformedParserInputFailsClosed() {
        val exception = runCatching { OutlanderPhev21ResponseParser.parse("00 21 01 GG") }.exceptionOrNull()
        assertIs<IllegalArgumentException>(exception)
    }

    @Test
    fun noCandidateIsNotFoundByResolver() {
        val result = OutlanderPhevDecoderResolver.resolve(emptyList(), "battery.internal_resistance.max")
        assertIs<OutlanderPhevDecoderResolver.Resolution.NotFound>(result)
    }

    @Test
    fun ambiguousCandidateIsRejectedByResolver() {
        val second = max.copy(variantId = "watchdog.ld4a.21_01")
        val result = OutlanderPhevDecoderResolver.resolve(listOf(max, second), max.signalId)
        assertIs<OutlanderPhevDecoderResolver.Resolution.Ambiguous>(result)
    }

    @Test
    fun negativeElmResponseIsNotPositiveData() {
        val result = Elm327ResponseClassifier.classify("7F 21 78")
        assertEquals(Elm327ResponseKind.NEGATIVE, result.kind)
    }
}
