# EV Acceleration Battery Analysis

## Purpose

AutoDiag should support a structured, read-only battery observation test for electric vehicles. The goal is not simply to display SOC, but to observe how the high-voltage battery behaves under a defined load event such as acceleration.

The feature must work only with signals that are actually available and verified for the target vehicle.

## Measurements

Where the vehicle exposes the required signals, capture synchronously:

- battery pack current
- battery pack voltage
- instantaneous battery power (calculated when valid)
- SOC
- battery pack temperature
- minimum/maximum battery temperature
- temperature spread
- cell/module voltage information where exposed
- minimum cell voltage
- maximum cell voltage
- cell voltage spread
- module-level values where exposed
- vehicle speed
- accelerator/throttle request where available
- timestamp
- CAN frame timing/data quality

For each measurement AutoDiag should retain the original raw value and metadata needed to reproduce the displayed result.

## Test model

The automated test should consist of a configurable observation window rather than an application-controlled driving maneuver.

Example phases:

1. PRE-CONDITION — capture a stable baseline.
2. LOAD EVENT — record the naturally occurring acceleration/load event.
3. RECOVERY — continue recording after the event.
4. ANALYSIS — calculate voltage/current/power and battery deltas.

The application must not command acceleration or otherwise control the vehicle as part of this test.

## Battery visualization

AutoDiag should offer two levels of presentation.

### Simple view

A user-friendly result using words and visual status indicators:

- Battery: GOOD / ATTENTION / WARNING / UNKNOWN
- Voltage sag: normal / elevated / unknown
- Cell balance: good / attention / warning / unavailable
- Temperature: normal / elevated / warning
- Data quality: high / medium / low

A battery graphic should show individual cells or modules with a normalized status indication when sufficiently detailed data is available.

### Expert view

An interactive battery representation should allow the user to inspect:

- every available cell/module
- voltage
- temperature
- minimum/maximum values
- delta from pack/module average
- timestamp
- values during the load event
- values before and after the event

The expert view must distinguish measured data from calculated data.

## Log and replay

Every completed test should be stored as a replayable recording.

The recording should contain:

- metadata about vehicle/profile
- capture timestamp
- sampling/transport information
- raw frames when permitted
- decoded signals
- verification status of each signal
- test phase markers
- analysis results

Replay should allow the user to:

- play/pause
- seek through the recording
- change playback speed
- inspect the battery visualization at a selected timestamp
- inspect numeric values
- switch between simple and expert views
- compare baseline/load/recovery
- export the recording/report

The replay engine should use deterministic recorded data so that decoder changes can be regression-tested against historical captures.

## Graphs

Expert analysis should provide synchronized time-series views for:

- pack voltage
- pack current
- calculated power
- SOC
- battery temperature
- cell/module minimum
- cell/module maximum
- cell/module spread
- vehicle speed

Selecting a point in one graph should move the replay cursor in all graphs and the battery visualization.

## Data-quality requirements

A result must include data-quality information. For example:

- missing frames
- irregular sample intervals
- stale signals
- unsupported signals
- insufficient capture duration
- decoder confidence

If cell-level data is unavailable, AutoDiag must say `CELL DATA UNAVAILABLE` rather than estimating individual cell values from pack voltage.

## Vehicle independence

This feature is not Tesla-only. The diagnostic engine should define a common EV battery data model while vehicle profiles map manufacturer-specific signals into that model.

Example normalized model:

```text
EVBatterySnapshot
  packVoltage
  packCurrent
  packPower
  soc
  temperatures[]
  modules[]
  cells[]
  timestamp
  quality
```

A Tesla profile, Hyundai/Kia profile, VW/VAG profile, etc. may expose different subsets of this model.

## Safety

This is intended as passive/read-only observation. The application must not send vehicle-control commands to initiate the load event. Any future active test requiring vehicle control must be a separate explicitly designed feature with appropriate safety controls.

## Verification

Battery conclusions must not be hard-coded from generic thresholds without considering vehicle chemistry, pack design, temperature, SOC, firmware and measurement conditions. Thresholds should be profile-specific and documented.
