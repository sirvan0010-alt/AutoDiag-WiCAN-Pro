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
                vin = o.optString("vin").ifBlank { "" }, make = o.optStringOrNull("make"), model = o.optStringOrNull("model"),
                year = o.optIntOrNull("year"), verification = verification(o), provenance = provenance(o),
                powertrain = o.optStringOrNull("powertrain")
            )
        }
    }
    fun ecus(body: String): List<EcuDataDefinition> {
        val arr = arrayOf(body, "ecus") ?: return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            EcuDataDefinition(EcuDataIdentity(o.optStringOrNull("ecuId") ?: o.optStringOrNull("id"), o.optStringOrNull("manufacturer"), o.optStringOrNull("hardwareNumber"), o.optStringOrNull("softwareNumber"), o.optStringOrNull("softwareVersion")), o.optString("displayName").ifBlank { o.optString("name") }, verification(o), provenance(o))
        }
    }
    fun signals(body: String): List<SignalDataDefinition> {
        val arr = arrayOf(body, "signals") ?: return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i); val req = parseRequest(o.opt("request"))
            SignalDataDefinition(o.getString("id"), o.optString("label").ifBlank { o.optString("name") }, o.optStringOrNull("unit"), req.elmPayload, o.optDouble("scale", 1.0), o.optDouble("offset", 0.0), verification(o), provenance(o))
        }
    }
    fun dtcs(body: String): List<DtcDataDefinition> {
        val arr = arrayOf(body, "dtcs") ?: arrayOf(body, "dtc") ?: return emptyList()
        return List(arr.length()) { i -> val o = arr.getJSONObject(i); DtcDataDefinition(o.getString("code"), o.optStringOrNull("description"), o.optStringOrNull("system"), verification(o), provenance(o)) }
    }
    fun datasetVersion(manifestBody: String): String = JSONObject(manifestBody).optString("datasetVersion", "unknown")
    fun recordCounts(manifestBody: String): Map<String, Int> { val r = JSONObject(manifestBody).optJSONObject("records") ?: return emptyMap(); return mapOf("vehicles" to r.optInt("vehicles", 0), "ecus" to r.optInt("ecus", 0), "signals" to r.optInt("signals", 0), "dtc" to maxOf(r.optInt("dtc", 0), r.optInt("dtcs", 0))) }
    private data class ParsedRequest(val protocol: String?, val elmPayload: String?)
    private fun parseRequest(raw: Any?): ParsedRequest = when (raw) {
        null, JSONObject.NULL -> ParsedRequest(null, null)
        is String -> ParsedRequest(if (raw.startsWith("01")) "obd" else null, raw.replace(" ", ""))
        is JSONObject -> ParsedRequest(raw.optStringOrNull("protocol"), raw.optStringOrNull("elm_payload")?.replace(" ", ""))
        else -> ParsedRequest(null, null)
    }
    private fun arrayOf(body: String, key: String): JSONArray? { val trimmed = body.trim(); if (trimmed.isEmpty()) return null; if (trimmed.startsWith("[")) return JSONArray(trimmed); return JSONObject(trimmed).optJSONArray(key) }
    private fun stringList(o: JSONObject, key: String): List<String> { val a = o.optJSONArray(key) ?: return emptyList(); return List(a.length()) { a.optString(it) }.filter { it.isNotBlank() } }
    private fun verification(o: JSONObject) = runCatching { VerificationState.valueOf(o.optString("verification", "UNVERIFIED")) }.getOrDefault(VerificationState.UNVERIFIED)
    private fun provenance(o: JSONObject): String { if (o.has("provenance") && o.get("provenance") is JSONObject) return o.getJSONObject("provenance").optString("source_id", "diagnostic-data"); return o.optString("provenance", "diagnostic-data") }
    private fun JSONObject.optStringOrNull(k: String): String? = if (!has(k) || isNull(k)) null else optString(k).takeIf { it.isNotBlank() }
    private fun JSONObject.optIntOrNull(k: String): Int? = if (!has(k) || isNull(k)) null else optInt(k)
}
