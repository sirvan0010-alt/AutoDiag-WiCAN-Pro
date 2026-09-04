package com.autodiag.core.capability

/**
 * Converts the textual ELM327 response into the same integer-token shape used
 * by the analysed Watchdog decoder.
 *
 * A three-character CAN header (for example 7E8) is intentionally retained as
 * one integer token. This is important because the source decoder's indexes
 * are indexes into that normalized token array, not necessarily raw CAN-data
 * byte offsets.
 */
object OutlanderPhev21ResponseParser {
    fun parse(normalizedResponse: String): IntArray {
        val tokens = normalizedResponse
            .replace('\r', '\n')
            .lines()
            .flatMap { it.trim().split(Regex("\\s+")) }
            .filter { it.isNotBlank() }

        require(tokens.isNotEmpty()) { "Outlander 21 01 response is empty" }

        return tokens.map { token ->
            require(token.matches(Regex("[0-9A-Fa-f]+"))) {
                "Invalid hexadecimal token in Outlander response: $token"
            }
            token.toInt(16)
        }.toIntArray()
    }
}
