package com.autodiag.core.capability

import org.json.JSONArray
import org.json.JSONObject

/** Parses both legacy JSON arrays (`data/*.json`) and v1 wrapped files (`vehicles.json`). */
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
                provenance = provenance(o),
                id = o.optStringOrNull("id"),
                powertrain = o.optStringOrNull("powertrain"),
                years = years(o),
            )
        }
    }

    fun ecus(body: String): List<EcuDataDefinition> {
        val arr = arrayOf(body, "ecus") ?: return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            EcuDataDefinition(
                identity = EcuDataIdentity(
                    ecuId = o.optStringOrNull("ecuId") ?: o.optStringOrNull("id"),
                    manufacturer = o.optStringOrNull("manufacturer"),
                    hardwareNumber = o.optStringOrNull("hardwareNumber"),
                    softwareNumber = o.optStringOrNull("softwareNumber"),
                    softwareVersion = o.optStringOrNull("softwareVersion"),
                ),
                displayName = o.optString("displayName").ifBlank { o.optString("name") },
                verification = verification(o),
                provenance = provenance(o),
                vehicleIds = stringList(o, "vehicle_ids"),
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
                provenance = provenance(o),
                vehicleIds = stringList(o, "vehicle_ids"),
                protocol = req.protocol,
                elmPayload = req.elmPayload,
            )
        }
    }

    fun dtcs(body: String): List<DtcDataDefinition> {
        val arr = arrayOf(body, "dtcs") ?: arrayOf(body, "dtc") ?: return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            DtcDataDefinition(
                code = o.getString("code"),
                description = o.optStringOrNull("description"),
                system = o.optStringOrNull("system"),
                verification = verification(o),
                provenance = provenance(o),
            )
        }
    }

    fun datasetVersion(manifestBody: String): String =
        JSONObject(manifestBody).optString("datasetVersion", "unknown")

    fun recordCounts(manifestBody: String): Map<String, Int> {
        val r = JSONObject(manifestBody).optJSONObject("records") ?: return emptyMap()
        return mapOf(
            "vehicles" to r.optInt("vehicles", 0),
            "ecus" to r.optInt("ecus", 0),
            "signals" to r.optInt("signals", 0),
            "dtc" to maxOf(r.optInt("dtc", 0), r.optInt("dtcs", 0)),
        )
    }

    private data class ParsedRequest(val protocol: String?, val elmPayload: String?)

    private fun parseRequest(raw: Any?): ParsedRequest = when (raw) {
        null, JSONObject.NULL -> ParsedRequest(null, null)
        is String -> ParsedRequest(if (raw.startsWith("01")) "obd" else null, raw.replace(" ", ""))
        is JSONObject -> {
            val payload = raw.optStringOrNull("elm_payload")?.replace(" ", "")
            ParsedRequest(raw.optStringOrNull("protocol"), payload)
        }
        else -> ParsedRequest(null, null)
    }

    private fun arrayOf(body: String, key: String): JSONArray? {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) return null
        if (trimmed.startsWith("[")) return JSONArray(trimmed)
        val obj = JSONObject(trimmed)
        return obj.optJSONArray(key)
    }

    private fun years(o: JSONObject): List<Int> {
        val direct = o.optJSONArray("years")
        if (direct != null) return List(direct.length()) { direct.optInt(it) }.filter { it > 0 }
        val gens = o.optJSONArray("generations") ?: return emptyList()
        val out = mutableListOf<Int>()
        for (i in 0 until gens.length()) {
            val y = gens.getJSONObject(i).optJSONArray("years") ?: continue
            for (j in 0 until y.length()) out += y.optInt(j)
        }
        return out.filter { it > 0 }.distinct()
    }

    private fun stringList(o: JSONObject, key: String): List<String> {
        val a = o.optJSONArray(key) ?: return emptyList()
        return List(a.length()) { a.optString(it) }.filter { it.isNotBlank() }
    }

    private fun verification(o: JSONObject) =
        runCatching { VerificationState.valueOf(o.optString("verification", "UNVERIFIED")) }
            .getOrDefault(VerificationState.UNVERIFIED)

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
