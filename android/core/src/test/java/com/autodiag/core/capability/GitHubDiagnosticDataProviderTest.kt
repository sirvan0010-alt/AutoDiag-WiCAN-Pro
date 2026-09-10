package com.autodiag.core.capability

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GitHubDiagnosticDataProviderTest {
    @Test
    fun emptyManifest_isUsableWithoutDataFiles() = runBlocking {
        val http = FakeHttp(mapOf("manifest.json" to """
            {"schemaVersion":1,"datasetVersion":"0.1.0","records":{"vehicles":0,"ecus":0,"signals":0,"dtc":0,"candidates":0}}
        """.trimIndent()))
        val provider = GitHubDiagnosticDataProvider("https://example.test", http)

        assertNull(provider.findVehicle("TMBTEST12345678901"))
        assertNull(provider.findDtc("P0401"))
        assertEquals(0, provider.findSignals(EcuDataIdentity(ecuId = "01")).size)
    }

    @Test
    fun normalizedRecords_areMappedAndVerificationIsPreserved() = runBlocking {
        val http = FakeHttp(mapOf(
            "manifest.json" to """{"records":{"vehicles":1,"ecus":1,"signals":1,"dtc":1,"candidates":0}}""",
            "data/vehicles.json" to """[{"vin":"TMBTEST12345678901","make":"Skoda","model":"Fabia","year":2020,"verification":"VERIFIED"}]""",
            "data/ecus.json" to """[{"ecuId":"ECM-1","displayName":"Engine ECU","manufacturer":"VW","verification":"PARTIALLY_VERIFIED"}]""",
            "data/signals.json" to """[{"id":"ECM-1:rpm","label":"Engine speed","unit":"rpm","request":"010C","scale":1.0,"offset":0.0,"verification":"VERIFIED"}]""",
            "data/dtc.json" to """[{"code":"P0401","description":"EGR flow insufficient","system":"ENGINE","verification":"VERIFIED"}]"""
        ))
        val provider = GitHubDiagnosticDataProvider("https://example.test", http)

        val vehicle = provider.findVehicle("tmbtest12345678901")!!
        val ecu = provider.findEcu(EcuDataIdentity(ecuId = "ECM-1"))!!
        val signals = provider.findSignals(EcuDataIdentity(ecuId = "ECM-1"))
        val dtc = provider.findDtc("p0401")!!

        assertEquals("Fabia", vehicle.model)
        assertEquals(VerificationState.VERIFIED, vehicle.verification)
        assertEquals("Engine ECU", ecu.displayName)
        assertEquals(VerificationState.PARTIALLY_VERIFIED, ecu.verification)
        assertEquals("010C", signals.single().request)
        assertEquals(VerificationState.VERIFIED, dtc.verification)
    }

    @Test
    fun candidateManifest_loads21_04Decoder() = runBlocking {
        val http = FakeHttp(mapOf(
            "manifest.json" to """{"records":{"candidates":1},"candidateFiles":["data/candidates/outlander_phev_watchdog_21_04.json"]}""",
            "data/candidates/outlander_phev_watchdog_21_04.json" to """
                {
                  "schemaVersion":2,
                  "variants":[
                    {
                      "id":"watchdog.lz3d.21_04",
                      "request":"21 04",
                      "verification":"unverified",
                      "candidates":[
                        {
                          "id":"watchdog.21_04.output_group_32",
                          "label":"Watchdog 21 04 voltage outputs (32 values)",
                          "unit":"V",
                          "decoder":{"kind":"unsigned_u8","responseIndexStart":0,"responseIndexEnd":31,"scale":0.02,"offset":0.0}
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()
        ))
        val provider = GitHubDiagnosticDataProvider("https://example.test", http)

        val candidates = provider.findDecoderCandidates("21 04", "watchdog.lz3d.21_04")

        assertEquals(1, candidates.size)
        assertEquals("watchdog.21_04.output_group_32", candidates.single().signalId)
        assertEquals("V", candidates.single().decoder.unit)
        assertEquals(0.02, candidates.single().decoder.scale)
    }

    @Test
    fun candidateManifest_missingFileList_failsLoudly() = runBlocking {
        val http = FakeHttp(mapOf("manifest.json" to """{"records":{"candidates":1}}"""))
        val provider = GitHubDiagnosticDataProvider("https://example.test", http)

        val error = assertFailsWith<IllegalStateException> {
            provider.findDecoderCandidates("21 04", null)
        }

        assertEquals(true, error.message!!.contains("candidateFiles"))
    }

    @Test
    fun candidateManifest_countMismatch_failsLoudly() = runBlocking {
        val http = FakeHttp(mapOf(
            "manifest.json" to """{"records":{"candidates":2},"candidateFiles":["data/candidates/one.json"]}""",
            "data/candidates/one.json" to "{}"
        ))
        val provider = GitHubDiagnosticDataProvider("https://example.test", http)

        val error = assertFailsWith<IllegalStateException> {
            provider.findDecoderCandidates("21 04", null)
        }

        assertEquals(true, error.message!!.contains("candidates=2"))
        assertEquals(true, error.message!!.contains("1 entries"))
    }

    @Test
    fun malformedCandidateFile_namesTheExactFile() = runBlocking {
        val badFile = "data/candidates/bad.json"
        val http = FakeHttp(mapOf(
            "manifest.json" to """{"records":{"candidates":1},"candidateFiles":["$badFile"]}""",
            badFile to "{not-json"
        ))
        val provider = GitHubDiagnosticDataProvider("https://example.test", http)

        val error = assertFailsWith<IllegalStateException> {
            provider.findDecoderCandidates("21 04", null)
        }

        assertEquals(true, error.message!!.contains(badFile))
    }

    private class FakeHttp(private val responses: Map<String, String>) : DiagnosticDataHttpClient {
        override fun get(url: String): String = responses[url.substringAfter("example.test/")]
            ?: error("Unexpected URL: $url")
    }
}
