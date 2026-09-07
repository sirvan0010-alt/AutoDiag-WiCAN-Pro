package com.autodiag.core.diagnostic

import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

/** Stable JSON artifact codec for diagnostic evidence snapshots. */
object DiagnosticEvidenceJson {
    const val FORMAT = "autodiag-diagnostic-evidence-v1"

    fun write(evidence: List<DiagnosticEvidence<*>>, output: OutputStream) {
        val root = JSONObject()
            .put("format", FORMAT)
            .put("evidence", JSONArray().also { array ->
                evidence.forEach { array.put(toJson(it)) }
            })
        output.write(root.toString().toByteArray(StandardCharsets.UTF_8))
    }

    fun write(store: DiagnosticEvidenceStore, output: OutputStream) =
        write(store.snapshot(), output)

    fun read(input: InputStream): List<DiagnosticEvidence<*>> =
        read(input.readBytes().toString(StandardCharsets.UTF_8))

    fun read(json: String): List<DiagnosticEvidence<*>> {
        val root = JSONObject(json)
        require(root.optString("format") == FORMAT) { "Unsupported evidence artifact format" }
        val array = root.optJSONArray("evidence") ?: JSONArray()
        return buildList(array.length()) {
            for (index in 0 until array.length()) {
                add(fromJson(array.getJSONObject(index)))
            }
        }
    }

    private fun toJson(item: DiagnosticEvidence<*>): JSONObject = JSONObject()
        .put("key", item.key)
        .put("value", item.value)
        .put("unit", item.unit)
        .put("timestampEpochMs", item.timestampEpochMs)
        .put("availability", item.availability.name)
        .put("verification", item.verification.name)
        .put("isDerived", item.isDerived)
        .put("quality", item.quality)
        .put("note", item.note)
        .put("provenance", JSONObject()
            .put("source", item.provenance.source.name)
            .put("sourceId", item.provenance.sourceId)
            .put("ecuId", item.provenance.ecuId)
            .put("vehicleProfile", item.provenance.vehicleProfile)
            .put("rawRepresentation", item.provenance.rawRepresentation)
        )

    private fun fromJson(item: JSONObject): DiagnosticEvidence<*> {
        val provenance = item.optJSONObject("provenance") ?: JSONObject()
        return DiagnosticEvidence(
            key = item.getString("key"),
            value = item.opt("value").takeUnless { it === JSONObject.NULL },
            unit = item.optString("unit").takeUnless { it.isEmpty() },
            timestampEpochMs = item.getLong("timestampEpochMs"),
            availability = EvidenceAvailability.valueOf(item.getString("availability")),
            verification = EvidenceVerification.valueOf(item.getString("verification")),
            provenance = EvidenceProvenance(
                source = EvidenceSource.valueOf(provenance.getString("source")),
                sourceId = provenance.optString("sourceId").takeUnless { it.isEmpty() },
                ecuId = provenance.optString("ecuId").takeUnless { it.isEmpty() },
                vehicleProfile = provenance.optString("vehicleProfile").takeUnless { it.isEmpty() },
                rawRepresentation = provenance.optString("rawRepresentation").takeUnless { it.isEmpty() }
            ),
            isDerived = item.optBoolean("isDerived", false),
            quality = item.optString("quality").takeUnless { it.isEmpty() },
            note = item.optString("note").takeUnless { it.isEmpty() }
        )
    }
}
