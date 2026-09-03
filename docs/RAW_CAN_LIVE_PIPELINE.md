# RAW CAN live pipeline

The first real-time WiCAN diagnostic path is deliberately transport-agnostic:

```text
WiCAN PRO
   -> TCP / SLCAN transport
   -> byte stream
   -> SLCAN codec
   -> CanFrame
   -> CanFrameStream
   -> live monitor / capture / diagnostics
```

`CanFrame` is the canonical classic-CAN 2.0 frame model. `CanFrameStream` is a hot coroutine `Flow` used to fan frames out to multiple consumers without coupling the UI to the transport.

## Rules

- Transport code must not know about Android UI.
- Diagnostic protocol code consumes frames through the shared stream.
- The stream does not replay old frames; capture/history is a separate concern.
- Backpressure is bounded by the stream buffer. A future production adapter must surface drops explicitly rather than silently treating them as transport errors.
- CAN-FD is intentionally not represented by this model yet; WiCAN PRO hardware/firmware support must be verified before adding it.

## Next integration

The next implementation step is to connect the verified SLCAN/TCP reader to `CanFrameStream`, then expose a live CAN monitor with ID/DLC/data, rate, byte rate, filters, drop/error counters and capture controls.
