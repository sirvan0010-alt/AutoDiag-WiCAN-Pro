# SEOBD — CAN mapping gate

Date: 2026-09-05

## Required chain

```text
payload
 ↓
CAN frame
 ↓
CAN ID
 ↓
DLC
 ↓
byte/bit
 ↓
endianness
 ↓
signedness
 ↓
scale
 ↓
offset
 ↓
unit
 ↓
physical signal
```

## Current result

The supplied native library gives strong evidence for an **application-layer vehicle-data protocol**, not a direct CAN-frame database.

The reconstructed object is:

```text
semantic vehicle-data ID
        ↓
protobuf field number / wire type
        ↓
protobuf integer value
        ↓
PushVehicleDataHolder
        ↓
CPushVehicleDataNotification
        ↓
dashboard/application signal
```

For example:

```text
semantic case 28
  = bms_battery_voltage
  ↓
protobuf field 60
  ↓
wire type 0 (varint)
  ↓
raw uint32
  ↓
PushVehicleDataHolder::bms_battery_voltage()
  ↓
CPushVehicleDataNotification battery-voltage member
  ↓
dashboard battery-voltage path
```

There is currently **no evidence in the reconstructed chain that field 60 is a CAN arbitration ID**. It is a protobuf field number.

## CAN fields: status

| Item | Status |
|---|---|
| CAN arbitration ID | UNVERIFIED |
| CAN extended/standard format | UNVERIFIED |
| DLC | UNVERIFIED |
| byte offset | UNVERIFIED |
| bit offset | UNVERIFIED |
| bit length | UNVERIFIED |
| endianness | UNVERIFIED |
| signedness | UNVERIFIED |
| scale | UNVERIFIED |
| offset | UNVERIFIED |
| unit | UNVERIFIED |
| physical-signal formula | UNVERIFIED |

## Why promotion is blocked

A manufacturer-specific CAN signal must not be created from a semantic name, protobuf number, or application-layer value alone. The missing proof must connect the vehicle-data application value to an actual CAN frame or to an authoritative protocol definition.

Acceptable promotion evidence is one of:

1. runtime capture with known request/response correlation and a simultaneous CAN trace;
2. authoritative protocol documentation containing the exact CAN signal definition;
3. complete native-code reconstruction showing CAN ID, frame layout, extraction and conversion before the value reaches the application layer.

## SEOBD safety rule

Until that evidence exists, the implementation may expose the application-layer protobuf field as **experimental/read-only reconstructed data**, but it must not label it as a manufacturer-specific CAN signal and must not generate write/control commands from it.

Status: `CAN_MAPPING_UNVERIFIED`.
