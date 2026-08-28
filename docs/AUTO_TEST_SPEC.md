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
A presentation-oriented profile combining the safest useful checks before buying a used EV. It includes the forensic stages below when the vehicle exposes the required evidence:

1. ECU/Gateway identity and VIN consistency
2. DTC + alert inventory
3. odometer cross-check from actually available sources
4. crash/airbag evidence audit
5. HV battery / pyrotechnic disconnect evidence
6. HV isolation / Riso
7. battery health snapshot and available cell/module analysis
8. thermal / drive-unit checks
9. coverage, confidence and evidence manifest

A missing forensic signal is `NOT_AVAILABLE`; it is never silently converted to `PASS`.

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
ECU / GATEWAY IDENTITY
  ↓
DTC + ALERT INVENTORY
  ↓
ODOMETER CROSS-CHECK
  ↓
CRASH / AIRBAG EVIDENCE
  ↓
HV PYROTECHNIC DISCONNECT EVIDENCE
  ↓
HV ISOLATION / RISO
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
DRIVE UNIT / THERMAL CHECKS
  ↓
ANALYSIS
  ↓
REPORT + REPLAY + EVIDENCE MANIFEST
```

Stages are skipped as `NOT_AVAILABLE` when the vehicle or current connection does not expose the required data. A skipped stage is not a failure.

## Forensic interpretation rules

### Pyrotechnic battery disconnect

AutoDiag may report a pyrotechnic battery disconnect state only when the supported vehicle exposes a documented or independently verified signal. A battery pyrofuse/PBDU is not treated as an airbag squib. A generic squib-resistance measurement must not be presented as pyrofuse resistance without vehicle-specific evidence.

Resistance similarity across multiple airbag circuits is, at most, an indicator requiring further inspection; it is not by itself proof of an emulator or crash repair.

### HV isolation / Riso

Vehicle-reported isolation and physical insulation testing are separate evidence types. The application stores the raw value, decoded unit, status, voltage context, threshold provenance and verification scope. It never fabricates an MΩ value from a status-only response.

ISO 6469-3 defines minimum isolation-resistance ratios of 100 Ω/V for DC circuits and 500 Ω/V for AC circuits. A universal `200 kΩ` minimum must therefore not be hard-coded as an ISO rule. Vehicle architecture and applicable protection provisions determine the correct interpretation.

### Crash / airbag evidence

A clean current airbag-controller result does not prove that a vehicle has never been crashed. AutoDiag reports available evidence such as deployment status, stored DTCs, crash records where exposed, ECU identity mismatch and relevant BMS/HV evidence. The result uses `CRASH_RELATED_EVIDENCE_FOUND` or `INSUFFICIENT_COVERAGE` instead of claiming a complete accident history when the vehicle does not expose one.

### Odometer

Mileage is compared only between ECUs that actually provide a documented/verified value. A discrepancy is an observation. CAN latency alone is not evidence of odometer tampering.

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
Crash evidence   ● / ? NOT AVAILABLE
HV disconnect    ● / ! REVIEW / ?
HV isolation     ●
Cell balance     ●
Thermal          ●
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

## Tooltip requirement

Every evaluated metric and every non-obvious status has a `?` info tooltip. Tooltip text is centralized in the knowledge base and is written in Czech. It explains what is measured, why it matters, data source, interpretation limits and verification state. Technical/internal error codes remain in logs, while the user-facing error message is Czech.

See `docs/UI_TOOLTIP_CZECH.md`.

## Replay

Replay is deterministic. The timeline is indexed by timestamp so moving the scrubber does not require scanning the entire log. The selected time drives all synchronized panels.

A user can therefore answer questions such as: "What voltage did cell 137 have at the exact moment the car accelerated?" or "How did this cell behave during DC charging?"

## Automation safety

`AUTO TEST` is read-only. Notifications and MQTT publication are separate actions. WRITE/COMMAND functionality is never implicitly enabled by this test.
