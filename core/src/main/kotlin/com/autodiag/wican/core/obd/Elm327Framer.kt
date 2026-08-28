package com.autodiag.wican.core.obd

/**
 * Minimal ELM327 line framer. It deliberately does not decode PID semantics.
 * ELM responses are terminated by '>' prompt; CR/LF are retained only as
 * transport framing and are removed from the returned response.
 */
object Elm327Framer {
    fun extractResponses(buffer: String): Pair<List<String>, String> {
        val responses = mutableListOf<String>()
        var remaining = buffer
        while (true) {
            val index = remaining.indexOf('>')
            if (index < 0) break
            responses += remaining.substring(0, index).trim('\r', '\n', ' ')
            remaining = remaining.substring(index + 1)
        }
        return responses to remaining
    }

    fun normalizeCommand(command: String): String =
        command.trim().replace("\\s+".toRegex(), " ").uppercase()
}
