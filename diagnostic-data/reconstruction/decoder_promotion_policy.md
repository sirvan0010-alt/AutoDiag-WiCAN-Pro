# SEOBD decoder promotion policy

A decoder may exist as a candidate without being enabled for vehicle diagnostics.

## Evidence ladder

1. STRING_ONLY — name/string found.
2. SYMBOL_CORRELATED — function/class relationship found.
3. DWARF_CORROBORATED — type/enum/accessor relationship independently confirmed.
4. MESSAGE_CORRELATED — request/response/message relationship confirmed.
5. SERIALIZATION_CORRELATED — protobuf or equivalent wire encoding established.
6. TRANSPORT_CORRELATED — BLE/CAN framing and direction established.
7. WIRE_CORRELATED — concrete payload bytes map to the message.
8. RUNTIME_CORRELATED — live/captured traffic confirms interpretation.
9. VERIFIED — conversion and semantics are reproducible.

## Promotion rules

- `SVDF_*` values never become CAN IDs by assumption.
- A protobuf field number never becomes a CAN ID by assumption.
- A byte position never becomes a physical signal without type/endian/scaling evidence.
- A field name never supplies a unit by itself.
- Static constants do not constitute measured vehicle values.
- Manufacturer-specific write/control functions stay blocked regardless of static confidence.
- Candidate decoders are allowed in reconstruction data but are not callable by the production diagnostic path.

## Decoder record

```text
signal_name
semantic_id
message_type
protobuf_field_number
wire_identifier
transport
payload_offset
bit_offset
bit_length
endianness
signedness
raw_type
scale
offset
unit
status
evidence_refs
```

## Required transition to VERIFIED

The chain must be reproducible from the source evidence:

`request → transport → response/push → framing → serialization → field → typed value → conversion → semantic signal`.

If any required link is absent, the registry retains `UNKNOWN` for that property.
