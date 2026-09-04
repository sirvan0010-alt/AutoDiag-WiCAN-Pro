package com.autodiag.core.capability

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Read-only provider for the normalized AutoDiag-WiCAN diagnostic-data repository.
 *
 * The provider deliberately consumes normalized JSON indexes rather than raw
 * proprietary ROD/VCDS/HaynesPro files. Authentication is injected by the caller;
 * no GitHub token is embedded in the application.
 */
class GitHubDiagnosticDataProvider(
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val http: DiagnosticDataHttpClient = UrlConnectionDiagnosticDataHttpClient()
) : DiagnosticDataProvider {

    @Volatile private var loaded = false
    private var vehicles: List<VehicleDataDefinition> = emptyList()
    private var ecus: List<EcuDataDefinition> = emptyList()
    private var signals: List<SignalDataDefinition> = emptyList()
    private var dtcs: List<DtcDataDefinition> = emptyList()

    override suspend fun findVehicle(vin: String): VehicleDataDefinition? {
        loadIfNeeded()
        return vehicles.firstOrNull { it.vin.equals(vin, ignoreCase = true) }
    }

    override suspend fun findEcu(identity: EcuDataIdentity): EcuDataDefinition? {
        loadIfNeeded()
        return ecus.firstOrNull { sameEcu(it.identity, identity) }
    }

    override suspend fun findSignals(identity: EcuDataIdentity): List<SignalDataDefinition> {
        loadIfNeeded()
        val ecu = ecus.firstOrNull { sameEcu(it.identity, identity) } ?: return emptyList()
        val ecuKey = ecu.identity.ecuId ?: return emptyList()
        return signals.filter { it.id.startsWith("$ecuKey:") }
    }

    override suspend fun findDtc(code: String): DtcDataDefinition? {
        loadIfNeeded()
        return dtcs.firstOrNull { it.code.equals(code, ignoreCase = true) }
    }

    @Synchronized
    private fun loadIfNeeded() {
        if (loaded) return
        vehicles = parseVehicles(http.get(url("data/vehicles.json")))
        ecus = parseEcus(http.get(url("data/ecus.json")))
        signals = parseSignals(http.get(url("data/signals.json")))
        dtcs = parseDtcs(http.get(url("data/dtc.json")))
        loaded = true
    }

    private fun url(path: String): String = baseUrl.trimEnd('/') + "/" + path

    private fun sameEcu(a: EcuDataIdentity, b: EcuDataIdentity): Boolean {
        fun eq(x: String?, y: String?): Boolean = x == null || y == null || x.equals(y, true)
        return eq(a.ecuId, b.ecuId) &&
            eq(a.manufacturer, b.manufacturer) &&
            eq(a.hardwareNumber, b.hardwareNumber) &&
            eq(a.softwareNumber, b.softwareNumber) &&
            eq(a.softwareVersion, b.softwareVersion)
    }

    private fun parseVehicles(body: String): List<VehicleDataDefinition> =
        JSONArray(body).let { array -> List(array.length()) { i ->
            val o = array.getJSONObject(i)
            VehicleDataDefinition(
                vin = o.getString("vin"), make = o.optStringOrNull("make"),
                model = o.optStringOrNull("model"), year = o.optIntOrNull("year"),
                verification = verification(o), provenance = o.optString("provenance", "diagnostic-data")
            )
        } }

    private fun parseEcus(body: String): List<EcuDataDefinition> =
        JSONArray(body).let { array -> List(array.length()) { i ->
            val o = array.getJSONObject(i)
            EcuDataDefinition(
                identity = EcuDataIdentity(
                    ecuId = o.optStringOrNull("ecuId"), manufacturer = o.optStringOrNull("manufacturer"),
                    hardwareNumber = o.optStringOrNull("hardwareNumber"), softwareNumber = o.optStringOrNull("softwareNumber"),
                    softwareVersion = o.optStringOrNull("softwareVersion")
                ),
                displayName = o.getString("displayName"), verification = verification(o),
                provenance = o.optString("provenance", "diagnostic-data")
            )
        } }

    private fun parseSignals(body: String): List<SignalDataDefinition> =
        JSONArray(body).let { array -> List(array.length()) { i ->
            val o = array.getJSONObject(i)
            SignalDataDefinition(
                id = o.getString("id"), label = o.getString("label"), unit = o.optStringOrNull("unit"),
                request = o.optStringOrNull("request"), scale = o.optDouble("scale", 1.0), offset = o.optDouble("offset", 0.0),
                verification = verification(o), provenance = o.optString("provenance", "diagnostic-data")
            )
        } }

    private fun parseDtcs(body: String): List<DtcDataDefinition> =
        JSONArray(body).let { array -> List(array.length()) { i ->
            val o = array.getJSONObject(i)
            DtcDataDefinition(
                code = o.getString("code"), description = o.optStringOrNull("description"),
                system = o.optStringOrNull("system"), verification = verification(o),
                provenance = o.optString("provenance", "diagnostic-data")
            )
        } }

    private fun verification(o: JSONObject): VerificationState =
        runCatching { VerificationState.valueOf(o.optString("verification", "UNVERIFIED")) }
            .getOrDefault(VerificationState.UNVERIFIED)

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (!has(key) || isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    private fun JSONObject.optIntOrNull(key: String): Int? =
        if (!has(key) || isNull(key)) null else optInt(key)

    companion object {
        const val DEFAULT_BASE_URL =
            "https://raw.githubusercontent.com/sirvan0010-alt/AutoDiag-WiCAN-Diagnostic-Data/main"
    }
}

interface DiagnosticDataHttpClient {
    fun get(url: String): String
}

class UrlConnectionDiagnosticDataHttpClient(
    private val connectTimeoutMs: Int = 5000,
    private val readTimeoutMs: Int = 10000
) : DiagnosticDataHttpClient {
    override fun get(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = connectTimeoutMs
            connection.readTimeout = readTimeoutMs
            connection.setRequestProperty("Accept", "application/json")
            val status = connection.responseCode
            if (status !in 200..299) throw IllegalStateException("Diagnostic data HTTP $status")
            return BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
