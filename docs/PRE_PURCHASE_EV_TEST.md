# AutoDiag EV Pre-Purchase Test

## Purpose

A repeatable inspection workflow for a used EV before purchase. The test combines vehicle identification, diagnostic faults, battery telemetry, charging behavior, thermal data, HV isolation data, drive-unit observations and a transparent confidence assessment.

This is an inspection aid, not a guarantee of vehicle condition and not a substitute for an OEM inspection.

## Test modes

### Quick — short loan / approximately 15–30 minutes
- Identify vehicle and market where possible
- Read available DTCs/alerts
- Record SOC, pack voltage, current and temperatures
- Check available cell/module spread
- Check available HV isolation status/value
- Short controlled load observation when safe
- Record recovery after load
- Capture a complete timestamped log
- Produce a limited-confidence report

### Standard — approximately 30–90 minutes
Everything in Quick plus:
- repeated load samples
- thermal gradient
- cell/module tracking
- charging observation if practical
- AC charging observation where available
- historical comparison if previous AutoDiag tests exist
- trend and anomaly analysis

### Full — extended ownership/inspection session
Everything in Standard plus:
- DC fast charging observation where practical
- charging curve
- detailed cell/module replay
- longer thermal observation
- Battery Fingerprint comparison
- repeated tests under comparable conditions
- comprehensive DTC and diagnostic knowledge-base report

## Battery load test

When vehicle data exposes the required signals, log synchronously:

- timestamp
- SOC
- pack voltage
- battery current
- calculated electrical power
- battery temperature(s)
- cell/module voltages
- min/max voltage and spread
- selected cell/module identity
- drive/load phase

The report must distinguish:

- absolute minimum cell voltage
- maximum deviation from pack/module peers
- voltage spread
- load response
- recovery response

A cell with the lowest instantaneous voltage during acceleration is **not automatically identified as the weakest cell**.

## Charging test

AC and DC charging are separate test modes. When cell-level data is available, record every exposed cell/module over time, including:

- absolute voltage
- deviation from peers
- minimum/maximum
- spread
- temperature
- charging current/power
- SOC
- time

The report should identify cells/modules whose deviation repeatedly appears during charging, especially at comparable SOC and temperature, without declaring a component defective from one observation.

## Thermal test

Record battery and available module temperatures throughout the test. Calculate only metrics supported by actual signals, such as:

- minimum temperature
- maximum temperature
- temperature spread
- rate of change
- temperature versus current/power

No generic temperature fault threshold is hardcoded without evidence.

## HV isolation / Riso

Record vehicle-reported isolation information when available:

- positive-to-chassis resistance
- negative-to-chassis resistance
- overall isolation
- vehicle-reported status
- raw diagnostic value when decoding is not yet verified

If only a status is available, show the status only. Never invent an MΩ value.

## DTC and alerts

The report separates:

- OEM DTC
- vehicle alert
- generic OBD-II code
- raw diagnostic response
- AutoDiag observation
- AutoDiag inference

Every known code can open the Diagnostic Knowledge Base. Where an independently verified OEM source exists, provide a direct link to the relevant explanation/troubleshooting/service procedure. Community information remains visibly separate.

## Vehicle identification

Build the strongest possible profile:

```text
Make → Model → Model year → Trim → Drive unit
→ Battery variant → Supplier → Chemistry → Market
→ Diagnostic generation / firmware where verifiable
```

For a confirmed US-market Tesla, show a visible `⚠️ US-market vehicle` indicator. Do not infer market from phone locale, GPS or IP address.

## Buyer report

The final report should have two layers.

### Simple view

```text
PRE-PURCHASE EV TEST

Vehicle        Tesla Model Y
Battery        🟢 data consistent with observed baseline
Cell balance   🟡 limited evidence
Thermal        🟢 no observed anomaly
HV isolation   🟢 vehicle reports normal
DTCs           🟡 2 codes found
Charging       ⚪ not tested
Confidence     72% / LIMITED

⚠️ Further inspection recommended
```

The wording must describe evidence, not claim a guaranteed battery SOH percentage or a component failure without sufficient evidence.

### Expert view

Provide:
- synchronized charts
- complete numerical samples
- cell/module heat map
- pack visualization where topology is known
- current/voltage/power curves
- temperature curves
- load/recovery curve
- charging curve
- DTC details
- Riso history
- raw CAN/diagnostic log where permitted
- replay controls

## Replay

Every completed test should be replayable. The user can scrub through time and see the battery state at that exact moment.

The replay should support:

```text
Simple:
🟢 normal   🟡 observation   🔴 attention   ⚪ unavailable

Expert:
Cell 001  3.812 V   +4 mV
Cell 002  3.808 V   0 mV
...
Cell 137  3.779 V  -29 mV
```

The heat-map status must be driven by evidence-backed rules. If no rule is resolved, display the value without a diagnostic color classification.

## Confidence

Confidence describes the quality and completeness of the evidence, not the probability that the car is healthy.

Examples of confidence reduction:
- short test
- unknown battery variant
- missing cell data
- unknown market
- unknown decoder generation
- no baseline history
- generic fallback profile
- inconsistent signal quality

A borrowed Tesla tested for one hour can still receive a useful report, but it must explicitly say that long-term Battery Fingerprint/trend evidence is unavailable.

## Purchase recommendation language

Allowed:

- `No anomaly observed in available data`
- `Observation requires further inspection`
- `Persistent deviation detected across repeated samples`
- `OEM diagnostic procedure available`
- `Assessment limited by missing vehicle data`

Not allowed without strong evidence:

- `Battery is definitely healthy`
- `Battery has X% SOH`
- `Cell 137 is definitely bad`
- `Buy / do not buy`

## Safety

No automated driving, charging, contactor or HV write command is part of this test. Any future write/action capability is isolated behind a separate experimental action layer and requires explicit confirmation.
