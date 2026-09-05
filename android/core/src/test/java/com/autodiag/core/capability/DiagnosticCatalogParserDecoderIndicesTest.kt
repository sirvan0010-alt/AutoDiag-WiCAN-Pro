package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals

class DiagnosticCatalogParserDecoderIndicesTest {
    @Test
    fun parsesAndDecodesNonContiguousResponseIndices() {
        val body = """
            {
              "source": "apk-forensic",
              "variants": [{
                "id": "watchdog.lz3e.21_03",
                "request": "21 03",
                "verification": "partially_verified",
                "candidates": [{
                  "id": "powertrain.generator_rpm",
                  "label": "GENERATOR_RPM",
                  "unit": "rpm",
                  "decoder": {
                    "kind": "unsigned_u16_be",
                    "responseIndices": [29, 26],
                    "scale": 1.0
                  }
                }]
              }]
            }
        """.trimIndent()

        val candidates = DiagnosticCatalogParser.decoderCandidates(body)
        assertEquals(1, candidates.size)
        assertEquals(listOf(29, 26), candidates.single().decoder.indices)

        val tokens = IntArray(30)
        tokens[29] = 0x12
        tokens[26] = 0x34
        assertEquals(0x1234.toDouble(), DataDrivenDecoder.decode(tokens, candidates.single().decoder))
    }
}
