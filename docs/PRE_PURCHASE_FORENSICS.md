# PRE-PURCHASE FORENSICS

This document defines the read-only forensic stages of `PRE-PURCHASE TEST` for EVs. It deliberately separates **measured/ECU-reported facts** from interpretation. Vehicle-specific CAN IDs, UDS DIDs and thresholds are not considered valid merely because they appear in a draft implementation.

## 1. Pyrotechnic battery disconnect / pyrofuse

The test may report a pyrotechnic disconnect state **only when the supported vehicle exposes a documented or independently verified signal**.

Possible evidence:

- BMS/HV battery controller DTCs or status indicating a pyrotechnic disconnect event.
- Crash/airbag controller evidence of deployment, where exposed.
- HV contactor/interlock state and related fault records, where exposed.
- VIN/software identity and ECU replacement evidence, where exposed.

### Important distinction

A battery pyrofuse/PBDU is not the same component as an airbag squib. A generic `squib resistance` value must never be interpreted as the resistance of the HV pyrofuse unless the vehicle documentation explicitly defines that measurement.

The application therefore uses these states:

- `INTACT_REPORTED` — ECU explicitly reports intact/normal.
- `TRIPPED_REPORTED` — ECU explicitly reports tripped/deployed.
- `FAULT_REPORTED` — ECU reports a relevant fault.
- `NOT_AVAILABLE` — no supported signal.
- `UNVERIFIED` — a candidate signal exists but its meaning/scope is not verified.

### Emulator / replacement suspicion

Equal resistance readings alone are **not proof** of an emulator. If a supported ECU exposes multiple independent circuit measurements, AutoDiag may flag statistical anomalies as `SUSPICIOUS_CONFIGURATION`, but the UI must state that this is an indicator requiring physical inspection/service documentation.

AutoDiag must not instruct the user to probe, bridge, bypass or energize a pyrotechnic circuit.

## 2. HV isolation / Riso

Vehicle-reported isolation is recorded separately from a physical insulation-resistance measurement.

ISO 6469-3 defines minimum isolation-resistance ratios of 100 Ω/V for DC circuits and 500 Ω/V for AC circuits. For a 400 V DC circuit, 100 Ω/V corresponds to 40 kΩ; it is therefore incorrect to hard-code `200 kΩ` as a universal ISO minimum. The applicable vehicle architecture and protection method must be considered. See the official ISO 6469-3 reference in the project knowledge base.

The result model stores:

- raw ECU value;
- decoded unit;
- reported status;
- maximum working voltage when known;
- calculated Ω/V when valid;
- threshold source and verification scope;
- whether the value came from the vehicle or from a physical test.

No MΩ value is fabricated from a status-only response.

## 3. Crash / airbag forensic audit

The audit checks **available evidence**, not an assumed universal crash-history DID.

Potential evidence sources:

- active and stored DTCs;
- deployment status where exposed;
- pretensioner/airbag deployment evidence where exposed;
- crash-event records where the ECU documents such records;
- VIN/vehicle identity reported by ECU versus Gateway/central vehicle identity;
- BMS/HV fault history related to crash or pyrotechnic disconnect;
- historical alerts where the vehicle exposes them.

A clean current RCM readout does not prove that the vehicle has never been crashed. Conversely, one historical fault does not by itself establish the severity of an accident.

The verdict vocabulary is therefore:

- `NO_RELEVANT_EVIDENCE_FOUND`
- `CRASH_RELATED_EVIDENCE_FOUND`
- `ECU_IDENTITY_MISMATCH`
- `INSUFFICIENT_COVERAGE`
- `UNVERIFIED`

## 4. Odometer cross-check

Mileage is compared only between ECUs that actually expose a documented/verified mileage value. A difference is reported as an observation first. `TAMPERING_CONFIRMED` is never produced from latency or a simple mismatch alone.

The application may separately report:

- `MILEAGE_CONSISTENT`
- `MILEAGE_DISCREPANCY`
- `MILEAGE_NOT_AVAILABLE`
- `MILEAGE_UNVERIFIED`

## 5. Master PRE-PURCHASE pipeline

The read-only sequence is:

```text
Capability Discovery
  -> Vehicle/ECU identity
  -> DTC + alert inventory
  -> Odometer cross-check
  -> Crash/airbag evidence
  -> HV battery / pyrotechnic disconnect evidence
  -> HV isolation / Riso
  -> Battery health snapshot
  -> Thermal / drive-unit checks
  -> Coverage + confidence
  -> Assessment
  -> PDF/report + evidence manifest
```

A critical safety finding may produce `REVIEW` or `CRITICAL_REVIEW`, but a missing signal produces `NOT_AVAILABLE`, not `PASS` or `FAIL`.

## 6. Evidence manifest

Every forensic finding stores:

- `finding_id`
- ECU/address and protocol when known
- raw response/capture reference
- decoded value and unit
- source/provenance
- vehicle scope
- verification state
- timestamp
- deterministic rule identifier
- human-readable Czech explanation

This allows the report to distinguish **what the car actually reported** from what AutoDiag inferred.
