package com.autodiag.core.diagnostic

import kotlin.test.Test
import kotlin.test.assertEquals

class DiagnosticEventTest {
    @Test
    fun streamPreservesEventOrder() {
        val stream = DiagnosticEventStream()
        stream.append(DiagnosticEvent(DiagnosticEventType.SESSION_STARTED, 1L, "s1"))
        stream.append(DiagnosticEvent(DiagnosticEventType.MEASUREMENT_RECEIVED, 2L, "s1", evidenceKey = "engine.rpm"))
        stream.append(DiagnosticEvent(DiagnosticEventType.SESSION_ENDED, 3L, "s1"))

        assertEquals(
            listOf(
                DiagnosticEventType.SESSION_STARTED,
                DiagnosticEventType.MEASUREMENT_RECEIVED,
                DiagnosticEventType.SESSION_ENDED
            ),
            stream.snapshot().map { it.type }
        )
    }
}
