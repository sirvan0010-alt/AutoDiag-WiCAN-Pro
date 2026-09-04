package com.autodiag.core.obd

import com.autodiag.core.transport.WiCanTransport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    private val commandMutex = Mutex()

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

    /**
     * Executes a command and returns a typed adapter-level outcome while
     * preserving the raw response. This is the preferred API for diagnostic
     * pipelines that must distinguish NO DATA from transport failure.
     */
    suspend fun commandDetailed(command: String, timeoutMs: Long = 3_000): Elm327Response {
        require(command.isNotBlank())
        return try {
            val raw = commandRaw(command, timeoutMs)
            Elm327ResponseClassifier.classify(raw)
        } catch (e: TimeoutCancellationException) {
            Elm327Response(
                kind = Elm327ResponseKind.TIMEOUT,
                raw = "",
                error = e.message ?: "ELM327 command timed out"
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: IllegalArgumentException) {
            Elm327Response(
                kind = Elm327ResponseKind.MALFORMED,
                raw = "",
                error = e.message ?: "Malformed ELM327 response"
            )
        } catch (e: Throwable) {
            Elm327Response(
                kind = Elm327ResponseKind.ERROR,
                raw = "",
                error = e.message ?: e::class.simpleName
            )
        }
    }

    /** Backwards-compatible raw command API. */
    suspend fun command(command: String, timeoutMs: Long): String =
        commandRaw(command, timeoutMs)

    suspend fun command(command: String): String = command(command, 3_000)

    private suspend fun commandRaw(command: String, timeoutMs: Long): String = commandMutex.withLock {
        require(command.isNotBlank())

        withTimeout(timeoutMs) {
            val ascii = Charset.forName("US-ASCII")
            val acc = StringBuilder()

            val reader = transport.observeIncoming().takeWhile { chunk ->
                acc.append(chunk.toString(ascii))
                acc.indexOf('>') < 0
            }

            coroutineScope {
                val collectJob = async(start = CoroutineStart.UNDISPATCHED) {
                    reader.collect { }
                }

                transport.send((command.trim() + "\r").toByteArray(Charsets.US_ASCII)).getOrThrow()
                collectJob.await()
            }

            val text = acc.toString()
            val idx = text.indexOf('>')
            require(idx >= 0) { "ELM327 prompt not received" }

            text.substring(0, idx)
                .replace("\r", "\n")
                .lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .joinToString("\n")
                .trim()
        }
    }

    suspend fun close() {
        initialized = false
        runCatching { transport.disconnect() }
    }
}
