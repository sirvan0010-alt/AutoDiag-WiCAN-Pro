package com.autodiag.core.diagnostic

import com.autodiag.core.can.CanFrame

/** Thin UDS request/response layer over one address-bound ISO-TP session. */
class UdsClient(private val session: IsoTpSession) {
    private var outstanding: Uds.Request? = null

    suspend fun request(service: Int, payload: ByteArray = byteArrayOf()): Result<Unit> {
        check(outstanding == null) { "A UDS request is already outstanding" }
        val request = Uds.Request(service, payload)
        outstanding = request
        val result = session.send(request.bytes())
        if (result.isFailure) outstanding = null
        return result
    }

    suspend fun accept(frame: CanFrame): Result<Uds.Response?> {
        val request = outstanding ?: return Result.success(null)
        return session.accept(frame).map { payload ->
            if (payload == null) return@map null
            val response = Uds.parse(request, payload)
            // 0x7F .. 0x78 is an interim response. Keep the request alive;
            // P2/P2* timeout policy belongs to the session/orchestrator.
            if (!response.pending) outstanding = null
            response
        }
    }

    fun reset() {
        outstanding = null
        session.reset()
    }

    fun hasOutstandingRequest(): Boolean = outstanding != null
}
