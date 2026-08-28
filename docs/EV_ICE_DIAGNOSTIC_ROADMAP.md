# EV / ICE Diagnostic Roadmap

This roadmap expands AutoDiag beyond a generic OBD reader while preserving the read-first architecture.

## EV / PHEV

### Battery
- vehicle-reported SOH where available
- cell/brick voltage and delta
- cell timeline during charging and load
- temperature distribution
- load/recovery characterization
- Battery Fingerprint
- charging energy/history when exposed

### HV safety
- vehicle-reported isolation resistance
- positive/negative/overall isolation where exposed
- status-only isolation mode
- raw undecoded isolation data storage
- isolation trend
- separate physical insulation-test record

### Charging
- AC/DC session identification
- charge current and power
- battery temperature context
- cell tracking throughout the charge
- charge curve replay
- event markers

## ICE / Hybrid

### DTC and readiness
- OBD-II DTCs
- freeze-frame snapshots
- I/M readiness
- MIL/status context

### Powertrain health
- misfire counters
- injector correction / smooth running
- boost requested vs actual
- common-rail pressure requested vs actual
- intake/exhaust/coolant temperature context

### Emissions
- DPF/OPF soot and ash values when exposed
- differential pressure
- last regeneration distance/time
- SCR/AdBlue status
- NOx sensor values/status

### Odometer audit

Read available odometer/runtime values from independent ECUs and present discrepancies transparently:

```text
Instrument cluster      184 210 km
Engine ECU               184 214 km
ABS                      184 211 km
TCU                      NOT_AVAILABLE

Observation: 4 km spread
```

A discrepancy is an observation, not automatic proof of odometer fraud.

## Protocol roadmap

- generic OBD-II
- ISO-TP
- read-only UDS services
- OEM diagnostic profiles
- security-gateway awareness
- J2534/PassThru as a future adapter capability, not an assumed WiCAN feature

Write/control services remain isolated from the read-only diagnostic engine.

## Product modes

### Driver
Simple result, limited terminology, clear explanations.

### Technician
Live values, DTC details, freeze frames, bus health and test controls that are read-only.

### Expert
Raw frames, timestamps, decoder provenance, replay, cell-level inspection and evidence records.

### Pre-Purchase
One-tap profile combining all supported read-only checks and generating a shareable evidence report.
