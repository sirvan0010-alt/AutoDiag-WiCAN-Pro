# SEOBD integration pipeline

This document defines the implementation contract for the complete diagnostic flow.

## Runtime chain

`WiCAN transport → Elm327Session → CapabilityDiscovery → ObdLiveDataEngine → DTC pipeline → ECU discovery → vehicle READ profile → EV health → repair intelligence → pre-purchase → AUTO TEST → MQTT/Home Assistant`

## 1. Live Data

`ObdLiveDataEngine` is the authoritative read-only Mode 01 polling layer. It serializes commands through `Elm327Session`, rejects unsupported PIDs, preserves failed samples as state, records evidence, and writes valid observations to `LiveDataStore`.

Android consumes `SensorSample` through `LiveDataViewModel`. UI must never manufacture a vehicle measurement. Simulator values are explicitly simulator data.

## 2. DTC / freeze frame

DTC screens consume normalized DTC evidence and expose stored/pending/permanent state, ECU scope, raw response, freeze-frame availability and verification. Clear remains a state-changing operation and requires explicit confirmation plus post-clear verification.

## 3. ECU discovery

Discovery is capability-first: functional scan, physical responses, identification, software/hardware/calibration identifiers and capability cache. A vehicle profile is accepted only when its evidence scope matches the current vehicle/ECU.

## 4. Vehicle READ profiles

Manufacturer profiles contain only verified or explicitly unverified signal definitions. Each signal carries source, scope, unit, decoder version and verification state. Unsupported signals are unavailable, not guessed.

## 5. EV health orchestration

The health engine runs `DATA_QUALITY → STATIC → LOAD → RECOVERY → TREND → CONFIDENCE`. It reports observations and confidence rather than universal battery thresholds. HV isolation/Riso data is safety-critical and remains read-only.

## 6. Repair intelligence

`DTC → possible causes → diagnostic checks → repair references → parts/labor/price estimate`. Every source retains provenance and exact vehicle/ECU scope. Broken or changed external references become `needs_review`.

## 7. Pre-purchase

The workflow state machine is:

`CONNECT → IDENTIFY → DISCOVER → DTC → FREEZE_FRAME → READINESS → LIVE_DATA → MONITORS → EV_TESTS → ANALYZE → REPAIR_ESTIMATE → REPORT`

The report aggregates evidence, confidence, scope and supported PASS/FAIL outcomes. It must not convert missing evidence into PASS.

## 8. AUTO TEST

AUTO TEST is a profile-driven orchestration of the same verified read-only stages. Each stage is cancellable, auditable and replayable. State-changing services are excluded unless their isolated safety policy explicitly permits them.

## 9. Remote telemetry

The telemetry boundary exposes normalized verified observations to MQTT/Home Assistant. Publishing must be rate-limited, timestamped, scoped to a vehicle profile and marked with quality/verification state. Commands are not exposed through the telemetry topic model.

## Safety invariants

- No invented CAN ID or signal mapping.
- No synthetic value presented as vehicle measurement.
- No unverified write/control command.
- Exact vehicle/ECU scope precedes profile acceptance.
- Evidence provenance follows every diagnostic value.
- Simulator and replay data remain visibly distinguished from live data.
