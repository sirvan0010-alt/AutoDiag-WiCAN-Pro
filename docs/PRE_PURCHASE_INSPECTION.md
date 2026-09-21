# Pre-Purchase Inspection Architecture

The PRE-PURCHASE TEST is a read-only, evidence-first workflow. It is designed for a buyer who may have only 30–90 minutes with a vehicle. Every stage is capability-gated and every result retains provenance.

## Test modes

- **Quick** — communication, identity, DTC/alerts, basic battery/thermal snapshot.
- **Standard** — Quick + repeated baseline + battery/load/recovery + charging/thermal observations where available.
- **Full** — Standard + extended replay capture, cross-ECU consistency checks and report generation.
- **Short-loan** — same tests as available, but confidence is explicitly limited because there is little historical data.

## Universal workflow

```text
Safety/data-quality gate
  -> Capability Discovery
  -> VIN / vehicle identity
  -> Market hint
  -> ECU topology
  -> DTC / alerts + freeze-frame
  -> odometer cross-check
  -> battery / engine health
  -> thermal / charging
  -> bus health
  -> trend/fingerprint comparison if history exists
  -> confidence analysis
  -> PDF report + replay
```

Unsupported stages are `NOT_AVAILABLE`, never `PASS`.

## Tesla / EV

Where the vehicle exposes the required data, the assistant can collect:

- pack voltage/current/SOC
- cell/brick/module voltage and delta
- temperature distribution and thermal history if exposed
- vehicle-reported SOH, clearly marked as `REPORTED`
- battery load response and recovery
- AC/DC charging context and charge-curve observations
- HV isolation/Riso status/value when exposed
- drive-unit and inverter observations when verified
- DTCs/alerts with source links
- 12 V / low-voltage system observations where exposed
- charging anomalies and thermal events

The app must not convert a cell-voltage observation into a capacity diagnosis or invent a numeric Riso value.

## VAG MEB / other EV platforms

The architecture supports OEM-specific modules for VW/Škoda/SEAT/Audi, Hyundai/Kia and other EVs. ECU addresses, data identifiers and thresholds are profile data and require evidence; they are not hardcoded assumptions from a generic vehicle list.

Candidate checks include gateway/ECU inventory, odometer consistency, BMS-reported health, cell delta, thermal data, charging behavior, HV isolation and crash/pyrofuse information when legitimately exposed.

## ICE / hybrid

Candidate checks include:

- odometer cross-check across available ECUs
- DTC + freeze-frame + readiness
- DPF/OPF soot/ash and differential pressure where exposed
- regeneration history
- SCR/AdBlue/NOx status
- injector correction / smooth-running values
- per-cylinder misfire counters
- requested vs actual boost
- requested vs actual rail pressure
- coolant/oil/intake temperature context
- hybrid battery data where exposed

A missing PID is not evidence of a fault.

## Anti-tamper / mileage consistency

AutoDiag may report mismatches between independent odometer values. It must **not** label a vehicle as having a mileage stopper or CAN filter solely from latency, jitter or a missing response. Such signals can have many benign causes.

The correct UI is:

```text
Mileage consistency
  ECU A       142,381 km
  ECU B       142,394 km
  ECU C       unavailable

⚠ Mismatch observed: 13 km
Confidence: medium
Meaning: values differ; cause not established.
```

A hardware CAN-filter suspicion requires multiple independent observations and remains a lead for physical inspection, not a diagnosis.

## Report

The report includes:

- vehicle identity and market evidence
- adapter/transport and security state
- capabilities actually observed
- DTC/alert list and source links
- battery/engine measurements with context
- unavailable/unknown data explicitly listed
- timeline and event markers
- confidence and verification levels
- SHA-256 digest of the raw inspection dataset
- QR payload containing the report/data digest and report identifier

The hash proves integrity of the referenced dataset; it does not prove that the vehicle data itself were truthful or that a repair diagnosis is correct.
