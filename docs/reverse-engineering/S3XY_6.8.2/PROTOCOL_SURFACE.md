# S3XY 6.8.2 protocol surface

Observed from native symbols, generated protobuf symbols, embedded protobuf descriptors, DWARF source-path metadata and BLE transport symbols. This records independently useful interfaces and evidence without reproducing proprietary source code.

## 1. BLE transport/session

Observed classes and operations:
- `CBLETransport` — BLE service discovery, characteristic read/write, notification handling and MTU checks.
- `CSession` — device verification, session initialization, push subscription, response/push routing and reconnect/error state.
- `CBLECommander` — request/response dispatch for buttons, dashboard, stalks, knob, strip, car settings, vehicle data, Tesla BLE and firmware operations.

The binary explicitly references Qt BLE APIs including `QLowEnergyController`, `QLowEnergyService`, `QLowEnergyCharacteristic` and descriptor/notification operations. Both write-with-response and write-without-response paths are present. The code explicitly handles insufficient MTU as an unusable connection condition.

Observed UUIDs:
- `5857a678-87c6-11eb-8dcd-0242ac130003`
- `8D53DC1D-1DB7-4CD3-868B-8A527460AA84`
- `DA2E7828-FBCE-4E01-AE9E-261174997C48`

The exact service/characteristic role of each UUID is not asserted here without runtime mapping.

## 2. Vehicle-data subscription protocol

The embedded `enhapi_vehicle_data.proto` descriptor was recovered directly from the native binary.

`ReqSubscribeVehicleData`:
- field 1 `subscribe_all`: bool
- field 2 `unsubscribe_all`: bool
- field 13 `subscribe_fields`: repeated enum `.SubscribeVehicleDataField`
- field 14 `unsubscribe_fields`: repeated enum `.SubscribeVehicleDataField`

`RespSubscribeVehicleData`:
- field 1 `result`: enum `.Status`

`SubscribeVehicleDataField` contains a direct mapping from enum value to vehicle-data field, including BMS, drivetrain, driving-state, charging, climate, lights, autopilot, latches, trip and sensor fields. The complete field-number/type mapping is recorded in `VEHICLE_DATA_SURFACE.md`.

This is strong wire-level evidence that the application can request selective vehicle-data subscriptions rather than only receiving a fixed UI stream.

## 3. Vehicle-event subscription protocol

`ReqSubscribeVehicleEventsData`:
- field 1 `subscribe_all`: bool
- field 2 `unsubscribe_all`: bool
- field 13 `subscribe_fields`: repeated enum `.SubscribeVehicleEventsDataField`
- field 14 `unsubscribe_fields`: repeated enum `.SubscribeVehicleEventsDataField`

`RespSubscribeVehicleEventsData`:
- field 1 `result`: enum `.Status`

`PushVehicleEventsDataHolder` fields:
- 1 `scroll_wheel_left_double_press`: bool
- 2 `scroll_wheel_left_pressed`: enum `.SwitchState`
- 3 `scroll_wheel_left_scroll_ticks`: int32
- 4 `scroll_wheel_left_tilt_left`: enum `.SwitchState`
- 5 `scroll_wheel_left_tilt_right`: enum `.SwitchState`
- 6 `scroll_wheel_right_double_press`: bool
- 7 `scroll_wheel_right_pressed`: enum `.SwitchState`
- 8 `scroll_wheel_right_scroll_ticks`: int32
- 9 `scroll_wheel_right_tilt_left`: enum `.SwitchState`
- 10 `scroll_wheel_right_tilt_right`: enum `.SwitchState`

`SwitchState` values observed in the descriptor: `SS_SNA=0`, `SS_Off=1`, `SS_On=2`, `SS_Fault=3`.

## 4. Tesla API / BLE session

Observed Tesla-specific operations:
- `TeslaAPI::ReqConnectToCar` / `CmdConnectToCar` / `RespConnectToCar`
- `TeslaAPI::ReqStartSession` / `CmdStartSession` / `RespStartSession`
- `TeslaAPI::ReqSendPublicKeyToCar` / `CmdSendPublicKeyToCar` / `RespSendPublicKeyToCar`
- `TeslaAPI::ReqGetApiStatus` / `RespGetApiStatus`
- `TeslaAPI::PushApiStatus`
- `TeslaAPI::PushSessionStatus`
- `TeslaAPI::PushUIStatus`

Embedded authorization endpoints include Tesla's `auth.tesla.com` and `auth.tesla.cn` OAuth authorization URLs.

Native crypto references include mbedTLS ECDH with `MBEDTLS_ECP_DP_SECP256R1`. This is security-sensitive session infrastructure; AutoDiag should not copy credentials, keys or proprietary authentication material.

## 5. Dashboard/vehicle data streams

Observed messages include:
- `PushDashQuickData`
- `PushDashMediumData`
- `PushDashSlowData`
- `ReqDashQuickDataFactors` / response
- `ReqDashMediumDataFactors` / response
- `ReqDashSlowDataFactors` / response
- `ReqDashTriggerAllDataPush` / `RespDashTriggerAllDataPush`

These indicate separate data-rate/factor mechanisms in addition to the general vehicle-data subscription model.

## 6. Device/control protocol families

The binary contains request/response/push classes for:
- button scan, button metadata, labels, actions and one-click execution
- commander initialization/settings and feature support
- dashboard initialization/settings/status/data/firmware
- stalk scan/status/details/firmware
- knob configuration/menu/state/firmware
- light strip configuration/effects/firmware
- device discovery/removal/restart/factory reset
- Wi-Fi configuration
- automation/smart-action settings
- Tesla API and Tesla BLE status/session operations

## 7. OTA / write boundary

The binary contains explicit OTA and write-related paths including firmware update, OTA encryption, device restart, factory reset and configuration setters. These must remain isolated in AutoDiag and must not be promoted to read-only diagnostic capability.

## 8. Important evidence rule

A recovered protobuf schema establishes wire-level names/numbers/types. It does **not** establish:
- physical unit or scaling
- ECU/CAN origin
- freshness/latency
- exact BLE framing outside the protobuf payload
- vehicle generation coverage
- firmware compatibility
- safety of a write operation

Those require runtime and vehicle evidence and must remain `unknown` or `unverified` until verified.
