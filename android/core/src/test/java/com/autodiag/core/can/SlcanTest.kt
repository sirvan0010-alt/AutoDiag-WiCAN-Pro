package com.autodiag.core.can

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SlcanTest {
    @Test fun fragmentedStandardFrame() {
        val p = SlcanParser()
        assertTrue(p.accept("t1233A1B2C\r".substring(0, 5).encodeToByteArray()).isEmpty())
        val events = p.accept("3A1B2C\r".encodeToByteArray())
        val frame = (events.single() as SlcanParser.Event.Frame).frame
        assertEquals(0x123, frame.id)
        assertEquals("A1 B2 C", frame.hex())
    }

    @Test fun extendedAndRemoteFrames() {
        val p = SlcanParser()
        val events = p.accept("T18DAF1103A1B2C\rt1231\rR18DAF1100\r".encodeToByteArray())
        assertEquals(3, events.size)
        val ext = (events[0] as SlcanParser.Event.Frame).frame
        assertTrue(ext.isExtended)
        val remote = (events[2] as SlcanParser.Event.Frame).frame
        assertTrue(remote.isExtended && remote.isRemote)
    }

    @Test fun malformedFrameIsReported() {
        val p = SlcanParser()
        val event = p.accept("t1239\r".encodeToByteArray()).single()
        assertTrue(event is SlcanParser.Event.Error)
    }
}
