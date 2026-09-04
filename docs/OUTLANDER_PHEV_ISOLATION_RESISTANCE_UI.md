# Outlander PHEV — HV Isolation Resistance diagnostic UI

## Priority

HV isolation resistance is a first-class diagnostic domain. It must not be shown as a generic battery number.

## Meaning

`ISOLATION_RESISTANCE` in the reverse-engineered PHEV Watchdog source represents the measured electrical isolation of the high-voltage system. The extracted decoder exposes the value in kOhm.

The application must explain that this is different from internal cell resistance and different from cell-voltage delta.

## UI hierarchy

### HV system → Isolation resistance

Show:

- Current isolation resistance: value + kOhm
- Measurement timestamp
- Source ECU/module when known from verified vehicle evidence
- Verification state: Unverified / Partially verified / Verified
- Minimum observed value during the current capture session
- Maximum observed value during the current capture session
- Number of samples
- Trend chart over time
- Raw request/response available in Expert mode

### Interpretation

Display three separate concepts:

1. **Current value** — latest decoded measurement.
2. **Observed min/max** — statistical bounds from the current capture session, not manufacturer limits.
3. **Manufacturer/service limit** — only shown when supported by authoritative evidence for the exact vehicle configuration. Never derive a safety limit from the decoder's numeric range.

If no authoritative limit is available, show `Limit: unknown` rather than inventing one.

## What is being measured

The UI should describe the measurement as isolation between the high-voltage electrical system and the vehicle's reference/chassis side. Do not imply a specific physical test topology, positive-to-chassis versus negative-to-chassis calculation, or a particular insulation-monitoring component until this is established by vehicle/service documentation or measured evidence.

## ECU/module attribution

The screen must distinguish:

- vehicle subsystem: HV battery / high-voltage system
- reporting ECU/module: exact identity only when observed or independently documented
- signal name: `ISOLATION_RESISTANCE`
- protocol/request: source-derived candidate only until real vehicle capture validates it

The application must not label the reporting module as BMU/BMS/EV-ECU merely because another battery signal comes from a related decoder.

## Related values

Show isolation resistance alongside, but independently from:

- pack voltage
- battery current
- maximum/minimum cell voltage
- cell-voltage delta
- maximum/minimum module temperature
- internal resistance
- maximum/minimum internal resistance
- battery cooling-fan PWM

A failure or missing value in one category must not automatically mark the others unavailable.

## Safety and interpretation rules

- Low isolation resistance can be safety-relevant; the app must not present an unverified threshold as a pass/fail rule.
- Do not instruct the user to touch HV components or perform live electrical measurements on the vehicle.
- The diagnostic app is an observation/diagnostic tool; physical HV insulation testing belongs to appropriate service procedures and qualified personnel.
- `UNKNOWN` is a valid state.
- A decoded value from a static APK source remains `UNVERIFIED` until the request/response is observed on the scoped vehicle and the decoder is validated.

## Capture model

Each accepted sample should retain:

- vehicle identity (privacy-aware VIN handling)
- ECU identity
- signal ID
- raw request
- raw response
- decoded value
- unit
- timestamp
- adapter/source
- verification state

This supports later replay and independent decoder validation.

## Source-derived decoder currently known

The PHEV Watchdog `z3/a` decoder contains an `ISOLATION_RESISTANCE` field decoded from two response bytes as an unsigned 16-bit value with kOhm interpretation. This is a source extraction, not yet a vehicle-verified mapping.

A separate Watchdog battery decoder family also exposes internal resistance fields. These must remain separate from `ISOLATION_RESISTANCE` in the application data model.

## Future enhancement

When exact vehicle/service evidence is available, add:

- authoritative normal range
- warning threshold
- critical threshold
- diagnostic interpretation
- DTC cross-reference
- measurement conditions
- whether the reported value is a positive-to-chassis, negative-to-chassis, equivalent, or calculated isolation value

Until then the UI should be explicit about what is known, what is measured, and what remains unknown.
