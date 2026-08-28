package com.autodiag.core.obd

import com.autodiag.core.transport.WiCanTransport
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import java.nio.charset.Charset

/**
 * Read-only ELM327 session over [WiCanTransport].
 *
 * Incoming TCP data may be split across arbitrary chunks, so a response is
 * accumulated until the ELM prompt '>' is observed. No vehicle-control
 * commands are issued here.
 */
class Elm327Session(private val transport: WiCanTransport) {
    @Volatile
    private var initialized: Boolean = false

    val isInitialized: Boolean get() = initialized

    suspend fun initialize(): Result<Unit> = runCatching {
        command("ATZ", timeoutMs = 5_000)
        command("ATE0")
        command("ATL0")
        command("ATH1")
        command("ATSP0")
        initialized = true
    }.onFailure {
        initialized = false
    }

    suspend fun command(command: String, timeoutMs: Long = 3_000): String {
        require(command.isNotBlank())
        return withTimeout(timeoutMs) {
            coroutineScope {
                // Subscribe before TX so a fast ELM response cannot be lost.
                val response = async {
                    val ascii = Charset.forName("US-ASCII")
                    val acc = StringBuilder()
                    transport.observeIncoming().first { chunk ->
                        acc.append(chunk.toString(ascii))
                        acc.indexOf('>') >= 0
                    }
                    val text = acc.toString()
                    val idx = text.indexOf('>')
                    text.substring(0, idx)
                        .replace("\r", "\n")
                        .lines()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .joinToString("\n")
                        .trim()
                }

                transport.send((command.trim() + "\r").toByteArray(Charsets.US_ASCII)).getOrThrow()
                response.await()
            }
        }
    }

    suspend fun close() {
        initialized = false
        runCatching { transport.disconnect() }
    }
}
