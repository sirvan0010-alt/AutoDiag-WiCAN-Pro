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

Replay/unit coverage exercises single-frame UDS, multi-frame ISO-TP reassembly, negative UDS responses and malformed ISO-TP input.

## ReadDataByIdentifier foundation

The read-only DID layer adds an explicit `0x22` request model and a strict `0x62` response parser. A caller must provide the requested DID; the parser rejects a response for a different DID. Returned bytes are preserved as raw `UdsDidValue` data.

## Standard ECU identification layer

`UdsEcuIdentificationDecoder` adds a second, deliberately narrow semantic layer over the raw DID bytes. It recognizes standardized UDS identification DIDs, including:

- `F190` — VIN
- `F18A` — system supplier identifier
- `F18B` — ECU manufacturing date
- `F18C` — ECU serial number
- `F188/F189` — vehicle-manufacturer ECU software number/version
- `F191` — vehicle-manufacturer ECU hardware number
- `F192/F193` — system-supplier ECU hardware number/version
- `F194/F195` — system-supplier ECU software number/version
- `F197` — system name or engine type

The semantic mapping is standardized, but the record format/content can remain ECU- or supplier-specific. Therefore the implementation **always retains the raw DID bytes** and only exposes a text view when the returned bytes are printable ASCII. Dates and other binary/BCD representations are not silently converted to text.

`EcuIdentificationSnapshot` provides convenient VIN, supplier, hardware and software fields while retaining per-field verification status. Manufacturer-specific or unknown DIDs remain unlabeled instead of being guessed.

This is the intended boundary:

```text
UDS 0x22 request
   ↓
transport / ISO-TP
   ↓
UDS 0x62 response
   ↓
requested DID check
   ↓
raw DID value
   ↓
standard DID semantic mapping
   ↓
verified ECU identification snapshot
```

This does **not** mean that every ECU implements every standardized DID. An ECU may legitimately return a negative response such as `requestOutOfRange`; that remains an availability result rather than proof of a communication failure.

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
