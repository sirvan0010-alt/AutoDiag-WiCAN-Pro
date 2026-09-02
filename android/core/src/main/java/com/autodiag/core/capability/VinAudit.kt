package com.autodiag.core.capability

/**
 * VIN consistency audit across ECU responses returned by OBD Mode 09 PID 02.
 *
 * This is deliberately evidence-based: a module is reported only when its
 * response contains a decodable VIN. No VIN is inferred from the vehicle
 * identity, registration data, or another ECU.
 */
data class EcuVinRecord(
    val ecuAddress: String?,
    val vin: String,
    val source: String = "OBD Mode 09 PID 02"
) {
    val displayEcu: String get() = ecuAddress?.let { "ECU $it" } ?: "ECU bez identifikátoru"
}

data class VinAudit(
    val referenceVin: String? = null,
    val ecuRecords: List<EcuVinRecord> = emptyList()
) {
    val mismatches: List<EcuVinRecord>
        get() = referenceVin?.let { ref -> ecuRecords.filter { it.vin != ref } }.orEmpty()

    val hasMismatch: Boolean get() = mismatches.isNotEmpty()
    val isConsistent: Boolean get() = ecuRecords.isNotEmpty() && !hasMismatch
}

object VinResponseParser {
    private val vinRegex = Regex("[A-HJ-NPR-Z0-9]{17}")
    private val headerRegex = Regex("^(?:0x)?([0-9A-F]{3}|[0-9A-F]{8})\\s*[:>-]?\\s*(.*)$", RegexOption.IGNORE_CASE)

    /**
     * Parses ELM327 text with optional CAN headers. It accepts ASCII VINs as
     * well as hex-encoded ISO-TP payload fragments. When headers are present,
     * records are kept separate so the UI can identify the ECU that disagrees.
     */
    fun parse(body: String): List<EcuVinRecord> {
        val groups = linkedMapOf<String?, StringBuilder>()
        var current: String? = null
        var sawHeader = false

        body.lineSequence().forEach { raw ->
            val line = raw.trim()
            if (line.isBlank()) return@forEach
            if (line.equals("SEARCHING...", true) || line.equals("NO DATA", true)) return@forEach

            val match = headerRegex.matchEntire(line)
            if (match != null && match.groupValues[2].contains(Regex("[0-9A-Fa-f]{4,}"))) {
                current = match.groupValues[1].uppercase()
                sawHeader = true
                groups.getOrPut(current) { StringBuilder() }.append(' ').append(match.groupValues[2])
            } else {
                groups.getOrPut(current) { StringBuilder() }.append(' ').append(line)
            }
        }

        if (!sawHeader) {
            val vin = extractVin(body)
            return vin?.let { listOf(EcuVinRecord(null, it)) }.orEmpty()
        }

        return groups.mapNotNull { (ecu, text) ->
            extractVin(text.toString())?.let { EcuVinRecord(ecu, it) }
        }
    }

    fun extractVin(body: String): String? {
        vinRegex.find(body.uppercase().replace("\\s+".toRegex(), ""))?.value?.let { return it }

        val hex = body.uppercase().replace("[^0-9A-F]".toRegex(), "")
        if (hex.length >= 34) {
            val bytes = buildList {
                var i = 0
                while (i + 1 < hex.length) {
                    add(hex.substring(i, i + 2).toIntOrNull(16) ?: return@buildList)
                    i += 2
                }
            }
            val ascii = bytes.map { it.toChar() }.joinToString("")
            vinRegex.find(ascii)?.value?.let { return it }
        }
        return null
    }
}
