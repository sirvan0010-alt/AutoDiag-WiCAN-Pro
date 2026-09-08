# S3XY Commander / Button Integration

This integration is an optional external transport/telemetry source. It is not assumed to be present, and AutoDiag must not assume that a proprietary BLE or CAN protocol is available without verification.

## Architecture

```text
S3XY Commander / compatible telemetry source
              |
        BLE / supported API
              |
              v
      S3XY Adapter Reader
              |
       normalized signals
              v
        AutoDiag Engine
```

If a compatible Commander stream is detected and verified, AutoDiag can consume telemetry passively instead of issuing duplicate polling requests for the same signals. This is a performance optimization, not a guarantee that CAN traffic is zero-overhead.

## Button triggers

The WiCAN Pro itself also has a multifunction push button. AutoDiag may expose a generic hardware-trigger abstraction so a supported physical button can initiate:

- **Blackbox Snapshot** — save a configurable pre/post-event buffer.
- **Quick Diagnostic** — start a read-only DTC/capability snapshot.
- **High-frequency logging** — start/stop a configured capture profile.
- **AUTO TEST** — start the safe, read-only automatic health test.

For a 30 s pre-event + 15 s post-event capture, the ring buffer must already be running; the button cannot recover data that was never captured.

## S3XY-specific macros

Brake-service, HVAC actuation, battery preconditioning overrides and other write/command macros are **not part of the initial integration**. They belong to the isolated WRITE/COMMAND subsystem and require:

1. verified command semantics for the exact vehicle/software scope,
2. explicit user confirmation,
3. safety interlocks,
4. simulator/replay tests,
5. a dedicated safety review.

They must never be triggered automatically by a notification rule or by an unverified button mapping.

## Telemetry normalization

Every imported signal retains provenance:

```json
{
  "signal": "drive_unit.power",
  "value": 82.1,
  "unit": "kW",
  "source": "external_telemetry",
  "source_device": "S3XY_Commander",
  "decoder_version": "...",
  "verification": "partially_verified",
  "timestamp": "ISO8601"
}
```

A signal from a third-party decoder is never relabeled as OEM-reported merely because it originated on the vehicle CAN bus.
