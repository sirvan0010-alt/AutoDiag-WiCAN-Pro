package com.autodiag.core.obd

import kotlin.test.Test
import kotlin.test.assertEquals

class LiveDataPollPlanTest {
    @Test
    fun highPriorityPidsUseFastCadence() {
        listOf(0x04, 0x05, 0x0C, 0x0D, 0x11).forEach { pid ->
            assertEquals(LiveDataPriority.HIGH, LiveDataPidPolicy.priority(pid))
            assertEquals(500L, LiveDataPidPolicy.intervalMs(pid))
            assertEquals(LiveDataPollPlan(pid, LiveDataPriority.HIGH, 500L), LiveDataPidPolicy.plan(pid))
        }
    }

    @Test
    fun mediumPriorityPidsUseOneSecondCadence() {
        listOf(0x06, 0x07, 0x08, 0x09, 0x0E, 0x0F, 0x10).forEach { pid ->
            assertEquals(LiveDataPriority.MEDIUM, LiveDataPidPolicy.priority(pid))
            assertEquals(1_000L, LiveDataPidPolicy.intervalMs(pid))
        }
    }

    @Test
    fun unknownPidsRemainLowPriority() {
        listOf(0x01, 0x02, 0x22, 0xA0).forEach { pid ->
            assertEquals(LiveDataPriority.LOW, LiveDataPidPolicy.priority(pid))
            assertEquals(2_500L, LiveDataPidPolicy.intervalMs(pid))
            assertEquals(LiveDataPollPlan(pid, LiveDataPriority.LOW, 2_500L), LiveDataPidPolicy.plan(pid))
        }
    }
}
