# SEOBD — vehicle signal promotion matrix

Date: 2026-09-05

This matrix prevents semantic protobuf fields from being incorrectly promoted into CAN decoders.

| Layer | Example: battery voltage | Evidence status |
|---|---|---|
| Semantic vehicle-data ID | 28 | VERIFIED_STATIC |
| Protobuf field | 60 | SERIALIZATION_CORRELATED |
| Protobuf wire type | 0 / varint | SERIALIZATION_CORRELATED |
| Application type | uint32 | DWARF_CORROBORATED |
| BLE characteristic payload | QByteArray | TRANSPORT_CORRELATED |
| Security transform | path-dependent; Encrypt/Decrypt exists | TRANSPORT_CORRELATED |
| Outer framing | exact byte header/length semantics | PENDING |
| Inner protobuf payload | EnhApiPayloadHolder / typed messages | SERIALIZATION_CORRELATED |
| CAN arbitration ID | unknown | UNVERIFIED |
| CAN DLC | unknown | UNVERIFIED |
| CAN byte offset | unknown | UNVERIFIED |
| CAN bit offset/length | unknown | UNVERIFIED |
| Endianness | unknown | UNVERIFIED |
| Signedness at CAN layer | unknown | UNVERIFIED |
| Scale | unknown | UNVERIFIED |
| Offset | unknown | UNVERIFIED |
| Physical unit conversion | unknown | UNVERIFIED |

## Promotion rule

A SEOBD manufacturer-specific CAN signal may only move to `VERIFIED` when a complete evidence chain exists:

`CAN frame → arbitration ID → DLC → byte/bit location → endian/signed interpretation → raw value → scale/offset → unit → physical signal → application/protobuf correlation`.

A protobuf semantic field alone is insufficient.

## Current concrete result

`bms_battery_voltage` is known to be semantic field ID `28`, protobuf field `60`, wire type `0`, and application return type `uint32`.

It is **not** currently known to be any particular CAN ID or CAN byte sequence.

## Safety gate

Do not add a manufacturer-specific CAN decoder from the current evidence. Read-only application-level parsing can be implemented against the reconstructed protobuf layer, but CAN signal promotion remains blocked until runtime/capture correlation and conversion evidence are obtained.
