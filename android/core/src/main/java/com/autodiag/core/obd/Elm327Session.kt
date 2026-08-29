package com.autodiag.core.obd

import com.autodiag.core.transport.WiCanTransport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/** Read-only ELM327 session. Commands are serialized and the ELM prompt is framed across TCP chunks. */
class Elm327Session(private val transport: WiCanTransport) {
    private val commandMutex = Mutex()

    suspend fun initialize(): Result<Unit> = runCatching {
        command("ATZ", 3_000)
        command("ATE0")
        command("ATL0")
        command("ATH1")
        command("ATSP0")
    }

    suspend fun command(command: String, timeoutMs: Long = 3_000): String = commandMutex.withLock {
        require(command.isNotBlank())
        val response = kotlinx.coroutines.coroutineScope {
            val waiter = async(Dispatchers.IO) {
                withTimeout(timeoutMs) {
                    val builder = StringBuilder()
                    transport.observeIncoming().collect { chunk ->
                        builder.append(chunk.toString(Charsets.US_ASCII))
                        if (builder.contains(">")) throw PromptReached(builder.toString())
                    }
                    error("WiCAN incoming stream ended before ELM prompt")
                }
            }
            try {
                transport.send((command.trim() + "\r").toByteArray()).getOrThrow()
                waiter.await()
            } catch (t: PromptReached) {
                t.payload.removeSuffix(">")
            }
        }
        response.replace(">", "").trim()
    }

    private class PromptReached(val payload: String) : CancellationException()
}
