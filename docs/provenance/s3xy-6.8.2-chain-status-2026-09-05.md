# S3XY 6.8.2 — complete extraction-chain status

Date: 2026-09-05
Source: `S3XY_6.8.2.xapk`
XAPK SHA-256: `b6d5188457978b4a9fd0e4c138d316abb7144291ff0eda97b20678a135faddc9`

## Chain

`application -> ECU/vehicle capability -> protocol object -> request -> transport -> response -> decoder -> normalized signal -> test -> provenance -> candidate -> vehicle capture -> VERIFIED -> production`

## Completed from supplied application evidence

| Stage | Result | Evidence class |
|---|---|---|
| Application package | XAPK/base/splits extracted | APPLICATION_OBSERVED |
| Native core | `libS3XYButtons_arm64-v8a.so` identified | APPLICATION_OBSERVED |
| Runtime architecture | Qt6/QML + Java bridge + native C++ | APPLICATION_OBSERVED |
| BLE layer | Qt BLE controller/service/characteristic classes present | APPLICATION_OBSERVED |
| Vehicle-data model | protobuf model types present | APPLICATION_OBSERVED |
| Capability vocabulary | 131 `SVDF_*` symbols observed | APPLICATION_OBSERVED |
| Subscription request | `ReqSubscribeVehicleData` present | APPLICATION_OBSERVED |
| Subscription response | `RespSubscribeVehicleData` present | APPLICATION_OBSERVED |
| Push telemetry | `PushVehicleDataHolder` and `ProcessPushVehicleDataHolder` present | APPLICATION_OBSERVED |
| Subscribe processor | `ProcessSubscripVehicleDataRequest/Response` present | APPLICATION_OBSERVED |
| Unsubscribe processor | unsubscribe request/response processors present | APPLICATION_OBSERVED |
| Typed subscription enum | `SubscribeVehicleDataField` descriptor and validity function present | APPLICATION_OBSERVED |
| Enum range | `SubscribeVehicleDataField_IsValid` accepts values `< 0x83` and rejects `>= 0x83`; therefore valid numeric range is 0..130 | APPLICATION_OBSERVED |
| Device identity | UUID exchange/removal and `CSession::VerifyDevice` present | APPLICATION_OBSERVED |
| Action surface | large typed `EAT_*` action family observed | APPLICATION_OBSERVED |

## Decoder-ready signals

The following read-only signal names are sufficiently established as application vocabulary to enter the AutoDiag capability registry, but they are **not** assigned CAN IDs, UDS DIDs, protobuf field numbers, units or scales unless independently recovered:

- `bms.battery_voltage`
- `bms.battery_current`
- `bms.charge_status`
- `bms.ideal_energy_remaining`
- `bms.max_regen_power`
- `bms.max_discharge_power`
- `bms.nominal_full_pack_new`
- `bms.nominal_full_pack_now`
- `bms.nominal_remaining`
- `bms.energy_buffer`
- `bms.temp_pt_inlet`
- `bms.temp_battery_inlet`
- `bms.temp_inlet_target`
- `bms.cell_max_temp`
- `bms.cell_min_temp`
- `bms.cell_max_voltage`
- `bms.cell_min_voltage`
- `bms.ac_charge_total`
- `bms.dc_charge_total`
- `bms.regen_total`
- `bms.discharge_total`
- `bms.battery_heating_state`
- `drivetrain.front_torque`
- `drivetrain.front_power`
- `drivetrain.rear_torque`
- `drivetrain.rear_power`
- `drivetrain.inverters_count`
- `driving.speed`
- `driving.gear`
- `driving.regen_level`
- `driving.wiper_speed`
- `charging.dc_current`
- `charging.dc_voltage`
- `charging.low_bus_voltage`
- `charging.low_bus_current`
- `charging.high_bus_voltage`
- climate state fields
- latch state fields
- outside/brake temperature fields
- navigation/trip fields

## What is deliberately NOT promoted

The static artifact does not by itself establish:

1. Tesla vehicle ECU CAN identifiers for these fields.
2. UDS request/response bytes for these fields.
3. Exact protobuf field-number mapping of every `PushVehicleDataHolder` member.
4. Exact numeric mapping of every `SubscribeVehicleDataField` enum name.
5. Exact BLE service/characteristic UUID used for vehicle-data traffic.
6. Exact serialized request bytes.
7. Exact serialized push payload bytes.
8. Tesla API authorization/token semantics.
9. Vehicle-generation applicability.
10. Real-vehicle correctness.

## Promotion gates

`APPLICATION_OBSERVED`
→ static evidence captured.

`APPLICATION_DERIVED`
→ behavior reconstructed from code/data structures.

`CANDIDATE`
→ deterministic mapping exists and has unit tests.

`VEHICLE_CAPTURE`
→ request/response/push bytes reproduced against an explicitly scoped vehicle.

`VERIFIED`
→ capture validates decoder, addressing, applicability and semantics.

`PRODUCTION`
→ only after verification and project safety gates.

## Safety boundary

Read-only telemetry can proceed through the normal capability/decoder pipeline. Control, coding, adaptation, actuator, security and write operations remain isolated and disabled by default. Presence of an action enum is not evidence that AutoDiag may execute that action.

## CI state at extraction checkpoint

The previously observed AutoDiag Android workflow run `33946157349` was still `in_progress`, with core unit tests running and APK build/upload pending. Therefore this extraction checkpoint does not claim a green CI result.

## Conclusion

The **static application extraction chain is complete to the evidence boundary**: application architecture, BLE abstraction, typed subscription model, request/response/push processors, capability vocabulary, device identity surface and safety classification are recorded.

The remaining stages that require new evidence are explicitly blocked rather than guessed: exact wire-level protobuf/BLE mapping and real-vehicle validation. This is the maximum defensible promotion level from the supplied S3XY 6.8.2 artifact alone.
