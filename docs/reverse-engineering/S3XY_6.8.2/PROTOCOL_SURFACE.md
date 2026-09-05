# S3XY 6.8.2 protocol surface

Observed from native symbols, generated protobuf symbols, DWARF source-path metadata and BLE transport symbols. This records independently useful interfaces and evidence without reproducing proprietary source code.

## 1. BLE transport/session

Observed classes and operations:
- `CBLETransport` — BLE service discovery, characteristic read/write, notification handling and MTU checks.
- `CSession` — device verification, session initialization, push subscription, response/push routing and reconnect/error state.
- `CBLECommander` — request/response dispatch for buttons, dashboard, stalks, knob, strip, car settings, vehicle data, Tesla BLE and firmware operations.

The binary explicitly references Qt BLE APIs including `QLowEnergyController`, `QLowEnergyService`, `QLowEnergyCharacteristic` and descriptor/notification operations. Both write-with-response and write-without-response paths are present.

Observed UUIDs:
- `5857a678-87c6-11eb-8dcd-0242ac130003`
- `8D53DC1D-1DB7-4CD3-868B-8A527460AA84`
- `DA2E7828-FBCE-4E01-AE9E-261174997C48`

The exact service/characteristic role of each UUID is not asserted here without runtime mapping.

## 2. Vehicle-data protocol

Observed protobuf message families:
- `ReqSubscribeVehicleData` / `RespSubscribeVehicleData`
- `ReqSubscribeVehicleEventsData` / `RespSubscribeVehicleEventsData`
- `PushVehicleDataHolder`
- `PushVehicleEventsDataHolder`
- `PushDashQuickData`
- `PushDashMediumData`
- `PushDashSlowData`
- `ReqDashQuickDataFactors`, `ReqDashMediumDataFactors`, `ReqDashSlowDataFactors` and matching responses
- `ReqDashTriggerAllDataPush` / `RespDashTriggerAllDataPush`

This is a strong indication that the app receives vehicle data as structured push/subscription messages rather than relying only on periodic UI polling.

## 3. Tesla API / BLE session

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

## 4. Device/control protocol families

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

## 5. OTA / write boundary

The binary contains explicit OTA and write-related paths including firmware update, OTA encryption, device restart, factory reset and configuration setters. These must remain isolated in AutoDiag and must not be promoted to read-only diagnostic capability.

## 6. Important evidence rule

A symbol, protobuf message or UI element proves implementation surface only. It does **not** prove:
- field number
- unit or scaling
- ECU/CAN origin
- exact transport framing
- vehicle generation coverage
- firmware compatibility
- safety of a write operation

Those require descriptor/runtime/vehicle evidence and must stay `unknown` or `unverified` until verified.
