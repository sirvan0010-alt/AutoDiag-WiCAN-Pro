# Automation Engine

## Purpose

The Automation Engine turns verified read-only vehicle data into repeatable rules, scheduled tests, remote telemetry and notifications. It is intentionally separate from CAN/OBD transport and vehicle decoding.

## Action classes

- `READ / LOG / ANALYZE` — acquire data, persist captures, calculate metrics and analyze replay sessions.
- `NOTIFY / ALERT` — push/local notifications, MQTT and Home Assistant events. These have independent cooldowns, rate limits and audit logging.
- `WRITE / COMMAND` — disabled by default and outside the initial diagnostic automation layer. No read-only rule may reach it accidentally.

## Rule model

Rules are data, not UI-only state. They should be exportable/versionable as JSON or YAML and reference stable semantic signals rather than undocumented CAN IDs.

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

Every execution records rule ID/version, timestamp, vehicle identity scope, capability snapshot, input values, condition results, actions attempted, notification result and errors/timeouts.

## Safety boundaries

- Read-only is the default.
- No hidden writes, coding or resets.
- No automatic contactor/actuator commands.
- A future WRITE subsystem requires a separate safety review, explicit user confirmation, vehicle-specific allowlists and independent audit logging.

## Implementation order

1. Rule data model
2. Replay/dry-run evaluator
3. Notification abstraction and rate limiting
4. Session manager
5. Scheduled read-only telemetry
6. Automatic Health Check orchestrator
7. MQTT/Home Assistant integration
8. UI rule editor
9. Only after separate safety review: experimental write subsystem
