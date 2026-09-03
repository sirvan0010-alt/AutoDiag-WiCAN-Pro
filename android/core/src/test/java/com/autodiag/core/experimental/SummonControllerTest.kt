package com.autodiag.core.experimental

import com.autodiag.core.experimental.summon.SummonBlockReason
import com.autodiag.core.experimental.summon.SummonController
import com.autodiag.core.experimental.summon.SummonExecutionMode
import com.autodiag.core.experimental.summon.SummonPhase
import com.autodiag.core.experimental.summon.SummonRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SummonControllerTest {
    private val okHold = SummonRequest(
        expertModeEnabled = true,
        featureToggleEnabled = true,
        holdActive = true,
        mode = SummonExecutionMode.DRY_RUN,
        parkEvidence = true,
        speedKmh = 0.0,
    )

    @Test
    fun disabledToggleBlocks() {
        val t = SummonController().tick(okHold.copy(featureToggleEnabled = false))
        assertEquals(SummonPhase.BLOCKED, t.phase)
        assertEquals(SummonBlockReason.FEATURE_DISABLED, t.blockReason)
        assertFalse(t.wouldTransmit)
    }

    @Test
    fun liveVehicleAlwaysForbidden() {
        val t = SummonController().tick(okHold.copy(mode = SummonExecutionMode.LIVE_VEHICLE))
        assertEquals(SummonBlockReason.LIVE_VEHICLE_FORBIDDEN, t.blockReason)
        assertFalse(t.wouldTransmit)
    }

    @Test
    fun dryRunHoldDoesNotTransmit() {
        val t = SummonController().tick(okHold)
        assertEquals(SummonPhase.HOLDING, t.phase)
        assertFalse(t.wouldTransmit)
    }

    @Test
    fun releaseHoldCancels() {
        val t = SummonController().tick(okHold.copy(holdActive = false))
        assertEquals(SummonPhase.CANCELLED, t.phase)
    }

    @Test
    fun unknownSpeedIsNotZero() {
        val t = SummonController().tick(okHold.copy(speedKmh = null))
        assertEquals(SummonBlockReason.MISSING_EVIDENCE, t.blockReason)
    }
}
