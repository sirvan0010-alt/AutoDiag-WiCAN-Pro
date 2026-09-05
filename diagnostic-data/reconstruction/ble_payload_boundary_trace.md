# SEOBD — BLE payload boundary trace

Date: 2026-09-05
Source: supplied `S3XY_6.8.2.xapk`, native ARM64 library `libS3XYButtons_arm64-v8a.so`

## Objective

Trace the vehicle-data subscription path beyond protobuf serialization and identify the exact boundary between BLE characteristic data, optional security transformation, and the protobuf payload.

## Proven call chain

### TX

`CMessageHelper::GetReqSubscribeVehicleData(bool)` constructs `ReqSubscribeVehicleData` and populates `subscribe_fields` with semantic IDs.

The `CSession::SendDataToDevice(QString const&, QByteArray const&)` path then:

1. validates the session and `QByteArray` length;
2. resolves a device characteristic using `GetDeviceCharacteristicID(ECharacteristicType)`;
3. compares the requested path/characteristic;
4. selects encryption using `GetEncryptType(QString const&)` when required;
5. passes the resulting `QByteArray` into the BLE transport object's virtual send operation.

The native function contains the explicit `QByteArray` length guard `<= 0x200` (512 bytes).

## RX

`CBLETransport::slotServiceCharacteristicChanged(QLowEnergyCharacteristic const&, QByteArray const&)` receives the BLE characteristic value directly from Qt's Bluetooth layer.

The function:

- obtains the characteristic UUID;
- matches the characteristic against registered service/characteristic paths;
- handles multiple characteristic paths;
- eventually calls `CTransport::signalDataPushReceived(QString const&, QByteArray const&)` with a `QByteArray`.

`CSession::slotDataPushReceived(QString const&, QByteArray const&)` then receives that byte array. The session checks the characteristic/path and, for encrypted paths, calls its security object's decrypt operation with the received `QByteArray`. The resulting byte array is forwarded through `signalDataPushReceived(...)`.

This establishes the following evidence-backed boundary:

`Qt BLE characteristicChanged QByteArray` → `CBLETransport` characteristic/path selection → `CTransport::signalDataPushReceived` → `CSession::slotDataPushReceived` → optional decrypt → `CSession::signalDataPushReceived` → `CBLECommander::slotDataPushReceived`.

## Important finding: no generic CAN framing was found at this boundary

The received object at the transport/session boundary is a Qt `QByteArray`. The current static evidence does not show that this byte array is itself a classical CAN frame (`CAN ID + DLC + data`). It is an application/BLE payload container.

The BLE transport code is therefore not evidence for a CAN arbitration ID, DLC, byte offset, bit offset, endianness, scale, or physical unit.

## Security boundary

The session explicitly supports path-dependent encryption. The native library contains `CEnhSecurity::Encrypt(int, QByteArray const&)` and `CEnhSecurity::Decrypt(int, QByteArray const&)`. The encryption implementation uses mbedTLS AES-CTR primitives.

Therefore the byte sequence observed at the physical BLE characteristic can differ from the protobuf byte sequence consumed by the application. A decoder must not parse encrypted characteristic bytes as protobuf until the decrypt boundary is crossed.

## Protobuf boundary

`EnhApiPayloadHolder::_InternalParse(char const*, google::protobuf::internal::ParseContext*)` exists in the same native binary, together with `_InternalSerialize(...)`, `ByteSizeLong()` and `GetMetadata()`.

`EnhApiPayloadHolder` contains generated accessors for:

- `preqsubscribevehicledata`
- `prespsubscribevehicledata`
- `preq...` / `presp...` message families

The current evidence therefore supports an outer protobuf holder carrying typed request/response messages, but does not yet establish a universal byte-level framing header preceding `EnhApiPayloadHolder`.

## Current evidence chain

`GetReqSubscribeVehicleData()`
→ `ReqSubscribeVehicleData`
→ protobuf serialization
→ `QByteArray`
→ `CSession::SendDataToDevice()`
→ optional `CEnhSecurity::Encrypt()`
→ BLE transport write
→ BLE `characteristicChanged`
→ `CBLETransport`
→ `CTransport::signalDataPushReceived()`
→ `CSession::slotDataPushReceived()`
→ optional `CEnhSecurity::Decrypt()`
→ `CBLECommander::slotDataPushReceived()`
→ `EnhApiPayloadHolder` / typed push message processing.

## Evidence addresses

- `CMessageHelper::GetReqSubscribeVehicleData(bool)`: `0x4b94afc`
- `CSession::SendDataToDevice(QString const&, QByteArray const&)`: `0x4827dc0`
- `CBLETransport::slotServiceCharacteristicChanged(...)`: `0x4b7ee70`
- `CSession::slotDataPushReceived(...)`: `0x4825cf8`
- `CEnhSecurity::Encrypt(...)`: `0x4b7df30`
- `CEnhSecurity::Decrypt(...)`: `0x4b7e3d4`
- `EnhApiPayloadHolder::_InternalParse(...)`: `0x4a78ea8`
- `EnhApiPayloadHolder::_InternalSerialize(...)`: `0x4a82ff4`
- `EnhApiPayloadHolder::GetMetadata()`: `0x4a8c6a8`
- `CBLECommander::slotDataPushReceived(...)`: `0x47d7004`
- `CBLECommander::ProcessPushVehicleDataHolder(...)`: `0x47fcaf0`

## Status

`TRANSPORT_CORRELATED` for BLE/session flow.

`SERIALIZATION_CORRELATED` for generated protobuf.

`WIRE_CORRELATED`: pending exact runtime/capture byte correlation.

`CAN_MAPPING`: `UNVERIFIED`.

No CAN signal is promoted by this artifact.
