package com.autodiag.core.obd

import com.autodiag.core.transport.WiCanTransport
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

/** Read-only ELM327 session. It does not issue vehicle-control commands. */
class Elm327Session(private val transport: WiCanTransport) {
    suspend fun initialize(): Result<Unit> = runCatching {
        command("ATZ", 3_000)
        command("ATE0")
        command("ATL0")
        command("ATH1")
        command("ATSP0")
    }

    suspend fun command(command: String, timeoutMs: Long = 3_000): String {
        require(command.isNotBlank())
        transport.send((command.trim() + "\r").toByteArray()).getOrThrow()
        return withTimeout(timeoutMs) {
            val bytes = transport.observeIncoming().first { chunk ->
                chunk.toString(Charsets.US_ASCII).contains(">")
            }
            bytes.toString(Charsets.US_ASCII)
                .replace(">", "")
                .trim()
        }
    }
}
