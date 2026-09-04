package com.autodiag.core.capability

import org.json.JSONArray
import org.json.JSONObject

/**
 * Replayable evidence container for Outlander PHEV experiments.
 *
 * It stores exactly what the adapter exchange produced. It does not store or
 * invent ECU/CAN mappings and it never upgrades verification by itself.
 */
data class OutlanderPhevEvidenceSample(
    val timestampEpochMs: Long,
    val request: String,
    val response: String?,
    val adapterStatus: String,
    val error: String? = null,
    val parsedByteCount: Int? = null,
    val isolationResistanceKOhm: Int? = null,
    val internalResistanceMaxMOhm: Double? = null,
    val internalResistanceMinMOhm: Double? = null,
    val verification: OutlanderMeasurementVerification = OutlanderMeasurementVerification.UNVERIFIED,
)

class OutlanderPhevEvidenceSession(
    val sessionId: String,
    val startedAtEpochMs: Long,
    val source: String = "wican-outlander-21-01",
    private val samples: MutableList<OutlanderPhevEvidenceSample> = mutableListOf(),
) {
    fun append(sample: OutlanderPhevEvidenceSample) { samples += sample }
    fun snapshot(): List<OutlanderPhevEvidenceSample> = samples.toList()

    /** Export is deliberately normalized: no vehicle-owner data and no guessed mapping. */
    fun toJson(): String {
        val root = JSONObject()
            .put("schemaVersion", 1)
            .put("sessionId", sessionId)
            .put("startedAtEpochMs", startedAtEpochMs)
            .put("source", source)
            .put("requestPolicy", "read_only")
            .put("mappingPolicy", "no_guessed_can_or_ecu_mapping")
        val array = JSONArray()
        samples.forEach { sample ->
            array.put(JSONObject()
                .put("timestampEpochMs", sample.timestampEpochMs)
                .put("request", sample.request)
                .put("response", sample.response)
                .put("adapterStatus", sample.adapterStatus)
                .put("error", sample.error)
                .put("parsedByteCount", sample.parsedByteCount)
                .put("isolationResistanceKOhm", sample.isolationResistanceKOhm)
                .put("internalResistanceMaxMOhm", sample.internalResistanceMaxMOhm)
                .put("internalResistanceMinMOhm", sample.internalResistanceMinMOhm)
                .put("verification", sample.verification.name))
        }
        return root.put("samples", array).toString(2)
    }

    companion object {
        fun fromJson(body: String): OutlanderPhevEvidenceSession {
            val root = JSONObject(body)
            val session = OutlanderPhevEvidenceSession(
                sessionId = root.getString("sessionId"),
                startedAtEpochMs = root.getLong("startedAtEpochMs"),
                source = root.optString("source", "wican-outlander-21-01")
            )
            val array = root.optJSONArray("samples") ?: return session
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                val verification = runCatching {
                    OutlanderMeasurementVerification.valueOf(o.optString("verification", "UNVERIFIED"))
                }.getOrDefault(OutlanderMeasurementVerification.UNVERIFIED)
                session.append(
                    OutlanderPhevEvidenceSample(
                        timestampEpochMs = o.getLong("timestampEpochMs"),
                        request = o.getString("request"),
                        response = o.optString("response").takeIf { it.isNotBlank() },
                        adapterStatus = o.optString("adapterStatus", "UNKNOWN"),
                        error = o.optString("error").takeIf { it.isNotBlank() },
                        parsedByteCount = o.optInt("parsedByteCount").takeIf { o.has("parsedByteCount") && !o.isNull("parsedByteCount") },
                        isolationResistanceKOhm = o.optInt("isolationResistanceKOhm").takeIf { o.has("isolationResistanceKOhm") && !o.isNull("isolationResistanceKOhm") },
                        internalResistanceMaxMOhm = o.optDouble("internalResistanceMaxMOhm").takeIf { o.has("internalResistanceMaxMOhm") && !o.isNull("internalResistanceMaxMOhm") },
                        internalResistanceMinMOhm = o.optDouble("internalResistanceMinMOhm").takeIf { o.has("internalResistanceMinMOhm") && !o.isNull("internalResistanceMinMOhm") },
                        verification = verification,
                    )
                )
            }
            return session
        }
    }
}
