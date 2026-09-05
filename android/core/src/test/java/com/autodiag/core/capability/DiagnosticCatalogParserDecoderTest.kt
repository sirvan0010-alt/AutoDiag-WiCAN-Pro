package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals

class DiagnosticCatalogParserDecoderTest {
    @Test
    fun parsesSingleByteAndBigEndianCandidatesWithoutLosingVariant() {
        val body = """
            {
              "schemaVersion": 2,
              "source": "PHEV Watchdog APK direct forensic analysis",
              "verification": "unverified",
              "variants": [
                {
                  "id": "watchdog.le4a.21_01",
                  "request": "21 01",
                  "verification": "partially_verified",
                  "candidates": [
                    {
                      "id": "battery.internal_resistance.max",
                      "label": "Maximum battery internal resistance",
                      "unit": "MΩ",
                      "decoder": {
                        "kind": "unsigned_u8",
                        "responseIndex": 38,
                        "scale": 0.1,
                        "offset": 0.0
                      }
                    }
                  ]
                },
                {
                  "id": "watchdog.ld4a.21_01",
                  "request": "21 01",
                  "verification": "partially_verified",
                  "candidates": [
                    {
                      "id": "battery.internal_resistance.max",
                      "label": "Maximum battery internal resistance",
                      "unit": "MΩ",
                      "decoder": {
                        "kind": "unsigned_u16_be",
                        "responseIndexStart": 12,
                        "responseIndexEnd": 13,
                        "scale": 0.001,
                        "offset": 0.0
                      }
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val candidates = DiagnosticCatalogParser.decoderCandidates(body)

        assertEquals(2, candidates.size)
        assertEquals("watchdog.le4a.21_01", candidates[0].variantId)
        assertEquals(38, candidates[0].decoder.start)
        assertEquals(DataDecoderSpec.Kind.UNSIGNED_U8, candidates[0].decoder.kind)
        assertEquals(0.1, candidates[0].decoder.scale)
        assertEquals("watchdog.ld4a.21_01", candidates[1].variantId)
        assertEquals(12, candidates[1].decoder.start)
        assertEquals(13, candidates[1].decoder.end)
        assertEquals(DataDecoderSpec.Kind.UNSIGNED_U16_BE, candidates[1].decoder.kind)
    }
}
