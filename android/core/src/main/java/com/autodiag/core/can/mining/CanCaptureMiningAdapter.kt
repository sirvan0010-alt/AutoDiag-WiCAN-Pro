package com.autodiag.core.can.mining

import com.autodiag.core.can.CanCaptureSession

/**
 * Converts the existing capture-v1 in-memory model to the mining view at the
 * ingest boundary. Original capture records/artifacts remain unchanged.
 */
fun CanCaptureSession.toCanFrameViews(): List<CanFrameView> =
    records.map { record ->
        val frame = record.frame
        CanFrameView(
            timestampMillis = record.timestampNanos / NANOS_PER_MILLI,
            canId = frame.id,
            extended = frame.isExtended,
            remote = frame.isRemote,
            dlc = frame.dataLength,
            payload = frame.data.copyOf(),
        )
    }

private const val NANOS_PER_MILLI = 1_000_000L
