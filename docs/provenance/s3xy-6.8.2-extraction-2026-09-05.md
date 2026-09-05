# S3XY 6.8.2 forensic extraction

Date: 2026-09-05  
Source artifact: `S3XY_6.8.2.xapk`  
XAPK SHA-256: `b6d5188457978b4a9fd0e4c138d316abb7144291ff0eda97b20678a135faddc9`

## Evidence scope

This record is based on static forensic extraction of the supplied XAPK: ZIP/APK structure, DEX metadata/constants, native ELF exported symbols and strings. No live vehicle session was available during extraction.

Therefore extracted application behavior is **application evidence**, not automatic real-vehicle verification.

## Package architecture

- Android application using Qt 6 / QML with a Java Android bridge.
- Native core: `libS3XYButtons_arm64-v8a.so`.
- BLE/GATT stack exposed through Qt Bluetooth classes including `QLowEnergyController`, `QLowEnergyService`, `QLowEnergyCharacteristic` and descriptors.
- Network/WebView integration is present through Qt Network and Java bridge classes.
- Java bridge classes include `com.enhance.EnhanceActivity`, `EnhGetRequest`, `EnhPostRequest`, `AppUtils` and `AndroidWebViewController`.

## Vehicle-data model

The native binary contains a structured vehicle-data model with protobuf-related types including:

- `ReqSubscribeVehicleData`
- `RespSubscribeVehicleData`
- `PushVehicleDataHolder`
- `CPushVehicleDataNotification`
- `CTeslaAPIVehicleDataResponse`

A static symbol inventory identified 131 `SVDF_*` vehicle-data fields. The inventory includes BMS, charging, drivetrain, driving-state, climate, latch, lighting, sensor, trip and navigation domains.

Examples of directly observed field names include:

- BMS: `battery_current`, `battery_voltage`, `cell_max_voltage`, `cell_min_voltage`, `cell_max_temp`, `cell_min_temp`, `charge_status`, `max_discharge_power`, `max_regen_power`, `nominal_remaining`, `nominal_full_pack_now`, `nominal_full_pack_new`, `ideal_energy_remaining`, `energy_buffer`, `ac_charge_total`, `dc_charge_total`, `discharge_total`, `battery_heating_state`.
- Drivetrain: `front_power`, `front_torque`, `rear_power`, `rear_torque`, `rear_right_power`, `rear_right_torque`, `temp_front_stator`, `temp_rear_stator`.
- Driving: `speed`, `gear`, `accel_pedal_pos`, `brake_pressed`, `regen_level`, `stopping_mode`, `traction_control`, `motor_on_mode_state`, turn signals and wiper state.
- Climate: HVAC, fan, AC, defrost, recirculation, vents, heated/cooled seats and steering-wheel heater.
- Latches: car lock, doors, frunk and trunk.
- Sensors: outside temperature and four brake temperatures.
- Navigation: vehicle/destination coordinates, distance, energy at arrival, arrival time and traffic delay.

These names establish the application's data vocabulary. Exact protobuf field numbers, wire types and transport payload encodings require a separate descriptor/native-code extraction pass and must not be guessed from symbol names.

## Actions and control surface

Static native symbols expose large action enumerations, including climate, charging, locking, doors, lights, seats, media, gear, suspension, traction-control, regen, track mode and Tesla API actions. The inventory contains approximately 399 `EAT_*` symbols plus additional `EAID_*`, `KST_*`, `UP_S_*` and `ESA_*` groups.

Observed examples include `EAT_CLIMATE_AC_ON`, `EAT_CLIMATE_DOG_MODE`, `EAT_CHARGE_PORT_OPEN_CLOSE`, `EAT_LOCK_UNLOCK_LOCK_CAR`, `EAT_OPEN_TRUNK`, `EAT_GEAR_SHIFT_SET_P`, `EAT_REGEN_BREAK_SET_50` and many others.

These are **capability evidence only**. AutoDiag must not expose equivalent vehicle-control operations as production functionality merely because the application contains the action enum. Any write/control implementation remains safety-gated and requires exact protocol, vehicle scope, security and validation evidence.

## Device identity and security

The native layer exposes UUID exchange/removal and device verification symbols, including `ReqSendUUID`, `RespSendUUID`, `ReqRemoveAllButThisUUID`, `RespRemoveAllButThisUUID`, Knob/Strip UUID variants and `CSession::VerifyDevice`. The binary also contains the error text `Device UUID do not match!`.

This establishes that device identity verification exists in the application architecture. It does **not** by itself establish a reusable authentication algorithm for AutoDiag.

## Communication architecture

Native symbols include `CBLECommander`, `CCommanderData`, `CCarModule`, request/response processors and vehicle-data subscription processors. The observed structure is consistent with a layered pipeline:

`UI/QML -> application bridge -> native commander -> BLE/GATT -> structured request/response model -> vehicle-data subscription -> push notification -> UI`

AutoDiag should take this as an architectural reference while keeping its own transport abstraction independent of the source application.

## Tesla API

The binary exposes Tesla API action categories and Java/native network callbacks. Exact endpoints, authorization flow and token handling have **not** been reconstructed in this pass. No endpoint or credential mechanism is inferred here.

## Reuse policy for AutoDiag

The project may use this extraction as independently documented behavioural/reference evidence and re-implement useful concepts with explicit provenance. Do not copy proprietary binaries, generated native code, credentials, or protected application assets into AutoDiag.

Suggested evidence classification:

- `APPLICATION_OBSERVED`: directly observed in the supplied application.
- `APPLICATION_DERIVED`: behavior reconstructed from static code analysis.
- `CANDIDATE`: technically plausible and ready for controlled testing, but not vehicle-verified.
- `VERIFIED`: only after independent vehicle/capture validation within an explicit vehicle scope.

The extraction therefore feeds AutoDiag's knowledge/capability system but does not bypass the project's permanent verification gates.

## Next extraction pass

1. Recover protobuf descriptors/field numbers/types.
2. Trace `CBLECommander::SendData` and request/response processors.
3. Recover BLE service/characteristic UUID mapping.
4. Trace `ProcessPushVehicleDataHolder` and subscription lifecycle.
5. Separate vehicle telemetry from remote Tesla API data.
6. Normalize useful read-only telemetry into AutoDiag diagnostic-data candidates.
7. Add deterministic parser/decoder tests before any production promotion.
