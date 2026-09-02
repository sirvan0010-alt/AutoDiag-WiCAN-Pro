# ISO-TP / UDS read-only foundation

AutoDiag now has a small typed foundation below the vehicle-profile layer:

```text
CAN frame
   ↓
ISO-TP PCI decoder
   ↓
ISO-TP payload
   ↓
UDS response classifier
   ↓
vehicle/ECU decoder
```

## Scope

The initial implementation is intentionally **read-only**. It classifies classic-CAN ISO-TP single, first, consecutive and flow-control frames and distinguishes UDS positive responses from negative responses (`0x7F`).

It does not yet perform multi-frame reassembly, transmit flow-control frames, execute UDS services, or perform SecurityAccess/coding/programming.

## Why this layer matters

The WiCAN ecosystem must not couple an individual vehicle brand to the physical adapter. ISO-TP and UDS belong below VAG, Tesla, BMW, Hyundai/Kia and other profile implementations. A future profile can consume the same normalized diagnostic payloads while the adapter/transport remains independent.

## Safety contract

Unknown, malformed or incomplete traffic must remain observable as an error/unknown condition. A decoded transport packet is not by itself proof that a particular ECU, vehicle function or service is supported.

Write services remain outside this foundation and must be isolated behind explicit capability checks, exact vehicle/ECU scope, verification and recovery requirements.
