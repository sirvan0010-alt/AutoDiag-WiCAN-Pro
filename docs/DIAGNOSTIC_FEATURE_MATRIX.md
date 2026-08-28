# AutoDiag Diagnostic Feature Matrix

This is the product capability target. A feature is only promoted to `verified` when its decoder, evidence and real-vehicle/replay validation satisfy the project quality gates.

## EV / PHEV battery

- Pack voltage/current/SOC when exposed
- Minimum/maximum cell or brick voltage
- Cell delta and per-cell identity
- Cell voltage timeline during REST, LOAD, RECOVERY, AC and DC charging
- Pack/module/brick/cell hierarchy
- Battery temperature and temperature spread when exposed
- Load response and recovery curves
- Current-normalized analytical metrics such as mV/100A without universal thresholds
- Battery Fingerprint and persistent-deviation tracking
- Vehicle-reported SOH when available
- Clear separation between vehicle-reported SOH and AutoDiag observations
- AC/DC charging context and energy data when exposed
- HV isolation/Riso: numeric, status-only or raw-undecoded modes
- Contactor/HV status where verified
- 12 V battery voltage and charging behavior where exposed

## ICE / hybrid

- Generic OBD-II DTC and freeze-frame data
- I/M readiness monitors
- Odometer cross-check across available ECUs
- DPF soot/ash, differential pressure and regeneration data where exposed
- AdBlue/SCR/NOx diagnostics where exposed
- Misfire counters per cylinder where exposed
- Injector correction/smooth-running data where exposed
- Requested vs actual boost pressure
- Requested vs actual common-rail pressure
- Coolant, intake, fuel and exhaust temperature context where exposed
- Live data recording and replay

These are capability targets, not claims that every vehicle exposes every item.

## Bus health / auto-electrician view

Where the transport and vehicle interface provide sufficient information:

- CAN bus load
- frame rate
- error-frame observations
- bus-off / error-passive state when accessible
- timestamped frame loss/drop counters
- bitrate/configuration evidence
- raw CAN capture
- K-Line traffic capture where supported
- protocol/session diagnostics

The UI must label whether a value is measured by the interface, reported by the vehicle, or inferred by AutoDiag.

## OBD / UDS architecture

The diagnostic stack is layered:

```text
Transport
  ↓
CAN / K-Line framing
  ↓
ISO-TP where required
  ↓
OBD-II / UDS transport services
  ↓
OEM decoder
  ↓
Evidence + knowledge base
```

Read-only services are the initial scope. SecurityAccess, RoutineControl, TransferData, coding, flashing and actuator control remain isolated experimental/write functionality and cannot be enabled by a normal read-only profile.

## Pre-Purchase Test

The one-tap test should orchestrate only capabilities that the vehicle exposes:

1. Identity and market detection
2. Capability Discovery
3. DTC scan and freeze-frame collection
4. 12 V system check where available
5. EV battery REST baseline
6. controlled load observation when safe and data is available
7. recovery observation
8. AC/DC charging analysis when the vehicle is actually charging
9. cell/module tracking
10. HV isolation/Riso data collection
11. odometer cross-check where supported
12. ICE-specific DPF/SCR/misfire/pressure checks where supported
13. anomaly explanation
14. confidence and data-quality report
15. replayable evidence package

Unsupported stages are shown as `NOT_AVAILABLE`; they are not failures.

## Report philosophy

The final report separates:

- **Vehicle-reported facts**
- **Measured interface data**
- **Verified decoded values**
- **Evidence-backed analysis**
- **Unresolved observations**
- **Missing/unsupported data**

The report must never turn a missing value into a guessed value merely to make the report look complete.
