# Reference APK feature inventory

The supplied Android APKs are used as behavioral and data-model references. We do not copy their UI design or proprietary implementation.

## Torque reference

Observed data/profile concepts:

- Long name + short display name
- Mode/PID or ECU-specific request identifier
- ECU/CAN header / target address
- Formula/equation for byte decoding
- Minimum and maximum display/engineering range
- Units
- Boolean/bit-field decoding
- Derived values referencing other values
- Vehicle-specific profiles matched by ECU/software/workshop identifiers
- Standard OBD-II equivalent PID linking
- Custom PIDs
- Logging and sampling-rate concepts
- Adapter read-speed information
- Multiple ECU families and non-OBD diagnostic requests

The important architectural lesson is that acquisition address, byte extraction, scaling, engineering range, identity and derived calculations should be separate metadata, not embedded in the UI.

## PHEV Watchdog reference

Observed EV/PHEV telemetry concepts:

- SOC / SOH
- pack voltage/current/power
- cell minimum/maximum voltage and cell identifiers
- per-cell voltage history
- module temperature minimum/maximum and identifiers
- average/target cell voltage
- charge/discharge power limits
- battery cooling/fan state
- resistance and cell-difference metrics
- sampling and historical logging
- DTC history/database concepts
- multiple chart modes

These are candidates for our EV telemetry model, history store and graph engine. Vehicle-specific meanings must still carry provenance and verification scope.

## Car Scanner reference

Useful product-scope references include broad vehicle profiles, ECU diagnostics, live data, custom/extended data and logging. The project should reproduce capabilities where protocol evidence permits, but use its own modern UI and evidence model.

## Remote-control reference

The supplied remote-control application is relevant to the future control/service architecture only. It must not cause the read-only diagnostic transport to become write-capable implicitly.

## Implementation policy

1. Reuse protocol facts and data-model ideas where they are standard or independently verifiable.
2. Do not copy proprietary source code or application UI.
3. Do not blindly ship third-party APK asset databases into the public repository.
4. Prefer an importer/schema so compatible profile data can be loaded as user/project data.
5. Every vendor-specific signal must carry vehicle/ECU scope, raw representation and verification status.
6. Unknown scaling stays raw/unknown instead of being guessed.
7. Read-only diagnostics remains the priority path.
8. Experimental control features live under `docs/experimental/` and isolated control APIs.
