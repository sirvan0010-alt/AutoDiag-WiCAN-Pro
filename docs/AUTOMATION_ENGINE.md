# Automation Engine

## Purpose

The Automation Engine turns verified read-only vehicle data into repeatable rules, scheduled tests, remote telemetry and notifications. It is intentionally separate from CAN/OBD transport and vehicle decoding.

## Action classes

Automation actions are explicitly classified:

### READ / LOG / ANALYZE

Read vehicle data, record a session, calculate metrics, create a report, or run a replay analysis. These actions do not command the vehicle.

### NOTIFY / ALERT

Push notification, local notification, MQTT publish, Home Assistant event, or other external alert. Notification actions have their own rate limits, cooldowns and audit log. They are not vehicle writes.

### WRITE / COMMAND

Any action that changes vehicle state or transmits a control/diagnostic write. This class is disabled by default and is outside the initial AutoDiag milestones. It must never be reached accidentally by a read-only rule.

## Rule model

Rules are data, not UI-only configuration. They should be exportable and versionable as JSON/YAML.

Conceptual example:

```yaml
name: Tesla parked battery monitor
when:
  vehicle: identified
if:
  - signal: battery.soc
    operator: lt
    value: 30
then:
  - action: notify
    channel: mqtt
    message: "Battery SOC below configured level"
```

A rule references signals by stable semantic identifiers. It must not contain undocumented CAN IDs as a substitute for a decoder.

## Dry-run / replay simulation

Every rule should support simulation against a recorded session before activation.

The simulator reports:

- when the rule would have triggered
- which samples satisfied each condition
- which action would have executed
- how often notification cooldowns would have suppressed an action

No vehicle command is sent during replay.

## Session boundaries

A session is an explicit unit of recorded evidence. Recommended phases:

- `PARKED`
- `REST`
- `DRIVE`
- `LOAD_TEST`
- `RECOVERY`
- `AC_CHARGE`
- `DC_CHARGE`
- `POST_CHARGE`
- `ENDED`

The orchestration layer closes a session deliberately rather than relying on an endless logger. Battery and charging analyses consume the appropriate session type.

## Automatic Health Check

The automatic test is a profile-driven sequence, not a fixed universal script:

```text
Capability Discovery
  -> identification
  -> communication check
  -> DTC/alert read
  -> battery snapshot
  -> cell/module snapshot if available
  -> HV isolation/Riso if available
  -> thermal data
  -> charging state/data
  -> drive-unit data
  -> optional controlled road capture
  -> STATIC/LOAD/RECOVERY analysis
  -> report
  -> replayable session
```

Unsupported capabilities become `NOT_AVAILABLE`; they do not become failures.

## Remote telemetry / home use

A core project goal is to allow an owner to leave the WiCAN interface in the vehicle and monitor read-only telemetry over the home Wi-Fi network when the vehicle/interface supports it.

Potential outputs:

- live dashboard
- MQTT
- Home Assistant
- local notifications
- periodic health snapshots
- battery/temperature trend history

The design must account for Wi-Fi sleep, vehicle sleep, adapter power loss, reconnection and stale data. A stale value must never be presented as live.

## Sexy Button / one-tap workflow

The UI should eventually provide a prominent one-tap action such as:

> **AUTO TEST**

The button starts only the tests supported by the discovered vehicle capabilities and current safety policy. It does not mean every possible diagnostic function is executed.

Example result:

```text
AUTO TEST COMPLETE

Vehicle          Tesla Model Y
Communication   OK
DTC / Alerts    0 detected
Battery         Data captured
Cell analysis   Available
Riso            Status available
Charging        Not tested
Drive unit      Data captured

Confidence      84%
Replay          Available
```

## Auditability

Every rule execution records:

- rule ID/version
- timestamp
- vehicle identity scope
- capability snapshot
- input values
- condition results
- actions attempted
- notification result
- errors/timeouts

## Safety boundaries

- Read-only is the default.
- No hidden writes.
- No automatic coding.
- No automatic resets.
- No automatic contactor/actuator commands.
- Experimental WRITE support, if ever added, must use a separate command subsystem and explicit user confirmation.

## Implementation order

1. Rule data model
2. replay/dry-run evaluator
3. notification abstraction and rate limiting
4. session manager
5. scheduled read-only telemetry
6. Automatic Health Check orchestrator
7. MQTT/Home Assistant integration
8. UI rule editor
9. only after separate safety review: experimental write subsystem
