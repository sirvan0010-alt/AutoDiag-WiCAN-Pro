# Automation Engine

## Purpose

The Automation Engine turns verified read-only vehicle data into repeatable rules, scheduled tests, remote telemetry and notifications. It is intentionally separate from CAN/OBD transport and vehicle decoding.

## Current implementation boundary

The current Android core implements the deterministic rule/evaluation layer, replay, trigger edge detection, notification cooldowns, data-quality gating and audit events. Standard OBD Mode 01 data can be promoted into semantic signals only through the explicit `ObdSemanticSignalAdapter`; only the registered SAE J1979 PID set is promoted. Manufacturer-specific signals require their own verified vehicle-profile adapter.

The current implementation does **not** prove a live vehicle producer for Tesla/Fleet Telemetry, nor does it promote undocumented Tesla CAN/UDS identifiers. Tessie-derived Tesla strings are replay/evidence candidates until an independent producer and protocol evidence are available.

## Action classes

- `READ / LOG / ANALYZE` — acquire data, persist captures, calculate metrics and analyze replay sessions.
- `NOTIFY / ALERT` — push/local notifications, MQTT and Home Assistant events. These have independent cooldowns, rate limits and audit logging.
- `WRITE / COMMAND` — disabled by default and outside the initial diagnostic automation layer. No read-only rule may reach it accidentally.

## Rule model

Rules are data, not UI-only state. They are exportable/versionable as JSON and reference stable semantic signals rather than undocumented CAN IDs.

## Data quality gate

Before a rule is evaluated, every required trigger/condition signal must be present, have a non-null value and carry a valid timestamp no older than the configured maximum age. Future timestamps are rejected. A rejected sample produces an explicit `DATA_QUALITY_REJECTED` audit event containing only rule metadata, the rejection reason and stale/missing signal IDs; credentials and secrets are never included.

## Dry-run / replay

Every rule can be simulated against a recorded session before activation. The simulator reports trigger timestamps, satisfied conditions, measured values/context, the action that would execute, and notification cooldown suppression. Replay never sends vehicle commands.

## Session boundaries

A recorded session has explicit phases: `PARKED`, `REST`, `DRIVE`, `LOAD_TEST`, `RECOVERY`, `AC_CHARGE`, `DC_CHARGE`, `POST_CHARGE`, `ENDED`. The logger must close sessions deliberately and record start/end timestamps and end reason.

## Automatic Health Check

The one-tap `AUTO TEST` is profile-driven and capability-driven, not a universal fixed script:

```text
Capability Discovery
 -> vehicle identification / market hint
 -> communication check
 -> DTC / vehicle alerts
 -> battery snapshot
 -> module/cell data when available
 -> HV isolation / Riso when available
 -> thermal data
 -> charging state/data
 -> drive-unit data
 -> optional controlled road capture
 -> STATIC / LOAD / RECOVERY / TREND / CONFIDENCE analysis
 -> report + replay session
```

Unsupported capabilities are `NOT_AVAILABLE`, never automatic failures.

## Pre-purchase test

`PRE_PURCHASE_TEST` is a dedicated read-only workflow for a borrowed/inspected vehicle. It must optimize useful evidence within limited access time and report confidence instead of inventing a definitive SOH percentage.

When supported, capture VIN/model/year/market hint, DTCs and warnings, battery SOC/voltage/current/temperature, module and cell voltage/temperature, cell imbalance across rest/load/recovery, AC/DC charging observations, HV isolation/Riso and drive-unit/thermal data. If the vehicle is available for only about one hour, missing long-rest baseline is explicitly shown as `Limited assessment`.

## US-market indication

Market detection must be evidence-based: VIN decoding, verified vehicle metadata or a verified OEM diagnostic signal. If `market_hint=US` is supported with a confidence/source, the UI displays a visible `⚠ US-market vehicle detected` indicator and explains the source. A guessed VIN pattern alone must not be presented as fact.

## Battery charging and cell tracking

When cell-level data exists, the same tracking engine operates during both `AC_CHARGE` and `DC_CHARGE`. Record absolute cell voltage, deviation from pack/peers, minimum/maximum cell identity, temperature/delta, charge current/power, SOC and phase duration.

The instantaneous lowest cell during acceleration is **not** automatically called the weakest cell. Under load it is a voltage-response observation. Stronger conclusions require persistent behavior across suitable conditions and evidence.

## Replay hierarchy

Replay follows:

`Session → Phase → Pack → Module → Cell → Sample`

The user can scrub through time and inspect the exact recorded voltage of every available cell at that timestamp. A selected cell retains its history while pack/module views highlight its relative deviation. Timestamp indexing uses binary-search-friendly structures so thousands of samples do not require a full scan for every scrub operation.

## Remote telemetry

When supported by the interface and vehicle, read-only telemetry may be exposed through a live dashboard, MQTT, Home Assistant, notifications and periodic health snapshots. Wi-Fi sleep, vehicle sleep, adapter power loss, reconnects and stale data must be handled explicitly; stale data must never be shown as live.

## Explainability

Every alert/report finding must state why it exists and which diagnostic pillar contributed: `STATIC`, `LOAD`, `RECOVERY`, `TREND` or `CONFIDENCE`. It must include the observation, context and evidence/threshold provenance. Insufficient evidence becomes `UNKNOWN`/`LIMITED_ASSESSMENT`, never a fabricated fault.

## Auditability

Every evaluated rule records rule ID/version, timestamp, observed values, condition results, action/policy and outcome. Data-quality rejections are also recorded as `DATA_QUALITY_REJECTED` with missing/stale signal IDs. Vehicle identity, capability snapshots and command results belong to higher integration layers and are not fabricated by the core rule engine.

## Safety boundaries

- Read-only is the default.
- No hidden writes, coding or resets.
- No automatic contactor/actuator commands.
- A future WRITE subsystem requires a separate safety review, explicit user confirmation, vehicle-specific allowlists and independent audit logging.

## Implementation order

1. Rule data model
2. Replay/dry-run evaluator
3. Notification abstraction and rate limiting
4. Data-quality gate and audit trail
5. Session manager
6. Connect verified live producers to semantic signals
7. Scheduled read-only telemetry
8. Automatic Health Check orchestrator
9. MQTT/Home Assistant integration
10. UI rule editor
11. Only after separate safety review: experimental write subsystem
