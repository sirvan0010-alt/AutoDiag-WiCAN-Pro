# AutoDiag — EV Diagnostics Architecture Overview

This document is the entry point for `docs/`. Read it before implementing or reviewing diagnostic logic.

## Current authority

The project is documentation-first. The repository currently contains the initial battery-analysis and UI-tooltip specifications; additional architecture modules are being introduced incrementally. A document is authoritative only when explicitly marked so.

### Rules

- `AI_CONTEXT.md` defines project-wide AI/development rules.
- `README.md` defines the public project purpose and high-level architecture.
- Battery diagnostic logic must remain evidence-based and context-aware.
- Historical/draft numbers must never silently become production thresholds.
- Vehicle-specific claims require a source and verification state.

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
          v                       |
       History <------------------+
```

## Battery test model

Battery analysis is not based on a single universal mV threshold.

Samples must retain context such as:

- timestamp
- SOC
- battery temperature
- pack voltage
- battery current
- power where available
- test phase (`REST`, `LOAD`, `RECOVERY`, `AC_CHARGE`, `DC_CHARGE`)
- cell/module measurements where available
- source and verification state

The engine should distinguish:

1. **STATIC** — rest/low-load observations.
2. **LOAD** — response under acceleration or another controlled load.
3. **RECOVERY** — how the system returns after load is removed.
4. **TREND** — repeated measurements of the same vehicle.
5. **CONFIDENCE** — completeness and quality of evidence.

Peak cell-voltage difference during acceleration must not automatically identify the lowest-voltage cell as a failed/weak cell. Load response and recovery are separate observations.

## Charging analysis

If cell-level data are available, AutoDiag should record and replay individual cell/module behavior during both:

- AC charging
- DC fast charging

The UI should be able to show:

- live minimum/maximum cell voltage
- delta between cells
- cell/module identity
- temperature and temperature delta
- current and pack voltage
- charging power
- evolution over time
- replay of the complete capture

At high SOC, the application should be able to identify which cell/module first develops a persistent deviation, without automatically declaring it defective.

## Battery visualization

Two views are required conceptually:

### Driver view

Simple language and restrained visual status:

```text
Battery health observation

🟢 No persistent abnormality observed

Cell balance       12 mV
Temperature delta   3.1 °C
Load response       recorded
Recovery            normal observation
Confidence           82%
```

### Expert / Replay view

Detailed numerical analysis:

- pack topology where known
- modules/bricks
- individual cells/groups where actually available
- voltage and temperature traces
- current/power trace
- event markers
- synchronized replay
- exact timestamps
- raw values

The visual battery map must be driven by a verified topology profile. It must not invent physical cell positions when the topology is unknown.

## Riso / HV isolation

Isolation is a separate safety-critical subsystem.

The application distinguishes:

- `vehicle_reported_value`
- `vehicle_reported_status`
- `physical_test_result`
- `raw_undecoded`

AutoDiag must never fabricate an MΩ value from a fault status or from generic assumptions. A vehicle-specific decoder may expose a numerical isolation value, a categorical status, or only a raw diagnostic response.

Physical HV insulation testing is not treated as equivalent to a CAN-reported value. Procedures involving HV test equipment must defer to the vehicle manufacturer's safety and service documentation.

## Diagnostic Knowledge Base

Every supported DTC/alert can have a structured knowledge entry:

```text
code
  -> vehicle / ECU scope
  -> official description
  -> severity
  -> symptoms / effects
  -> diagnostic conditions
  -> possible causes (only sourced)
  -> related measurements
  -> OEM troubleshooting procedure
  -> OEM repair procedure
  -> source URL/reference
  -> verification state
  -> last reviewed date
```

The UI should expose this through `What does it mean?`, `What should be checked?`, and `Service procedure` actions.

No source = no authoritative explanation. Community information may be shown separately and must never be presented as OEM guidance.

## Automatic Health Check

The long-term automatic test consists of safe read-only stages:

```text
Connection
  -> vehicle identification
  -> ECU communication
  -> DTC / alert scan
  -> battery snapshot
  -> cell/module snapshot
  -> HV isolation data (if exposed)
  -> thermal system
  -> charging data
  -> drive-unit data
  -> controlled road-test capture where appropriate
  -> analysis
  -> report + replay
```

The exact test sequence is vehicle-profile dependent. Unsupported stages become `NOT_AVAILABLE`, not `PASS`.

## Automation / remote telemetry

AutoDiag should eventually support:

- scheduled read-only telemetry
- Wi-Fi/home-network monitoring
- MQTT/Home Assistant integration
- event-based notifications
- user-defined read-only rules

Automation must not silently execute CAN writes.

## Verification states

Every vehicle-specific signal, threshold, decoder and procedure should use:

- `unverified`
- `partially_verified`
- `verified`

Verification is scoped to the exact vehicle generation, hardware, software and test conditions where practical.

## Historical specifications

Older drafts may contain community numbers or provisional thresholds. They are useful research material but are not production truth unless an evidence record resolves them.

This prevents a community observation such as a particular mV value from becoming a hidden universal diagnostic rule.

## Implementation order

1. Data model and provenance/evidence model.
2. Transport abstraction and simulator.
3. CAN/OBD parsers and replay.
4. Vehicle decoder framework.
5. Battery STATIC/LOAD/RECOVERY/TREND engine.
6. Battery fingerprint/history.
7. Riso/HV isolation model.
8. Diagnostic Knowledge Base.
9. Automatic Health Check orchestration.
10. Android UI and expert replay visualization.
11. Vehicle-specific profiles and evidence-backed thresholds.
12. Additional manufacturers.
