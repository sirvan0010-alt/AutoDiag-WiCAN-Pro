package com.autodiag.core.contribution

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class HttpContributionTransport(
    private val endpointUrl: String,
    private val connectTimeoutMs: Int = 10_000,
    private val readTimeoutMs: Int = 15_000
) : ContributionTransport {
    override suspend fun upload(batch: List<ContributionRecord>): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            require(endpointUrl.startsWith("https://")) {
                "Contribution endpoint must be https:// — refusing to send diagnostic data over plaintext HTTP."
            }
            val payload = encodeBatch(batch)
            val connection = (URL(endpointUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }
            try {
                connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
                val code = connection.responseCode
                if (code !in 200..299) error("Contribution upload rejected by server: HTTP $code")
            } finally {
                connection.disconnect()
            }
        }
    }

    internal fun encodeBatch(batch: List<ContributionRecord>): String {
        val sb = StringBuilder()
        sb.append("{\"records\":[")
        batch.forEachIndexed { index, record ->
            if (index > 0) sb.append(',')
            encodeRecord(record, sb)
        }
        sb.append("]}")
        return sb.toString()
    }

    private fun encodeRecord(r: ContributionRecord, sb: StringBuilder) {
        sb.append('{')
        field(sb, "contributionId", r.contributionId); sb.append(',')
        rawField(sb, "schemaVersion", r.schemaVersion.toString()); sb.append(',')
        rawField(sb, "consentVersion", r.consentVersion.toString()); sb.append(',')
        sb.append("\"vehicleScope\":")
        if (r.vehicleScope == null) sb.append("null") else {
            sb.append('{'); field(sb, "wmiVdsModelYear", r.vehicleScope.wmiVdsModelYear); sb.append('}')
        }
        sb.append(','); nullableField(sb, "ecuSoftwareHint", r.ecuSoftwareHint)
        sb.append(','); nullableField(sb, "adapterFirmwareHint", r.adapterFirmwareHint)
        sb.append(','); field(sb, "monthBucket", r.monthBucket)
        sb.append(','); field(sb, "appVersion", r.appVersion)
        sb.append(','); sb.append("\"pidObservations\":[")
        r.pidObservations.forEachIndexed { i, p ->
            if (i > 0) sb.append(',')
            sb.append('{'); rawField(sb, "pid", p.pid.toString()); sb.append(',')
            nullableField(sb, "unit", p.unit); sb.append(',')
            rawField(sb, "sampleCount", p.sampleCount.toString()); sb.append(',')
            rawField(sb, "min", p.min.toString()); sb.append(',')
            rawField(sb, "max", p.max.toString()); sb.append(',')
            rawField(sb, "mean", p.mean.toString()); sb.append(',')
            rawField(sb, "hadDecodeFailure", p.hadDecodeFailure.toString()); sb.append('}')
        }
        sb.append("],\"dtcObservations\":[")
        r.dtcObservations.forEachIndexed { i, d ->
            if (i > 0) sb.append(',')
            sb.append('{'); field(sb, "code", d.code); sb.append(',')
            rawField(sb, "occurrenceCount", d.occurrenceCount.toString()); sb.append(',')
            nullableField(sb, "ecuAddressHint", d.ecuAddressHint); sb.append('}')
        }
        sb.append("]}")
    }

    private fun field(sb: StringBuilder, name: String, value: String) =
        sb.append('"').append(name).append("\":\"").append(escape(value)).append('"')

    private fun nullableField(sb: StringBuilder, name: String, value: String?) {
        if (value == null) sb.append('"').append(name).append("\":null") else field(sb, name, value)
    }

    private fun rawField(sb: StringBuilder, name: String, rawValue: String) =
        sb.append('"').append(name).append("\":").append(rawValue)

    private fun escape(s: String): String = buildString {
        for (c in s) when (c) {
            '"' -> append("\\\""); '\\' -> append("\\\\")
            '\n' -> append("\\n"); '\r' -> append("\\r"); '\t' -> append("\\t")
            else -> if (c.code < 0x20) append("\\u%04x".format(c.code)) else append(c)
        }
    }
}
