package com.autodiag.core.obd

/** Classification of an ELM327 command exchange, before vehicle-level decoding. */
enum class Elm327ResponseKind {
    /** A prompt-terminated response containing usable non-error text. */
    POSITIVE,
    /** The adapter/ECU explicitly reported that no data was available. */
    NO_DATA,
    /** The command did not complete within the configured timeout. */
    TIMEOUT,
    /** The adapter reported an ELM-level command/communication error. */
    ERROR,
    /** A response could not be framed as a valid ELM exchange. */
    MALFORMED,
    /** A negative diagnostic response was returned and must not be treated as data. */
    NEGATIVE
}

data class Elm327Response(
    val kind: Elm327ResponseKind,
    val raw: String,
    val normalized: String = raw,
    val error: String? = null
)

object Elm327ResponseClassifier {
    private val errorTokens = setOf(
        "ERROR",
        "UNABLE TO CONNECT",
        "BUS INIT: ERROR",
        "CAN ERROR",
        "BUFFER FULL",
        "BUS BUSY",
        "STOPPED"
    )

    /** Classifies adapter-level text only; UDS NRC decoding remains a UDS concern. */
    fun classify(raw: String): Elm327Response {
        val normalized = raw
            .replace('\r', '\n')
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
            .trim()

        if (normalized.isEmpty()) {
            return Elm327Response(
                kind = Elm327ResponseKind.MALFORMED,
                raw = raw,
                normalized = normalized,
                error = "Empty ELM327 response"
            )
        }

        val lines = normalized.lines().map { it.uppercase() }
        if (lines.any { it == "NO DATA" || it.contains("NO DATA") }) {
            return Elm327Response(Elm327ResponseKind.NO_DATA, raw, normalized)
        }
        if (lines.any { it in errorTokens || errorTokens.any { token -> it.startsWith(token) } }) {
            return Elm327Response(Elm327ResponseKind.ERROR, raw, normalized)
        }
        if (lines.any { it.matches(Regex("^7F\\s+[0-9A-F]{2}\\s+[0-9A-F]{2}.*$")) }) {
            return Elm327Response(Elm327ResponseKind.NEGATIVE, raw, normalized)
        }

        val hasDiagnosticPayload = lines.any { it.matches(Regex("^[0-9A-F]+(?:\\s+[0-9A-F]+)*$")) }
        if (!hasDiagnosticPayload) {
            return Elm327Response(
                kind = Elm327ResponseKind.MALFORMED,
                raw = raw,
                normalized = normalized,
                error = "No hexadecimal diagnostic payload found"
            )
        }

        return Elm327Response(Elm327ResponseKind.POSITIVE, raw, normalized)
    }
}
