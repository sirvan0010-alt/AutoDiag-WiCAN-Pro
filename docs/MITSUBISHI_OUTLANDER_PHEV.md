# Mitsubishi Outlander PHEV — diagnostic scope

## Active vehicle scope

This is the first manufacturer-specific implementation target for AutoDiag. Other vehicle manufacturers/models are intentionally out of scope for this work item.

Target:

- Manufacturer: Mitsubishi
- Model: Outlander PHEV
- Model year: unresolved until vehicle evidence is available
- Generation: unresolved until vehicle evidence is available
- Region: unresolved until VIN/configuration evidence is available

## Signal-map policy

The profile starts as an evidence-backed schema rather than a guessed CAN database. Request IDs, CAN IDs, UDS DIDs, PID values, byte layouts and scaling are not added unless supported by a source and/or measured vehicle evidence.

The current PHEV Watchdog analysis provides a capability inventory including battery SOC/SOH/capacity, pack voltage/current/power, cell-voltage information, module temperatures, cooling-fan control data, internal resistance/degradation information and trip/energy data. These entries remain `unknown` until the actual request/response mapping is established and validated.

## Capability granularity

Battery discovery must keep these dimensions separate:

- pack voltage/current/power
- cell voltage
- minimum/maximum cell voltage and cell identity
- cell-voltage delta
- module temperature
- cooling/fan information
- internal resistance
- degradation/cycle information
- charging/target-voltage information
- trip/energy information

A successful pack-level read must never imply that cell-level or module-level values are available.

## Verification levels

- `unverified`: source/schema evidence only
- `partially_verified`: request/response or profile evidence exists but vehicle validation is incomplete
- `verified`: response observed on the scoped Outlander configuration and decoder validated

## Runtime gate

The Outlander profile is selected only after vehicle identity matching. Vehicle year/region/generation must not be guessed from the model name alone. Capability Discovery remains read-only and failed communication must not be cached as `unavailable`.

## Next evidence required

1. Extract concrete request/response candidates from the available PHEV Watchdog APK analysis.
2. Record source/provenance for every candidate signal.
3. Build replay fixtures from observed frames/responses.
4. Validate decoding against a real Outlander PHEV.
5. Promote individual capabilities independently; do not promote the whole battery domain at once.
