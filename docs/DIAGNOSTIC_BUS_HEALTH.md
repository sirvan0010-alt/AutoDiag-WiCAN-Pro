# Diagnostic Bus Health

AutoDiag provides a separate expert view for auto-electricians. It distinguishes protocol diagnostics from physical bus-health observations.

## Metrics

When the interface exposes them, record:

- CAN bitrate/configuration
- bus load percentage
- frames per second
- error frames
- bus-off state
- error-passive state
- dropped/overrun frames
- timestamp jitter
- arbitration IDs and payload length
- raw capture

A metric that the adapter cannot measure is `NOT_AVAILABLE`, not zero.

## 120 Ω termination

A theoretical/physical termination check is not inferred from normal CAN traffic. If a physical resistance measurement is performed with the appropriate vehicle power state and test equipment, store it as a separate physical test result with operator, timestamp and conditions.

AutoDiag must not claim "bad termination" solely from an inferred bus-load or frame pattern unless a verified evidence rule supports that conclusion.

## Error interpretation

The expert view should expose:

```text
BUS HEALTH

Bitrate              500 kbit/s
Bus load              31 %
Frames                 842/s
Error frames            0
Bus-off                 NO
Error passive           NO
Dropped frames          0

Status                  NORMAL
```

All fields carry provenance: interface-measured, vehicle-reported, decoded, or inferred.

## Scope

This module supports troubleshooting of wiring, termination, noise and bus configuration while remaining separate from DTC interpretation. It does not automatically issue diagnostic or control commands to recover a bus.
