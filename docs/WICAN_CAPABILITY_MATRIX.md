# WiCAN Pro Capability Matrix

This document separates **physical/interface capability** from what AutoDiag has actually implemented and verified. A capability listed by the hardware vendor is not automatically a supported AutoDiag diagnostic feature.

## Verified hardware baseline

The current WiCAN Pro vendor specification lists an advanced OBD interface supporting ISO 15765-4 CAN, SAE J1939, ISO 11898 raw CAN, MS-CAN, GM high-speed CAN, Single-Wire CAN/GMLAN, SAE J1850 PWM/VPW, ISO 9141-2 and ISO 14230 slow/fast. It also lists microSD logging, USB-C, USB host/peripherals and a multifunction push button. AutoDiag treats these as **hardware capabilities**, then independently verifies firmware, wiring, vehicle topology and decoder support.

CAN FD is not listed in the current WiCAN Pro specification and therefore remains `NOT_SUPPORTED` until a verified hardware/firmware path exists.

## Matrix

| Interface / feature | WiCAN Pro hardware | AutoDiag policy | State |
|---|---:|---|---|
| HS-CAN / ISO 15765-4 | Yes | Read-only CAN/OBD transport | SUPPORTED_TARGET |
| Raw CAN / ISO 11898 | Yes | Capture/replay and diagnostics where configured | SUPPORTED_TARGET |
| MS-CAN | Yes | Capability-gated, vehicle-specific probing | SUPPORTED_TARGET |
| GM high-speed CAN | Yes | Capability-gated | SUPPORTED_TARGET |
| Single-Wire CAN / GMLAN | Yes | Capability-gated | SUPPORTED_TARGET |
| SAE J1850 PWM | Yes | Legacy protocol adapter layer | SUPPORTED_TARGET |
| SAE J1850 VPW | Yes | Legacy protocol adapter layer | SUPPORTED_TARGET |
| ISO 9141-2 / K-Line | Yes | Read-only legacy diagnostics where exposed | SUPPORTED_TARGET |
| ISO 14230 slow/fast | Yes | KWP transport layer | SUPPORTED_TARGET |
| CAN FD | Not listed | Never claim support; show unavailable | NOT_SUPPORTED |
| microSD logging | Yes | Capture persistence + offline replay | SUPPORTED_TARGET |
| USB-C | Yes | Firmware/USB path only where documented | HARDWARE_CAPABILITY |
| USB host | Yes | Optional peripheral transport | PLANNED |
| Wi-Fi | Yes | TCP/UDP/MQTT/HTTP subject to firmware configuration | SUPPORTED_TARGET |
| BLE | Firmware dependent | Capability discovery before use | CAPABILITY_GATED |
| J2534 PassThru | No native J2534 claim | External bridge only | PLANNED_VIA_BRIDGE |
| LIN | Not listed | Do not claim native LIN support | NOT_SUPPORTED |

## Security classification

Transport security and vehicle write capability are separate dimensions.

- HTTPS / MQTTS / WireGuard: `TRANSPORT_SECURE` only when the configured connection is actually authenticated and encrypted.
- Plain HTTP / plaintext MQTT / open TCP: `UNENCRYPTED_WARNING`.
- Secure transport does **not** make a dangerous vehicle command safe.
- READ and ANALYZE are initial production scope. WRITE/COMMAND remains isolated and disabled by default.

## Vehicle-bus discovery

AutoDiag may enumerate only interfaces actually exposed through the adapter and configured wiring. It must not infer that every listed protocol is simultaneously connected to the vehicle.

```text
Adapter
  WiCAN Pro
  Firmware: <reported version>

Hardware capability
  HS-CAN          ✓
  MS-CAN          ✓
  SW-CAN/GMLAN    ✓
  K-Line          ✓
  J1850           ✓
  CAN FD          ✗

Vehicle-observed
  Active bus      HS-CAN
  ECU responses   19
  Other buses     not tested
```

## Source policy

Hardware capability claims should link to vendor documentation. Vehicle-specific availability requires an observed response plus a verification state. Community decoder knowledge may guide probing but cannot silently become a production capability claim.

Vendor reference: https://www.meatpi.com/products/wican-pro
