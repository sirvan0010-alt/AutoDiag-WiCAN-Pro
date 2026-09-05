package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class OutlanderPhevDecoderResolverTest {
    private fun definition(signalId: String, variantId: String) = SignalDecoderDefinition(
        signalId = signalId,
        label = signalId,
        request = "21 01",
        variantId = variantId,
        decoder = DataDecoderSpec(DataDecoderSpec.Kind.UNSIGNED_U8, 0)
    )

    @Test
    fun returnsNotFoundWhenSignalIsAbsent() {
        val result = OutlanderPhevDecoderResolver.resolve(emptyList(), "battery.internal_resistance.max")
        assertIs<OutlanderPhevDecoderResolver.Resolution.NotFound>(result)
    }

    @Test
    fun rejectsMultipleVariantsWithoutGuessing() {
        val result = OutlanderPhevDecoderResolver.resolve(
            listOf(
                definition("battery.internal_resistance.max", "watchdog.le4a.21_01"),
                definition("battery.internal_resistance.max", "watchdog.ld4a.21_01")
            ),
            "battery.internal_resistance.max"
        )
        val ambiguous = assertIs<OutlanderPhevDecoderResolver.Resolution.Ambiguous>(result)
        assertEquals(
            listOf("watchdog.le4a.21_01", "watchdog.ld4a.21_01"),
            ambiguous.variantIds
        )
    }

    @Test
    fun resolvesExplicitVariant() {
        val expected = definition("battery.internal_resistance.max", "watchdog.ld4a.21_01")
        val result = OutlanderPhevDecoderResolver.resolve(listOf(expected), expected.signalId)
        val resolved = assertIs<OutlanderPhevDecoderResolver.Resolution.Resolved>(result)
        assertEquals(expected, resolved.definition)
    }

    @Test
    fun rejectsDuplicateDefinitionEvenWhenVariantMatches() {
        val definition = definition("battery.internal_resistance.min", "watchdog.le4a.21_01")
        val result = OutlanderPhevDecoderResolver.resolve(listOf(definition, definition), definition.signalId)
        assertIs<OutlanderPhevDecoderResolver.Resolution.Ambiguous>(result)
    }
}
