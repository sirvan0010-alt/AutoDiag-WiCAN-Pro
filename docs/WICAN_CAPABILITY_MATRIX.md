# WiCAN Capability Matrix

This document defines how AutoDiag describes WiCAN PRO capabilities without freezing firmware behavior into assumptions.

## Core rule

WiCAN PRO is the transport/interface layer. AutoDiag must discover and verify what is actually available instead of treating a hardware feature as proof that a vehicle, firmware build, or connection exposes that feature.

Capability states:

- `SUPPORTED` — documented and available through the detected interface/firmware scope.
- `SUPPORTED_BUT_NOT_VERIFIED` — the interface advertises/supports it, but AutoDiag has not verified the current path.
- `VEHICLE_DEPENDENT` — transport exists, but vehicle/ECU configuration determines availability.
- `FIRMWARE_DEPENDENT` — availability depends on firmware version/configuration.
- `NOT_SUPPORTED` — known not to be available on the detected configuration.
- `UNKNOWN` — insufficient evidence; never silently converted to unavailable.
- `ERROR` — discovery was attempted but failed for a technical reason.

## Interface capability families

| Capability | AutoDiag interpretation |
|---|---|
| Classical CAN 2.0A/B | Transport capability; verify active channel/bitrate before decoding |
| HS-CAN | Supported interface family where exposed by WiCAN hardware/firmware |
| MS-CAN | Supported interface family on compatible hardware/firmware; vehicle-dependent |
| GM HS-CAN | Supported interface family where exposed |
| GM SW-CAN/GMLAN | Supported interface family where exposed |
| K-Line / ISO 9141-2 | Supported protocol family where exposed |
| ISO 14230/KWP2000 | Supported protocol family where exposed |
| SAE J1850 PWM/VPW | Supported protocol family where exposed |
| CAN FD | Must be discovered explicitly; never inferred from vehicle model alone |
| ELM327 TCP | Transport target, not proof of diagnostic capability |
| SLCAN/raw CAN TCP | Transport target, not proof of diagnostic capability |
| mDNS | Discovery convenience; timeout/failure is a normal state |
| SD logging | Device-side logging capability where available |
| HTTPS | Secure transport capability where configured |
| MQTTS | Secure telemetry capability where configured |
| WireGuard | Secure remote transport capability where configured |

The matrix is deliberately not a static claim that every firmware revision exposes every feature. The decoder should attach `firmware_version`, `hardware_revision`, `transport_mode`, and discovery timestamp to the result.

## Granular vehicle capability discovery

Discovery must operate below the vehicle level:

```text
Vehicle
  └─ ECU / subsystem
      └─ data group
          └─ metric
              └─ module / brick / cell
```

Example:

```text
Battery
  cell_voltage       AVAILABLE
  cell_temperature   PARTIAL
  brick_voltage      AVAILABLE
  Riso               STATUS_ONLY

Module 1
  cell_voltage       AVAILABLE
  cell_temperature   AVAILABLE

Module 2
  cell_voltage       AVAILABLE
  cell_temperature   UNKNOWN
```

This prevents a single `Battery = supported` flag from hiding partial data.

## Cache identity

Capability discovery is cached by the most specific stable identity available:

```text
VIN
+ vehicle software / firmware scope
+ BMS firmware scope when available
+ decoder version
+ WiCAN firmware version
+ transport mode
```

A Model Y is therefore not treated as one immutable capability profile. A firmware change can invalidate a previous discovery result.

## Market detection

Market/region is only displayed when reliably decoded from VIN, vehicle data, or a verified decoder signal.

```text
US market reliably identified
→ display: ⚠ US-market configuration

EU market reliably identified
→ display: EU-market configuration

market unknown
→ display: Market: UNKNOWN
```

Model year or a guessed VIN pattern is not enough to assert a market.

## Safety boundary

Capability discovery is read-only. It may probe communication and documented read-only diagnostics, but must not turn a capability check into a write, routine-control, actuator, coding, flashing, or security-access operation.
