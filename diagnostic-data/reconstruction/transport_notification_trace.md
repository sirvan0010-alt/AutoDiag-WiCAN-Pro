# SEOBD — BLE transport and notification trace

Date: 2026-09-05

## Reconstructed path

```text
CMessageHelper::GetReqSubscribeVehicleData(bool)
        ↓
ReqSubscribeVehicleData (protobuf)
        ↓
CBLECommander::ProcessSubscripVehicleDataRequest(...)
        ↓
CSession::SendDataToDevice(QString, QByteArray)
        ↓
(optional encryption path selected by GetEncryptType())
        ↓
QLowEnergyService::writeCharacteristic(...)
        ↓
BLE device
        ↓
QLowEnergyService::characteristicChanged
        ↓
CSession::slotDataPushReceived(QString, QByteArray)
        ↓
CBLECommander notification processing
        ↓
EnhApiPayloadHolder::ParseFromArray(...)
        ↓
PushVehicleDataHolder
        ↓
CPushVehicleDataNotification
        ↓
CDashboardModule / dashboard data
```

## Direct evidence

### Request side

`CBLECommander::ProcessSubscripVehicleDataRequest(...)` calls:

- `CMessageHelper::GetReqSubscribeVehicleData(bool)` at native call site `0x47eb5a8`.
- `CSession::SendDataToDevice(QString const&, QByteArray const&)` at `0x47eb6a8`.

`CMessageHelper::GetReqSubscribeVehicleData(bool)` constructs `ReqSubscribeVehicleData` and fills its repeated integer subscription list. The first values are visible as immediate constants, including 24–29 and 125–128.

### BLE write

`CSession::SendDataToDevice(...)` resolves the device characteristic using `GetDeviceCharacteristicID(ECharacteristicType)` and performs an indirect call through the BLE object's vtable. The encryption path calls `GetEncryptType(QString)` before passing a `QByteArray` to the BLE object.

`CBLETransport::SendConfigData(...)` contains a direct call to Qt's:

`QLowEnergyService::writeCharacteristic(const QLowEnergyCharacteristic&, const QByteArray&, QLowEnergyService::WriteMode)`

at native address `0x4b82a50`.

This establishes the application→Qt BLE characteristic write boundary. The exact characteristic UUID and exact wire bytes still require a runtime/capture correlation or complete static recovery of the selected characteristic and framing/encryption implementation.

### Notification side

The native library contains:

- `CBLETransport::slotServiceCharacteristicChanged(QLowEnergyCharacteristic const&, QByteArray const&)`
- `CSession::slotDataPushReceived(QString const&, QByteArray const&)`
- `CBLECommander::slotDataPushReceived(QString const&, QByteArray const&)`
- `CBLECommander::ProcessSubscripVehicleDataResponse(QByteArray const&)`
- `CBLECommander::ProcessPushVehicleDataHolder(PushVehicleDataHolder const&)`

The subscription-response processor constructs `EnhApiPayloadHolder` and calls protobuf `MessageLite::ParseFromArray(const void*, int)` at `0x47f6cc0` (and equivalent paths for adjacent response types). This proves protobuf parsing from the received `QByteArray` at the application boundary.

## Push path

`CBLECommander::ProcessPushVehicleDataHolder(...)` allocates `CPushVehicleDataNotification` and copies holder values into the notification while checking each holder oneof case. For example, the battery-voltage path reads the holder discriminator at offset `0x2a4`, checks it against semantic case `28`, then copies the holder value from offset `0xa0` into the notification.

This is a raw-value transfer: no scale, offset or CAN extraction occurs in this function.

## Dashboard conversion boundary

`FillOldDashboardDataFromVehicleData(...)` reads notification values and forwards them to dashboard notification objects. Examples:

- battery current → `CPushDashQuickDataNotification::SetBattCurrent(int)`
- battery voltage → `CPushDashQuickDataNotification::SetBattVoltage(int)`
- front/rear torque and power → integer dashboard setters
- DC current → `CPushDashSlowDataNotification::SetDCCurrent(int)`
- DC voltage → `CPushDashSlowDataNotification::SetDCVoltage(int)`

`CDashboardData::SetBattVoltage(double)` and `SetBattCurrent(double)` simply store the already-converted `double` supplied by the caller. The static evidence inspected here does not establish a vendor scaling formula between the protobuf integer and that dashboard double.

## What is now proven

1. Semantic subscription IDs are explicitly requested.
2. `ReqSubscribeVehicleData` is a generated protobuf message.
3. `PushVehicleDataHolder` is a generated protobuf message with generated serialization/deserialization.
4. A received `QByteArray` is parsed by `EnhApiPayloadHolder::ParseFromArray`.
5. BLE characteristic writing is performed through Qt `QLowEnergyService::writeCharacteristic`.
6. The decoded holder is converted into a push notification containing raw application values.
7. The dashboard layer consumes those values.

## What is still blocked

- exact BLE characteristic UUID for this vehicle-data channel
- exact outer framing bytes
- encryption mode/key/session material for the selected channel, if applied
- complete captured payload
- protobuf field→wire byte examples from a runtime capture
- CAN arbitration ID
- CAN DLC
- CAN byte/bit location
- CAN endianness
- CAN signedness
- CAN scale/offset/unit

Status: `TRANSPORT_CORRELATED` through the Qt BLE write and protobuf parse boundaries; `WIRE_CORRELATED` and `CAN_MAPPING` remain unverified.
