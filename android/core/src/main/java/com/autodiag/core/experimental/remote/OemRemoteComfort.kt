package com.autodiag.core.experimental.remote

/**
 * Product ideas from MMC Remote Ctrl (lock / HVAC / charge timer) as a
 * **contract**, not a port of Inventec iMobile2.
 *
 * Execution requires an official OEM API implementation. Default transport
 * always returns BLOCKED.
 */
enum class RemoteComfortAction {
    LOCK,
    UNLOCK,
    HVAC_HEAT,
    HVAC_COOL,
    HVAC_OFF,
    CHARGE_START,
    CHARGE_STOP,
    CHARGE_TIMER_SET,
}

enum class RemoteComfortResult {
    BLOCKED_NO_OFFICIAL_API,
    BLOCKED_NOT_LICENSED,
    DRY_RUN_OK,
    EXECUTED,
}

data class RemoteComfortRequest(
    val action: RemoteComfortAction,
    val officialApiConfigured: Boolean = false,
    val dryRun: Boolean = true,
)

interface OemRemoteTransport {
    fun execute(request: RemoteComfortRequest): RemoteComfortResult
}

object BlockedOemRemoteTransport : OemRemoteTransport {
    override fun execute(request: RemoteComfortRequest): RemoteComfortResult {
        if (!request.officialApiConfigured) return RemoteComfortResult.BLOCKED_NO_OFFICIAL_API
        if (request.dryRun) return RemoteComfortResult.DRY_RUN_OK
        return RemoteComfortResult.BLOCKED_NOT_LICENSED
    }
}
