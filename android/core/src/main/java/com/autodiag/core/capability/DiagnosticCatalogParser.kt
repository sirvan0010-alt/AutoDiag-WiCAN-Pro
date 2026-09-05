package com.autodiag.core.capability

import org.json.JSONArray
import org.json.JSONObject

/** Parses legacy JSON arrays and v1 wrapped diagnostic data files. */
object DiagnosticCatalogParser {
    fun vehicles(body: String): List<VehicleDataDefinition> {
        val arr = arrayOf(body, "vehicles") ?: return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            VehicleDataDefinition(
                vin = o.optString("vin").ifBlank { "" },
                make = o.optStringOrNull("make"),
                model = o.optStringOrNull("model"),
                year = o.optIntOrNull("year"),
                verification = verification(o),
                provenance = provenance(o)
            )
        }
    }

    fun ecus(body: String): List<EcuDataDefinition> {
        val arr = arrayOf(body, "ecus") ?: return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            EcuDataDefinition(
                identity = EcuDataIdentity(
                    o.optStringOrNull("ecuId") ?: o.optStringOrNull("id"),
                    o.optStringOrNull("manufacturer"),
                    o.optStringOrNull("hardwareNumber"),
                    o.optStringOrNull("softwareNumber"),
                    o.optStringOrNull("softwareVersion")
                ),
                displayName = o.optString("displayName").ifBlank { o.optString("name") },
                verification = verification(o),
                provenance = provenance(o)
            )
        }
    }

    fun signals(body: String): List<SignalDataDefinition> {
        val arr = arrayOf(body, "signals") ?: return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            val req = parseRequest(o.opt("request"))
            SignalDataDefinition(
                id = o.getString("id"),
                label = o.optString("label").ifBlank { o.optString("name") },
                unit = o.optStringOrNull("unit"),
                request = req.elmPayload,
                scale = o.optDouble("scale", 1.0),
                offset = o.optDouble("offset", 0.0),
                verification = verification(o),
                provenance = provenance(o)
            )
        }
    }

    /** Parses candidate decoder variants without selecting an ambiguous variant. */
    fun decoderCandidates(body: String): List<SignalDecoderDefinition> {
        val root = JSONObject(body.trim())
        val variants = root.optJSONArray("variants") ?: return emptyList()
        val result = ArrayList<SignalDecoderDefinition>()
        for (i in 0 until variants.length()) {
            val variant = variants.getJSONObject(i)
            val variantId = variant.getString("id")
            val request = variant.getString("request").replace(" ", "")
            val candidates = variant.optJSONArray("candidates") ?: continue
            val requestCanId = parseCanId(variant.opt("requestCanId"))
            val responseCanId = parseCanId(variant.opt("responseCanId"))
            for (j in 0 until candidates.length()) {
                val candidate = candidates.getJSONObject(j)
                val d = candidate.optJSONObject("decoder") ?: continue
                val kind = runCatching {
                    DataDecoderSpec.Kind.valueOf(d.getString("kind").uppercase())
                }.getOrElse { throw IllegalArgumentException("Unsupported decoder kind: ${d.getString("kind")}") }
                val indices = if (d.has("responseIndices")) {
                    val a = d.getJSONArray("responseIndices")
                    List(a.length()) { a.getInt(it) }
                } else null
                val start = if (indices != null) indices.first() else if (d.has("responseIndex")) d.getInt("responseIndex") else d.getInt("responseIndexStart")
                val end = if (indices != null) indices.last() else if (d.has("responseIndexEnd")) d.getInt("responseIndexEnd") else start
                result += SignalDecoderDefinition(
                    signalId = candidate.getString("id"),
                    label = candidate.optString("label").ifBlank { candidate.getString("id") },
                    request = request,
                    variantId = variantId,
                    decoder = DataDecoderSpec(
                        kind = kind,
                        start = start,
                        end = end,
                        scale = d.optDouble("scale", 1.0),
                        offset = d.optDouble("offset", 0.0),
                        unit = candidate.optStringOrNull("unit"),
                        indices = indices
                    ),
                    verification = verification(variant),
                    provenance = root.optString("source", "diagnostic-data"),
                    requestCanId = requestCanId,
                    responseCanId = responseCanId
                )
            }
        }
        return result
    }

    fun dtcs(body: String): List<DtcDataDefinition> {
        val arr = arrayOf(body, "dtcs") ?: arrayOf(body, "dtc") ?: return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            DtcDataDefinition(
                o.getString("code"),
                o.optStringOrNull("description"),
                o.optStringOrNull("system"),
                verification(o),
                provenance(o)
            )
        }
    }

    fun datasetVersion(manifestBody: String): String = JSONObject(manifestBody).optString("datasetVersion", "unknown")

    fun recordCounts(manifestBody: String): Map<String, Int> {
        val r = JSONObject(manifestBody).optJSONObject("records") ?: return emptyMap()
        return mapOf(
            "vehicles" to r.optInt("vehicles", 0),
            "ecus" to r.optInt("ecus", 0),
            "signals" to r.optInt("signals", 0),
            "dtc" to maxOf(r.optInt("dtc", 0), r.optInt("dtcs", 0))
        )
    }

    private data class ParsedRequest(val protocol: String?, val elmPayload: String?)

    private fun parseRequest(raw: Any?): ParsedRequest = when (raw) {
        null, JSONObject.NULL -> ParsedRequest(null, null)
        is String -> ParsedRequest(if (raw.startsWith("01")) "obd" else null, raw.replace(" ", ""))
        is JSONObject -> ParsedRequest(raw.optStringOrNull("protocol"), raw.optStringOrNull("elm_payload")?.replace(" ", ""))
        else -> ParsedRequest(null, null)
    }

    private fun parseCanId(raw: Any?): Int? = when (raw) {
        null, JSONObject.NULL -> null
        is Number -> raw.toInt()
        is String -> raw.trim().removePrefix("0x").removePrefix("0X").takeIf { it.isNotBlank() }?.toIntOrNull(16)
        else -> null
    }

    private fun arrayOf(body: String, key: String): JSONArray? {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) return null
        if (trimmed.startsWith("[")) return JSONArray(trimmed)
        return JSONObject(trimmed).optJSONArray(key)
    }

    private fun verification(o: JSONObject) = runCatching {
        VerificationState.valueOf(o.optString("verification", "UNVERIFIED").uppercase())
    }.getOrDefault(VerificationState.UNVERIFIED)

    private fun provenance(o: JSONObject): String {
        if (o.has("provenance") && o.get("provenance") is JSONObject) {
            return o.getJSONObject("provenance").optString("source_id", "diagnostic-data")
        }
        return o.optString("provenance", "diagnostic-data")
    }

    private fun JSONObject.optStringOrNull(k: String): String? =
        if (!has(k) || isNull(k)) null else optString(k).takeIf { it.isNotBlank() }

    private fun JSONObject.optIntOrNull(k: String): Int? =
        if (!has(k) || isNull(k)) null else optInt(k)
}
