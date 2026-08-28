# Capability Discovery

## Purpose

AutoDiag must discover what the connected vehicle/interface actually exposes before enabling diagnostic screens or tests. A Model Y badge is not enough to assume that a particular BMS signal, cell measurement, Riso value, ECU, or charging metric is available.

## Identity scope

Capabilities are cached against the most specific identity available:

1. VIN (when safely available)
2. vehicle make/model/model year/trim
3. battery/drive-unit variant when known
4. BMS/ECU software or firmware version
5. adapter/transport and protocol version

A cache entry must never be reused blindly across vehicles just because the visible model and year match.

## Capability states

Each capability uses:

- `available` — detected and verified for this configuration
- `partial` — only part of the requested data is exposed
- `unavailable` — tested and not available
- `unknown` — not tested or decoding is unresolved
- `error` — discovery was attempted but communication failed

Example:

```text
Battery
  Cell voltage             AVAILABLE
  Cell temperature         PARTIAL (voltage only)
  Module identity          AVAILABLE
  Pack current             AVAILABLE
  Pack voltage             AVAILABLE
  Riso numerical value     UNKNOWN
  Riso status              AVAILABLE
```

## Granularity

Discovery is hierarchical. It must not stop at `Battery = YES`.

Examples:

- pack voltage available
- module voltage available
- cell-group voltage available
- individual cell voltage unavailable
- cell temperature available only for selected modules

The UI must expose this distinction so an unsupported value is never replaced by a blank-looking zero or a guessed value.

## Discovery sequence

```text
Connect
  -> identify interface
  -> identify vehicle
  -> identify ECU/BMS capabilities
  -> probe only safe/read-only services
  -> validate response format
  -> classify capability
  -> cache result
```

Discovery is read-only by default. No ECU coding, actuator command, contactor command, reset, flash, or other write operation is part of capability discovery.

## Tesla-specific examples

The Tesla profile may discover, where supported and verified:

- vehicle identity and market-related information
- pack voltage/current/power
- SOC
- battery temperatures and deltas
- cell/brick voltage groups
- module/brick identifiers
- charge state and charging power
- AC/DC charging context
- drive-unit information
- DTC/alert information
- HV isolation/Riso status or numerical data if exposed

The application must show `NOT AVAILABLE` when a particular Tesla generation or firmware does not expose a value. It must not infer cell-level access merely from the vehicle model.

## Market / regional warning

Where a reliable source exposes market/region information, AutoDiag may show a warning such as:

> ⚠ US-market vehicle — some functions/specifications may differ from EU vehicles.

This is informational. The warning must be based on an identified source (VIN decoding, vehicle configuration, or verified ECU data), not inferred from language, charging connector alone, or user location.

## Evidence

Every discovered capability should retain:

```json
{
  "capability": "battery.cell_voltage",
  "state": "available",
  "source": "vehicle_response",
  "verification": "partially_verified",
  "vehicle_scope": "Tesla Model Y 2022 LR",
  "firmware_scope": "known version or null",
  "timestamp": "ISO8601"
}
```

## Failure handling

A failed probe is not the same as an unsupported capability. The engine distinguishes:

- no response
- malformed response
- unsupported service
- known response but unknown decoder
- verified absence

This prevents transient Wi-Fi/CAN problems from permanently caching a false `unavailable` result.

## Future extension

Capability discovery is the gatekeeper for the Automatic Health Check, battery replay UI, Riso module, charging analysis, and manufacturer-specific diagnostics.
