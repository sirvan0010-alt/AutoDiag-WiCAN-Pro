package com.autodiag.core.experimental

import com.autodiag.core.experimental.remote.BlockedOemRemoteTransport
import com.autodiag.core.experimental.remote.RemoteComfortAction
import com.autodiag.core.experimental.remote.RemoteComfortRequest
import com.autodiag.core.experimental.remote.RemoteComfortResult
import org.junit.Assert.assertEquals
import org.junit.Test

class OemRemoteComfortTest {
    @Test
    fun lockWithoutApiBlocked() {
        val r = BlockedOemRemoteTransport.execute(
            RemoteComfortRequest(RemoteComfortAction.LOCK, officialApiConfigured = false),
        )
        assertEquals(RemoteComfortResult.BLOCKED_NO_OFFICIAL_API, r)
    }

    @Test
    fun dryRunWithApiConfigured() {
        val r = BlockedOemRemoteTransport.execute(
            RemoteComfortRequest(
                RemoteComfortAction.HVAC_HEAT,
                officialApiConfigured = true,
                dryRun = true,
            ),
        )
        assertEquals(RemoteComfortResult.DRY_RUN_OK, r)
    }
}
