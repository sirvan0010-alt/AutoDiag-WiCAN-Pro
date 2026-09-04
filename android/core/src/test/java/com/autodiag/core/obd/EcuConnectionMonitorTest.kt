package com.autodiag.core.obd

import kotlin.test.Test
import kotlin.test.assertEquals

class EcuConnectionMonitorTest {
    @Test
    fun successfulDiagnosticResponseMakesEcuOnline() {
        var now = 10_000L
        val monitor = EcuConnectionMonitor(nowMs = { now })

        assertEquals(EcuConnectionState.CONNECTING, monitor.state())
        monitor.onSuccess()
        assertEquals(EcuConnectionState.ONLINE, monitor.state())
    }

    @Test
    fun repeatedFailuresMakeEcuDegraded() {
        var now = 10_000L
        val monitor = EcuConnectionMonitor(nowMs = { now })
        monitor.onSuccess()

        monitor.onFailure()
        monitor.onFailure()
        assertEquals(EcuConnectionState.ONLINE, monitor.state())

        monitor.onFailure()
        assertEquals(EcuConnectionState.DEGRADED, monitor.state())
    }

    @Test
    fun elapsedSilenceMakesEcuOffline() {
        var now = 10_000L
        val monitor = EcuConnectionMonitor(nowMs = { now })
        monitor.onSuccess()

        now = 15_001L
        assertEquals(EcuConnectionState.OFFLINE, monitor.state())
    }

    @Test
    fun transportWithoutDiagnosticResponseDoesNotBecomeOnline() {
        val monitor = EcuConnectionMonitor(nowMs = { 10_000L })
        monitor.onFailure()
        monitor.onFailure()
        monitor.onFailure()

        assertEquals(EcuConnectionState.CONNECTING, monitor.state())
    }
}
