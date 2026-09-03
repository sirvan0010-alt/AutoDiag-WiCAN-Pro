# ISO-TP / UDS protocol foundation

AutoDiag now has a small typed foundation below the vehicle-profile layer:

```text
CAN frame
   ↓
ISO-TP PCI decoder
   ↓
ISO-TP payload / reassembly
   ↓
UDS response classifier
   ↓
vehicle/ECU decoder
   ↓
capability-gated diagnostic operation
```

## Scope

The current implementation is a **protocol foundation**, not a claim of complete UDS support. It can classify classic-CAN ISO-TP single, first, consecutive and flow-control frames, reassemble incoming multi-frame payloads, and distinguish UDS positive responses from negative responses (`0x7F`).

The current foundation does **not yet** implement the complete transmit side, UDS service execution, SecurityAccess workflows, coding/long-coding, programming, or vehicle-specific write procedures.

Those are future capabilities, not permanently forbidden capabilities.

## Read-only evidence pipeline

`CanIsoTpUdsPipeline` now connects the existing protocol layers without coupling them to a vehicle brand:

```text
CanFrame
   ↓
IsoTpReassembler
   ↓
UDS byte payload
   ↓
UdsResponseParser
   ↓
UdsPipelineResult
   ↓
DiagnosticEvidence<UdsResponse>
```

The pipeline is deliberately receive-only. It does not emit ISO-TP flow-control frames or send diagnostic commands. Positive UDS responses become `AVAILABLE` evidence; a valid UDS negative response becomes `UNAVAILABLE` with the NRC preserved instead of being misclassified as a transport failure. Malformed ISO-TP/UDS data remains a failed protocol result.

Evidence provenance is marked as `EvidenceSource.UDS`, while the CAN identifier may be retained as a caller-supplied source identifier. The implementation does not infer that a CAN ID represents engine, ABS, SRS, transmission or another ECU unless the caller supplies that scope.

Replay/unit coverage now exercises single-frame UDS, multi-frame ISO-TP reassembly, negative UDS responses and malformed ISO-TP input.

## Read and write architecture

AutoDiag is intended to support the full diagnostic lifecycle when a vehicle/ECU capability is positively established:

- observation and identification,
- live data and diagnostic reads,
- service actions and resets,
- configuration writes such as VAG coding/long coding and adaptations,
- actuator/routine control,
- programming where the exact procedure and prerequisites are known,
- security-critical operations behind explicit security and safety gates.

A write is never implied merely because an ECU answers a UDS request. Before a write, the system must establish the exact vehicle/ECU scope, supported service, prerequisites, current state, required security level, recovery path, and verification strategy. See `docs/WRITE_CAPABILITY_ARCHITECTURE.md`.

## Why this layer matters

The WiCAN ecosystem must not couple an individual vehicle brand to the physical adapter. ISO-TP and UDS belong below VAG, Tesla, BMW, Hyundai/Kia and other profile implementations. A future profile can consume the same normalized diagnostic payloads while the adapter/transport remains independent.

## Safety contract

Unknown, malformed or incomplete traffic must remain observable as an error/unknown condition. A decoded transport packet is not by itself proof that a particular ECU, vehicle function or service is supported.

Write operations must remain isolated behind explicit capability checks, exact vehicle/ECU scope, prerequisites, user confirmation where appropriate, verification/read-back, evidence and recovery requirements. `UNKNOWN` must never be treated as `SUPPORTED`.
