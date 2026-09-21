package com.autodiag.core.diagnostic

import com.autodiag.core.can.CanFrame
import kotlinx.coroutines.withTimeout

/** Explicit UDS timing policy. Values are supplied by the caller; no OEM timing is assumed. */
data class UdsTimeoutPolicy(
    val p2Millis: Long,
    val p2StarMillis: Long,
) {
    init {
        require(p2Millis > 0) { "P2 timeout must be > 0 ms" }
        require(p2StarMillis > 0) { "P2* timeout must be > 0 ms" }
    }
}

/**
 * Optional request/response orchestration above UdsClient.
 *
 * P2 applies while waiting for the first response. NRC 0x78 (Response Pending)
 * switches the active deadline to P2*. A subsequent 0x78 restarts P2*.
 * Cancellation and timeout always reset the client state.
 */
suspend fun UdsClient.requestAndWait(
    service: Int,
    payload: ByteArray = byteArrayOf(),
    policy: UdsTimeoutPolicy,
    receiveFrame: suspend () -> CanFrame,
): Result<Uds.Response> = runCatching {
    request(service, payload).getOrThrow()

    try {
        var timeoutMillis = policy.p2Millis
        while (true) {
            val response = withTimeout(timeoutMillis) {
                var parsed: Uds.Response? = null
                while (parsed == null) {
                    parsed = accept(receiveFrame()).getOrThrow()
                }
                parsed
            }

            if (!response.pending) {
                return@runCatching response
            }

            timeoutMillis = policy.p2StarMillis
        }
    } finally {
        if (hasOutstandingRequest()) reset()
    }
}
