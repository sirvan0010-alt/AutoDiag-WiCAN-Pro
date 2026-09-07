# WiCAN firmware → AutoDiag reference map

Status: **reference extraction / implementation boundary**

Source repository: https://github.com/cdufresne81/nc-flash-wican-fw
Source branch: `wican-pro`

## Rule

This firmware is treated as a reference for the **device/acquisition layer**, not as application code to copy into Android. It is a Mazda NC / NC Flash oriented WiCAN PRO firmware fork; vehicle-specific flash functionality is outside the initial AutoDiag scope.

## Reusable architecture

| Firmware area | Concept | AutoDiag/WiCAN target | Action |
|---|---|---|---|
| `main/slcan_port.c` | raw SLCAN TCP endpoint | `android/core` raw CAN transport | REFERENCE / REIMPLEMENT CLIENT |
| `main/datalog_lease_task.c` | bus lease / coexistence interlock | transport/session arbitration | ADAPT CONCEPT |
| `components/fast_log/poll_log.c` | polling scheduler + broadcast capture | acquisition/sampling engine | REFERENCE |
| `components/csv_logger/` | timestamped wide data logging | capture/time-series store | ADAPT CONCEPT |
| `components/event_log/` | operational event ring | diagnostic session/event log | ADAPT |
| `components/crash_report/` | field failure evidence | device diagnostics/status | REFERENCE |
| `/poll_status` | live acquisition health | WiCAN device status | ADAPT |
| HTTP config/status endpoints | device management plane | WiCAN device API client | REUSE/EXTEND |
| `components/autopid/` | configurable PID/sensor definitions | future data-driven sampling | REFERENCE ONLY until evidence gates are satisfied |

## Important boundary

The firmware's logger can combine polled ECU values and broadcast CAN capture. AutoDiag should consume the resulting observations as **measurements with provenance**, not automatically as verified signal definitions.

A raw frame remains a raw frame until its protocol/meaning is independently established.

## Coexistence model

The lease/interlock concept is particularly valuable for AutoDiag:

```text
WiCAN device
 ├── raw CAN / diagnostic session
 ├── passive capture
 └── polling/logger
        ↓
   explicit ownership/arbitration
        ↓
 Android transport/session manager
```

The Android client must avoid starting concurrent operations that corrupt the CAN conversation or invalidate timing assumptions.

## Acquisition model

Target architecture:

```text
WiCAN
  ↓
CanFrame(timestamp, arbitrationId, flags, dlc, data)
  ├── raw capture store
  ├── passive decoder pipeline
  └── ISO-TP reassembly
          ↓
       UDS/KWP
          ↓
     DiagnosticResponse
          ↓
     DecoderCandidate
          ↓
    SignalObservation
          ↓
 Evidence + verification gate
```

## What is explicitly NOT imported

- Mazda NC-specific ROM read/write algorithms
- PCM flashing
- write commands derived from this firmware
- vehicle-specific thresholds
- vehicle-specific CAN/PID meanings without independent evidence

## Verification requirements

Any new device-side capability must be tested at three levels:

1. deterministic unit tests;
2. simulator/replay tests;
3. real WiCAN + vehicle validation where the feature depends on hardware/vehicle behaviour.

The firmware's own documentation states that its architecture and code are the source of truth when documentation disagrees with code. AutoDiag follows the same principle: current GitHub code/CI beats stale handoff text.
