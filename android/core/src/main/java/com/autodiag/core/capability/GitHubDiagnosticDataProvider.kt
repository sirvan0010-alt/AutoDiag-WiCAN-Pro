package com.autodiag.core.capability

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/** Read-only provider for normalized AutoDiag-WiCAN diagnostic-data JSON. */
class GitHubDiagnosticDataProvider(
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val http: DiagnosticDataHttpClient = UrlConnectionDiagnosticDataHttpClient()
) : DiagnosticDataProvider {
    @Volatile private var loaded = false
    private var vehicles = emptyList<VehicleDataDefinition>()
    private var ecus = emptyList<EcuDataDefinition>()
    private var signals = emptyList<SignalDataDefinition>()
    private var dtcs = emptyList<DtcDataDefinition>()

    override suspend fun findVehicle(vin: String): VehicleDataDefinition? {
        loadIfNeeded(); return vehicles.firstOrNull { it.vin.equals(vin, true) }
    }
    override suspend fun findEcu(identity: EcuDataIdentity): EcuDataDefinition? {
        loadIfNeeded(); return ecus.firstOrNull { sameEcu(it.identity, identity) }
    }
    override suspend fun findSignals(identity: EcuDataIdentity): List<SignalDataDefinition> {
        loadIfNeeded()
        val ecuKey = ecus.firstOrNull { sameEcu(it.identity, identity) }?.identity?.ecuId ?: return emptyList()
        return signals.filter { it.id.startsWith("$ecuKey:") }
    }
    override suspend fun findDtc(code: String): DtcDataDefinition? {
        loadIfNeeded(); return dtcs.firstOrNull { it.code.equals(code, true) }
    }

    @Synchronized private fun loadIfNeeded() {
        if (loaded) return
        val manifest = JSONObject(http.get(url("manifest.json")))
        val r = manifest.optJSONObject("records")
        if ((r?.optInt("vehicles", 0) ?: 0) > 0) vehicles = parseVehicles(http.get(url("data/vehicles.json")))
        if ((r?.optInt("ecus", 0) ?: 0) > 0) ecus = parseEcus(http.get(url("data/ecus.json")))
        if ((r?.optInt("signals", 0) ?: 0) > 0) signals = parseSignals(http.get(url("data/signals.json")))
        if ((r?.optInt("dtc", 0) ?: 0) > 0) dtcs = parseDtcs(http.get(url("data/dtc.json")))
        loaded = true
    }
    private fun url(path: String) = baseUrl.trimEnd('/') + "/" + path

    private fun sameEcu(a: EcuDataIdentity, b: EcuDataIdentity): Boolean {
        fun eq(x: String?, y: String?) = x == null || y == null || x.equals(y, true)
        return eq(a.ecuId,b.ecuId) && eq(a.manufacturer,b.manufacturer) && eq(a.hardwareNumber,b.hardwareNumber) && eq(a.softwareNumber,b.softwareNumber) && eq(a.softwareVersion,b.softwareVersion)
    }
    private fun parseVehicles(body: String) = JSONArray(body).let { a -> List(a.length()) { i -> a.getJSONObject(i).let { o -> VehicleDataDefinition(o.getString("vin"),o.optStringOrNull("make"),o.optStringOrNull("model"),o.optIntOrNull("year"),verification(o),o.optString("provenance","diagnostic-data")) } } }
    private fun parseEcus(body: String) = JSONArray(body).let { a -> List(a.length()) { i -> a.getJSONObject(i).let { o -> EcuDataDefinition(EcuDataIdentity(o.optStringOrNull("ecuId"),o.optStringOrNull("manufacturer"),o.optStringOrNull("hardwareNumber"),o.optStringOrNull("softwareNumber"),o.optStringOrNull("softwareVersion")),o.getString("displayName"),verification(o),o.optString("provenance","diagnostic-data")) } } }
    private fun parseSignals(body: String) = JSONArray(body).let { a -> List(a.length()) { i -> a.getJSONObject(i).let { o -> SignalDataDefinition(o.getString("id"),o.getString("label"),o.optStringOrNull("unit"),o.optStringOrNull("request"),o.optDouble("scale",1.0),o.optDouble("offset",0.0),verification(o),o.optString("provenance","diagnostic-data")) } } }
    private fun parseDtcs(body: String) = JSONArray(body).let { a -> List(a.length()) { i -> a.getJSONObject(i).let { o -> DtcDataDefinition(o.getString("code"),o.optStringOrNull("description"),o.optStringOrNull("system"),verification(o),o.optString("provenance","diagnostic-data")) } } }
    private fun verification(o: JSONObject) = runCatching { VerificationState.valueOf(o.optString("verification","UNVERIFIED")) }.getOrDefault(VerificationState.UNVERIFIED)
    private fun JSONObject.optStringOrNull(k: String): String? = if (!has(k) || isNull(k)) null else optString(k).takeIf { it.isNotBlank() }
    private fun JSONObject.optIntOrNull(k: String): Int? = if (!has(k) || isNull(k)) null else optInt(k)

    companion object { const val DEFAULT_BASE_URL = "https://raw.githubusercontent.com/sirvan0010-alt/AutoDiag-WiCAN-Diagnostic-Data/main" }
}

interface DiagnosticDataHttpClient { fun get(url: String): String }

class UrlConnectionDiagnosticDataHttpClient(private val connectTimeoutMs: Int = 5000, private val readTimeoutMs: Int = 10000) : DiagnosticDataHttpClient {
    override fun get(url: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.requestMethod = "GET"; c.connectTimeout = connectTimeoutMs; c.readTimeout = readTimeoutMs
            c.setRequestProperty("Accept", "application/json")
            if (c.responseCode !in 200..299) throw IllegalStateException("Diagnostic data HTTP ${c.responseCode}")
            return BufferedReader(InputStreamReader(c.inputStream, Charsets.UTF_8)).use { it.readText() }
        } finally { c.disconnect() }
    }
}
