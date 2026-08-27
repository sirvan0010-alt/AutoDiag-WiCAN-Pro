# AutoDiag Automation & Sexy Button Layer

## Status

Architecture/specification. The existing AutoDiag feature catalog reserves remote monitoring, MQTT, Home Assistant and user rules, but this document defines the complete automation layer and its extension model.

## Goal

AutoDiag should not be only a diagnostic reader. It should also provide a configurable, read-first automation layer that can use WiCAN PRO telemetry and approved vehicle capabilities.

The goal is a software replacement/extension of the useful parts of a Sexy Button-style workflow without pretending that every vehicle action is safe or supported.

## What WiCAN PRO already provides

WiCAN PRO already supports Wi-Fi/BLE/USB, MQTT/MQTTS, Home Assistant integration, a physical button, vehicle automation/AutoPID features and raw/SLCAN interfaces. AutoDiag sits above this layer and adds the Android UI, rules, logging, diagnostics and vehicle-specific capability model.

## AutoDiag automation model

```text
Vehicle / CAN
      ↓
WiCAN PRO
      ↓
Transport
      ↓
Signal / Diagnostic Decoder
      ↓
Automation Engine
      ├── conditions
      ├── timers
      ├── state machine
      ├── schedules
      ├── debounce / hysteresis
      └── safety policy
      ↓
Actions
      ├── notification
      ├── Android action
      ├── MQTT
      ├── Home Assistant
      ├── logging / test start
      └── vehicle action (only when explicitly verified)
```

## Sexy Button-style actions

The UI should provide configurable large action buttons for supported functions, for example:

- start EV Health Test
- start battery logging
- start charging log
- start DC charging analysis
- save current snapshot
- start replay capture
- send data to Home Assistant
- wake/monitor session where supported
- execute a verified vehicle action

Vehicle-control actions are always manufacturer/profile specific.

## User-defined automations

Example:

```text
WHEN
  vehicle connected
AND
  SOC < 30%
AND
  home_wifi = true

THEN
  notify user
  start battery telemetry
  publish MQTT state
```

Another example:

```text
WHEN
  DC charging starts

THEN
  start charging test
  log cell/module telemetry
  log pack current/voltage/power
  log temperatures
```

Another:

```text
WHEN
  battery temperature delta exceeds resolved profile condition

THEN
  create diagnostic event
  save snapshot
  notify user
```

The condition engine may only use signals that are actually decoded and available.

## Rules

Each automation contains:

- id
- name
- enabled
- vehicle scope
- trigger
- conditions
- action list
- cooldown
- debounce/hysteresis
- required data signals
- safety class
- provenance

Example schema:

```json
{
  "id": "ev_health_on_charge",
  "name": "Start EV charging health log",
  "enabled": true,
  "vehicle_scope": "tesla_model_y",
  "trigger": {
    "signal": "charging.state",
    "equals": "charging"
  },
  "conditions": [],
  "actions": [
    {"type": "start_test", "test": "charging"},
    {"type": "mqtt_publish", "topic": "autodiag/vehicle/status"}
  ],
  "cooldown_s": 300,
  "safety_class": "read_only"
}
```

## Safety classes

### READ_ONLY

Allowed automatically:
- telemetry
- logging
- diagnostic reads
- snapshots
- reports
- notifications
- MQTT/Home Assistant publishing

### USER_CONFIRMATION

Requires explicit confirmation immediately before execution:
- any operation that changes vehicle state
- any diagnostic service routine
- any actuator request
- any write-capable UDS/CAN operation

### EXPERIMENTAL_WRITE

Disabled by default. Requires an explicit developer/experimental mode and a verified vehicle-specific implementation. Never generated from guessed CAN IDs.

## No hidden CAN injection

Automation must never turn a human-readable rule into an arbitrary CAN frame. Every write-capable action must map to a documented capability:

```text
Vehicle profile
  → capability ID
  → verified protocol implementation
  → safety policy
  → user confirmation
  → audit log
```

Unknown capability = unavailable.

## Physical-button extension

If WiCAN PRO's physical button is exposed through a supported interface, AutoDiag may treat it as an automation trigger:

```text
BUTTON SHORT
  → snapshot

BUTTON DOUBLE
  → start EV Health Test

BUTTON LONG
  → start/stop telemetry logging
```

Exact button event support depends on the WiCAN firmware/interface available at implementation time.

## Remote button

The Android app should provide a configurable "Quick Actions" screen designed as a software equivalent of a Sexy Button:

```text
┌──────────────────────────────┐
│        QUICK ACTIONS         │
│                              │
│   🔋 EV HEALTH TEST          │
│                              │
│   📊 LIVE BATTERY            │
│                              │
│   ⚡ CHARGING TEST            │
│                              │
│   📝 SNAPSHOT                │
│                              │
│   🏠 HOME ASSISTANT          │
└──────────────────────────────┘
```

Buttons are configurable per vehicle profile.

## Home Assistant

AutoDiag should support:

- MQTT telemetry
- MQTT events
- MQTT automation triggers
- Home Assistant discovery where practical
- Home Assistant notifications
- dashboard entities
- start/stop read-only diagnostics
- export/replay event notification

The application should not require Home Assistant for core operation.

## Remote monitoring

A vehicle can publish selected signals while the user is at home, subject to vehicle/WiCAN power and sleep behavior.

Examples:
- SOC
- charging state
- charging power
- battery temperature
- pack voltage/current
- selected diagnostic status
- WiCAN online/offline state

The app must distinguish live, cached and stale values.

## Offline / stale handling

Never display a cached value as live.

```text
🟢 LIVE       last sample < configured freshness window
🟡 STALE      cached value, vehicle not currently responding
⚪ UNKNOWN    no valid sample
```

## Automation history

Every rule execution creates an event:

```text
2026-08-28 21:14:03
Rule: ev_health_on_charge
Vehicle: Tesla Model Y
Trigger: charging.state = charging
Actions: test_started, mqtt_published
Result: success
```

This makes automation debuggable rather than a black box.

## Extension system

New actions and triggers are added through typed capability interfaces, not hardcoded UI branches.

```text
TriggerProvider
SignalProvider
ConditionEvaluator
ActionProvider
VehicleCapabilityProvider
AutomationStore
SafetyPolicy
AuditLogger
```

A new manufacturer can add a decoder/capability provider without rewriting the automation engine.

## Future extensions

- geofence triggers
- home/away state
- charging tariff triggers
- scheduled battery preconditioning logging
- automatic pre-purchase test workflow
- automatic report generation after charging
- Telegram/Android notification providers
- Home Assistant service calls
- Node-RED integration
- cloud sync (optional)
- multi-vehicle automation profiles

## Important distinction

WiCAN PRO's own web interface and firmware automation remain available. AutoDiag does not replace firmware functions unnecessarily. It adds a higher-level automation and diagnostics layer with a consistent Android UI, vehicle profiles, safety policy, evidence tracking and replay/history.
