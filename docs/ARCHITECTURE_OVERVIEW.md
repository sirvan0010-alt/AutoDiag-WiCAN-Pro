# AutoDiag — EV Diagnostics Architecture Overview

This is the entry point for `docs/`. Read it before implementing or reviewing diagnostic logic.

## Current authority

The project is documentation-first. Current authoritative architecture modules include:

- `docs/CAPABILITY_DISCOVERY.md` — what the vehicle/interface actually exposes
- `docs/AUTOMATION_ENGINE.md` — sessions, rules, notifications, remote telemetry and AUTO TEST
- `docs/DIAGNOSTIC_KNOWLEDGE_BASE.md` — DTC/alert explanations and source-linked OEM information
- `docs/IMPLEMENTATION_TASKS.md` — active implementation backlog
- battery/HV revision documents — evidence-based battery and Riso logic

Historical/draft numbers must never silently become production thresholds.

## Unified principle

> AutoDiag must never present an invented or inferred vehicle value as if it were directly measured, and must never turn an unsupported threshold into a definitive diagnosis.

If the vehicle exposes only a status, show a status. If it exposes a raw value, show the raw value with its source. If decoding is uncertain, label it uncertain.

## Diagnostic stack

```text
WiCAN PRO / other adapter
        |
        v
Transport
  TCP :3333 / TCP :23 / future transports
        |
        v
CAN / OBD framing
        |
        v
Capability Discovery
  vehicle + ECU + firmware scope
        |
        v
Vehicle decoder
  Tesla / VAG / Generic / future OEMs
        |
        +-----------------------------+
        |                             |
        v                             v
Live data                     DTC / Alerts
        |                             |
        +-------------+---------------+
                      v
             Diagnostic Engine
       STATIC / LOAD / RECOVERY
          / TREND / CONFIDENCE
                      |
          +-----------+-----------+
          |                       |
          v                       v
     Battery/HV              Knowledge Base
     Health tests             OEM procedures
          |                   explanations
          +---------+             |
                    v             |
                  History <-------+
                    |
                    v
              Automation Engine
          READ / NOTIFY / WRITE*
                    |
          +---------+---------+
          |                   |
          v                   v
      Remote telemetry     AUTO TEST
      MQTT / HA            report + replay

* WRITE is isolated, disabled by default and outside initial milestones.
```

## Capability-first behavior

The application first discovers capabilities before exposing detailed diagnostic claims. Discovery is granular and scoped to VIN + relevant software/firmware where available.

Example:

```text
Battery
  Cell voltage          ✓ available
  Cell temperature      ~ partial
  Riso numeric          ? unknown
  Riso status           ✓ available
```

A failed probe is not automatically an unavailable capability. Timeouts, malformed responses, unsupported services and unknown decoders remain distinct states.

## Battery test model

Battery analysis is not based on a single universal mV threshold. Samples retain timestamp, SOC, temperature, pack voltage, current, power, phase and available cell/module identity.

The engine distinguishes:

1. STATIC
2. LOAD
3. RECOVERY
4. TREND
5. CONFIDENCE

Peak cell-voltage difference during acceleration must not automatically identify the lowest-voltage cell as a failed/weak cell. Load response and recovery are separate observations.

## Charging analysis and replay

If cell-level data are available, AutoDiag records and replays individual cell/module behavior during both AC and DC charging. The expert replay view must allow the user to scrub time and inspect the exact available cell at that timestamp.

```text
Pack → Module/Brick → Cell → timestamp

Cell 137
4.103 V ── 4.151 V ── 4.168 V ── 4.189 V
             ^ user can inspect this sample
```

The replay engine uses a timestamp index/binary search so large captures remain responsive.

## Riso / HV isolation

Isolation is a separate safety-critical subsystem. The application distinguishes vehicle-reported numerical isolation, vehicle-reported status, physical test results and raw/undecoded data. AutoDiag never fabricates an MΩ value from a status or generic assumption.

## Diagnostic Knowledge Base

A finding can navigate directly through:

`finding → meaning → affected system → sourced checks → official explanation → official troubleshooting/service reference → related measurements`

OEM material is visibly separated from engineering and community sources. An unavailable OEM repair procedure remains unavailable rather than being replaced by generated instructions.

## Automatic Health Check / Sexy Button

The long-term one-tap `AUTO TEST` is profile-driven:

```text
Capability Discovery
  → identification
  → communication check
  → DTC / alert read
  → battery snapshot
  → cell/module snapshot if available
  → Riso/isolation if available
  → thermal data
  → charging data
  → drive-unit data
  → optional controlled road capture
  → analysis
  → report + replay
```

Unsupported stages become `NOT_AVAILABLE`, not `PASS` or `FAIL`.

## Automation / remote telemetry

The automation layer supports scheduled read-only telemetry, local/home Wi-Fi monitoring, MQTT/Home Assistant, notifications and user-defined rules. Notification actions have rate limits and audit logs. Rules are exportable and replayable in dry-run mode.

## Verification states

Every vehicle-specific signal, threshold, decoder and procedure uses:

- `unverified`
- `partially_verified`
- `verified`

Verification is scoped to vehicle generation, hardware, software and test conditions where practical.

## Implementation order

1. Data model and provenance/evidence model.
2. Transport abstraction and simulator.
3. CAN/OBD parsers and replay.
4. Capability Discovery.
5. Vehicle decoder framework.
6. Battery STATIC/LOAD/RECOVERY/TREND engine.
7. Battery fingerprint/history.
8. Riso/HV isolation model.
9. Diagnostic Knowledge Base.
10. Automatic Health Check orchestration.
11. Android UI and expert replay visualization.
12. Automation/remote telemetry.
13. Vehicle-specific profiles and evidence-backed thresholds.
14. Additional manufacturers.
