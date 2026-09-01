# AUTO TEST — specification

`AUTO TEST` is the one-tap orchestration layer for supported vehicles. It does not invent unavailable measurements. Capability Discovery determines which phases and metrics can actually run.

## Modes

### Quick
For a short vehicle loan or first inspection. Runs only immediately available read-only measurements and clearly reports `LIMITED ASSESSMENT` when no baseline/history exists.

### Standard
Adds rest baseline, drive/load observation, recovery and available charging telemetry.

### Full
Runs the complete supported sequence, including AC/DC charging when the user can safely provide those conditions, detailed cell/module logging and longitudinal fingerprint comparison.

### PRE-PURCHASE TEST
A presentation-oriented profile combining the safest useful checks before buying a used EV. It must show missing capabilities, confidence and evidence instead of turning missing data into a pass/fail result.

## Session sequence

```text
CONNECT
  ↓
CAPABILITY DISCOVERY
  ↓
VEHICLE ID / MARKET / BATTERY PROFILE
  ↓
DATA-QUALITY GATE
  ↓
REST BASELINE
  ↓
DRIVE / LOAD
  ↓
RECOVERY
  ↓
OPTIONAL AC CHARGE
  ↓
OPTIONAL DC FAST CHARGE
  ↓
HV ISOLATION / RISO
  ↓
DRIVE UNIT / THERMAL CHECKS
  ↓
ANALYSIS
  ↓
REPORT + REPLAY
```

Stages are skipped as `NOT_AVAILABLE` when the vehicle or current connection does not expose the required data. A skipped stage is not a failure.

## Battery load analysis

Every sample stores at least:

- timestamp
- phase
- pack voltage
- battery current
- power when available
- SOC
- battery temperature
- module/brick identifiers when available
- cell identifiers and voltage when available

The engine records peak response and recovery. A cell with the lowest instantaneous voltage during high current is **not automatically called the weakest cell**. The result must consider current, temperature, SOC, baseline, neighboring cells/modules and recovery.

`mV/100A` is an analytical comparison metric. It is not a universal fault threshold.

## Charging analysis

AC and DC charging are separate phases. If cell-level data exists, the app records the complete cell population over time, allowing the user to inspect:

- absolute cell voltage,
- deviation from the pack population,
- module/brick grouping,
- temperature where exposed,
- current and pack voltage,
- behavior as SOC rises.

The replay view must allow selection of a cell and scrubbing through its historical samples.

## Riso / HV isolation

The test records vehicle-reported isolation values/status when available and preserves their provenance. A raw diagnostic value that has not been decoded is not converted into MΩ by the application.

## Result model

The result is composed of:

```text
OBSERVATIONS
  + EVIDENCE
  + DATA QUALITY
  + CONFIDENCE
  + TRENDS
  = ASSESSMENT
```

The app should prefer wording such as `persistent deviation observed` or `limited assessment` over unsupported claims such as `bad cell` or an invented SOH percentage.

## Driver view

The first result screen should be readable in seconds:

```text
EV HEALTH TEST

Battery          ● GOOD / ● REVIEW / ? LIMITED
Cell balance     ●
Thermal          ●
HV isolation     ●
Charging         ● / NOT TESTED
Drive unit       ●

Confidence       82%

[ View details ]   [ Replay ]
```

The confidence value represents data completeness/evidence quality, not a guaranteed probability that the vehicle is healthy.

## Expert view

```text
PACK
 ├─ Module 01
 │   ├─ Cell 001   4.118 V   +4 mV
 │   ├─ Cell 002   4.114 V    0 mV
 │   └─ ...
 ├─ Module 02
 └─ ...

TIME  00:07:13.420
CURRENT  286 A
PACK     392.4 V
TEMP     24.1 °C
PHASE    LOAD
```

Selecting a cell updates the number, chart and topology highlight at the exact replay timestamp.

## Replay

Replay is deterministic. The timeline is indexed by timestamp so moving the scrubber does not require scanning the entire log. The selected time drives all synchronized panels.

A user can therefore answer questions such as: "What voltage did cell 137 have at the exact moment the car accelerated?" or "How did this cell behave during DC charging?"

## Automation safety

`AUTO TEST` is read-only. Notifications and MQTT publication are separate actions. WRITE/COMMAND functionality is never implicitly enabled by this test.

## Related design

Adaptive sampling rates (SLOW/MEDIUM/FAST/BURST), REST→LOAD→RECOVERY state machine, effectiveHz UI indicator, and bus-error handling that keeps `NOT_AVAILABLE reason: bus_error` separate from `vehicle_did_not_provide`:

- [`docs/ADAPTIVE_SAMPLING_AND_BUS_HEALTH.md`](ADAPTIVE_SAMPLING_AND_BUS_HEALTH.md)
- [`docs/DIAGNOSTIC_BUS_HEALTH.md`](DIAGNOSTIC_BUS_HEALTH.md)
