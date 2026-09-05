package com.autodiag.core.capability

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DiagnosticCatalogParserTest {
    @Test
    fun parsesHexCanIdsWithoutInventingMissingAddresses() {
        val definitions = DiagnosticCatalogParser.decoderCandidates(
            """
            {
              "source": "test",
              "variants": [
                {
                  "id": "watchdog.lz3b.21_02",
                  "request": "21 02",
                  "requestCanId": "0x761",
                  "responseCanId": "0x762",
                  "verification": "PARTIALLY_VERIFIED",
                  "candidates": [
                    {
                      "id": "battery.cell_voltage.group1",
                      "label": "cells",
                      "unit": "V",
                      "decoder": {
                        "kind": "unsigned_u8",
                        "responseIndexStart": 0,
                        "responseIndexEnd": 31,
                        "scale": 0.02
                      }
                    }
                  ]
                },
                {
                  "id": "watchdog.lz3e.21_03",
                  "request": "21 03",
                  "verification": "PARTIALLY_VERIFIED",
                  "candidates": [
                    {
                      "id": "powertrain.generator_rpm",
                      "label": "generator",
                      "unit": "rpm",
                      "decoder": {
                        "kind": "unsigned_u16_be",
                        "responseIndices": [29, 26]
                      }
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
        )

        val addressed = definitions.first { it.signalId == "battery.cell_voltage.group1" }
        assertEquals(0x761, addressed.requestCanId)
        assertEquals(0x762, addressed.responseCanId)

        val unresolved = definitions.first { it.signalId == "powertrain.generator_rpm" }
        assertNull(unresolved.requestCanId)
        assertNull(unresolved.responseCanId)
        assertEquals(listOf(29, 26), unresolved.decoder.indices)
    }
}
