# SEOBD deep reconstruction — stages 1–12

Date: 2026-09-05

## Purpose

This document records the reconstruction pipeline used to turn static native/DWARF evidence into a defensible diagnostic decoder. A semantic field is never promoted to a CAN decoder merely because its name or protobuf field number looks plausible.

## Stage 1 — DWARF type graph

Evidence already recovered from the ARM64 native library includes generated-message types, accessors, enum values and optional-field cases. The `SVDF_*` semantic identifiers are therefore treated as application-layer identifiers.

Key examples:
- `bms_battery_voltage` → semantic ID 28 → accessor returns `uint32`.
- `charging_dc_voltage` → semantic ID 76 → accessor returns `int32`.
- `bms_cell_max_voltage` → semantic ID 43 with matching optional-case enum.

Status: CORROBORATED_STATIC.

## Stage 2 — protobuf field definitions

The next reconstruction boundary is the generated protobuf type graph: message name → field name → protobuf field number → scalar/message type → repeated/optional/presence semantics.

The semantic `SVDF` number must not be confused with a protobuf wire field number. The registry therefore keeps these identifiers in separate namespaces.

Target record:

```text
message_type
field_name
semantic_id
protobuf_field_number
protobuf_wire_type
scalar_type
presence_model
source_symbol
confidence
```

Status: PARTIALLY RECONSTRUCTED; wire-level mapping remains open.

## Stage 3 — field numbers and concrete types

Generated accessors provide a strong cross-check. For every field we seek both the accessor type and the generated optional-case/presence metadata. A field is accepted only when the two agree.

Important distinction:
- semantic ID = vehicle-data application identifier;
- protobuf field number = serialization identifier within a protobuf message;
- CAN arbitration ID = transport identifier;
- BLE characteristic/packet identifier = transport identifier.

These four namespaces must remain separate.

Status: CORROBORATED for selected fields; full inventory pending.

## Stage 4 — serialization

Trace `SerializeToArray`, `SerializeAsString`, `ByteSizeLong`, generated internal metadata and message constructors around `ReqSubscribeVehicleData`, `RespSubscribeVehicleData` and `PushVehicleDataHolder`.

Goal: establish exact protobuf wire bytes and field tags before any transport interpretation.

A protobuf field tag is encoded as `(field_number << 3) | wire_type`; this is a serialization fact, not a CAN identifier.

Status: RECONSTRUCTION TARGET; no CAN mapping claimed.

## Stage 5 — CMessageHelper

Recovered native symbols include `CMessageHelper::GetReqSubscribeVehicleData(bool)`. This makes the message-builder layer a high-value pivot.

Trace required:

```text
GetReqSubscribeVehicleData
 → constructed request
 → serialization call
 → QByteArray/std::string boundary
 → BLE send method
```

The boolean argument must also be traced rather than guessed.

Status: SYMBOL-LEVEL ENTRY POINT CONFIRMED.

## Stage 6 — BLE transport

Recovered symbols include `CBLECommander` and a `slotDataPushReceived(QString const&, QByteArray const&)` boundary. This is the transition from typed message handling to byte transport.

Required evidence:
- characteristic/service UUID;
- write vs notify direction;
- packet fragmentation/reassembly;
- MTU assumptions;
- framing bytes;
- checksum/CRC if any;
- message routing identifier;
- timeout/retry behavior.

Status: APPLICATION-LAYER BOUNDARY CONFIRMED; exact wire framing unresolved.

## Stage 7 — framing

Do not interpret an arbitrary byte sequence as a CAN frame until framing is proven. Build a frame grammar from repeated static call sites and, where available, captured runtime traffic.

Candidate grammar fields:

```text
transport_header
message_type
length
sequence/correlation
payload
checksum
terminator
```

Each field gets an evidence reference and confidence state.

Status: OPEN.

## Stage 8 — request/response correlation

The subscription flow provides the first explicit correlation chain:

```text
ReqSubscribeVehicleData
        ↓
RespSubscribeVehicleData
        ↓
PushVehicleDataHolder notifications
```

Recovered processing symbols include `ProcessSubscripVehicleDataRequest(...)`, `ProcessPushVehicleDataHolder(...)` and `slotDataPushReceived(...)`.

The next step is to identify the actual response discriminator and whether push packets carry an independent message type, sequence number or command correlation.

Status: APPLICATION FLOW CORROBORATED.

## Stage 9 — PushVehicleDataHolder

`PushVehicleDataHolder` is currently the most valuable semantic endpoint because it exposes typed vehicle-data fields. The reconstruction registry should store:

```text
message
  → field
  → semantic ID
  → protobuf field number
  → scalar type
  → presence
  → accessor
```

Only after this mapping is stable should a field be connected to transport bytes.

Status: STRONG STATIC EVIDENCE.

## Stage 10 — actual wire payload

The decisive evidence is a byte-level path from the serialized message into the BLE payload. For every candidate payload we require:

```text
raw bytes
→ framing removal
→ message discriminator
→ protobuf/other serialization decode
→ field tag
→ typed value
```

If the payload contains an embedded CAN frame, the CAN layer is then separately reconstructed as:

```text
CAN ID
→ DLC
→ payload
→ signal bit range
→ endian/sign
→ scaling/offset
→ unit
```

No values are invented when the wire path is incomplete.

Status: NOT YET VERIFIED.

## Stage 11 — decoder reconstruction

A decoder becomes eligible for implementation only when its evidence chain is complete enough for the claimed layer. Candidate states:

- `STRING_ONLY`
- `SYMBOL_CORRELATED`
- `DWARF_CORROBORATED`
- `MESSAGE_CORRELATED`
- `WIRE_CORRELATED`
- `RUNTIME_CORRELATED`
- `VERIFIED`

`VERIFIED` requires a reproducible request/response or push-data interpretation and a known conversion path. Manufacturer-specific decoders remain disabled until that gate is met.

## Stage 12 — SEOBD registry promotion

Promotion path:

```text
raw extraction
   ↓
static evidence
   ↓
semantic field registry
   ↓
message/protobuf registry
   ↓
transport/framing registry
   ↓
wire decoder candidate
   ↓
runtime correlation
   ↓
verified decoder
   ↓
SEOBD read-only signal registry
```

Every promoted record retains provenance. Unsupported CAN IDs, scaling, units and runtime values remain `UNKNOWN` rather than being inferred from names.

## Current evidence snapshot

The current native reconstruction contains:
- 304,038 native symbols inspected;
- 84,304 project-candidate symbols in the deeper pass;
- 3,244 source/debug paths;
- 109,837 diagnostic-oriented strings/candidates;
- 2,206 protobuf-oriented symbol candidates.

These counts describe extraction candidates, not verified diagnostic signals.

## Safety gate

No write/control behavior is promoted by this document. No static value is presented as a measured vehicle value. No `SVDF_*` identifier is treated as a CAN arbitration ID. No scaling or physical unit is assumed without evidence.
