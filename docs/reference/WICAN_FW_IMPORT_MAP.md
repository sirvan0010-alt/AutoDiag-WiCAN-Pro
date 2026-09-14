# WiCAN firmware → AutoDiag reference map

Status: **reference extraction / implementation boundary**

## Firmware sources

### Primary upstream

- `https://github.com/meatpiHQ/wican-fw`
- default branch: `main`

### Public fork evidence

The public WiCAN fork network is treated as an additional evidence pool. Current inspected examples are recorded in `docs/reference/WICAN_FORK_EVIDENCE_REGISTER.md`.

Important rule: a fork can demonstrate an implementation or integration pattern, but it does not automatically prove a vehicle-specific diagnostic decoder.

### Additional implementation reference

- `https://github.com/cdufresne81/nc-flash-wican-fw`
- branch: `wican-pro`

This repository is a Mazda NC / NC Flash oriented WiCAN PRO firmware fork. Vehicle-specific flash functionality remains outside the initial AutoDiag scope.

## Rule

WiCAN firmware is treated as a reference for the **device/acquisition layer**, not as application code to copy into Android.

## Reusable architecture

| Firmware area / ecosystem capability | Concept | AutoDiag/WiCAN target | Action |
|---|---|---|---|
| `main/slcan_port.c` in reference fork | raw SLCAN TCP endpoint | `android/core` raw CAN transport | REFERENCE / REIMPLEMENT CLIENT |
| `main/datalog_lease_task.c` in reference fork | bus lease / coexistence interlock | transport/session arbitration | ADAPT CONCEPT |
| `components/fast_log/poll_log.c` in reference fork | polling scheduler + broadcast capture | acquisition/sampling engine | REFERENCE |
| `components/csv_logger/` in reference fork | timestamped wide data logging | capture/time-series store | ADAPT CONCEPT |
| `components/event_log/` in reference fork | operational event ring | diagnostic session/event log | ADAPT |
| `components/crash_report/` in reference fork | field failure evidence | device diagnostics/status | REFERENCE |
| `/poll_status` in reference fork | live acquisition health | WiCAN device status | ADAPT |
| HTTP config/status endpoints | device management plane | WiCAN device API client | REUSE/EXTEND |
| `components/autopid/` in reference fork | configurable PID/sensor definitions | future data-driven sampling | REFERENCE ONLY until evidence gates are satisfied |
| upstream WiCAN SLCAN/socketCAN support | raw CAN transport | transport abstraction | IMPLEMENT CLIENT-SIDE |
| upstream/fork ELM327-family interfaces | command/response framing | ELM327 transport adapter | IMPLEMENT CLIENT-SIDE |
| upstream/fork reverse-engineering tooling | capture/investigation workflow | evidence/capture tooling | ADAPT, KEEP PROVENANCE |

## Import boundary

The firmware implementation remains external. AutoDiag imports **protocol contracts, observations and architectural concepts**, not copied firmware source.

The upstream repository reports GPL-3.0. Any future source-code reuse requires a separate license review. Client-side reimplementation of documented/observed interfaces remains the default approach.

## Important diagnostic boundary

The firmware's logger can combine polled ECU values and broadcast CAN capture. AutoDiag must consume the resulting observations as **measurements with provenance**, not automatically as verified signal definitions.

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
- write commands derived from a firmware fork
- vehicle-specific thresholds
- vehicle-specific CAN/PID meanings without independent evidence
- any decoder solely because it appears in a WiCAN vehicle profile/fork

## Verification requirements

Any new device-side capability must be tested at three levels:

1. deterministic unit tests;
2. simulator/replay tests;
3. real WiCAN + vehicle validation where the feature depends on hardware/vehicle behaviour.

For diagnostic data, the canonical source is `AutoDiag-WiCAN-Diagnostic-Data`. Candidate data must pass its validation/evidence gate before AutoDiag treats it as available runtime knowledge.

The firmware's own documentation states that its architecture and code are the source of truth when documentation disagrees with code. AutoDiag follows the same principle: current GitHub code/CI beats stale handoff text.
