# SEOBD — protobuf wire-field reconstruction

Date: 2026-09-05

## Scope

This artifact records the static reconstruction of the `ReqSubscribeVehicleData` / `PushVehicleDataHolder` protobuf layer from the supplied ARM64 native library. It does **not** claim a CAN mapping.

## 1. Request construction

`CMessageHelper::GetReqSubscribeVehicleData(bool)` allocates `ReqSubscribeVehicleData` and fills its repeated integer `subscribe_fields` list. The observed values include semantic IDs such as 24, 25, 26, 27, 125, 126, 127, 128, 28, 29, 30, ... .

Therefore the application explicitly requests vehicle-data semantic fields by numeric identifier.

## 2. Protobuf field-number reconstruction

`PushVehicleDataHolder::_InternalSerialize(...)` checks the oneof discriminator for each semantic case and emits a protobuf key. For the EV-relevant block the emitted keys are varint keys (`wire_type = 0`). The protobuf field number is `key >> 3`.

| semantic ID / case | field | protobuf key | wire type |
|---:|---:|---:|---:|
| 27 | sensors_outside_temperature | 59 | 0 |
| 28 | bms_battery_voltage | 60 | 0 |
| 29 | bms_battery_current | 61 | 0 |
| 30 | bms_charge_status | 62 | 0 |
| 31 | bms_ideal_energy_remaining | 63 | 0 |
| 32 | bms_max_regen_power | 80 | 0 |
| 33 | bms_max_discharge_power | 81 | 0 |
| 34 | bms_nominal_full_pack_new | 82 | 0 |
| 35 | bms_nominal_full_pack_now | 83 | 0 |
| 36 | bms_nominal_remaining | 84 | 0 |
| 37 | bms_energy_buffer | 85 | 0 |
| 38 | bms_temp_pt_inlet | 86 | 0 |
| 39 | bms_temp_battery_inlet | 87 | 0 |
| 40 | bms_temp_inlet_target | 88 | 0 |
| 41 | bms_cell_max_temp | 89 | 0 |
| 42 | bms_cell_min_temp | 90 | 0 |
| 43 | bms_cell_max_voltage | 91 | 0 |
| 44 | bms_cell_min_voltage | 92 | 0 |
| 45 | bms_ac_charge_total | 93 | 0 |
| 46 | bms_dc_charge_total | 94 | 0 |
| 47 | bms_regen_total | 95 | 0 |
| 48 | bms_discharge_total | 112 | 0 |
| 49 | bms_battery_heating_state | 113 | 0 |
| 50 | drivetrain_front_torque | 114 | 0 |
| 51 | drivetrain_front_power | 115 | 0 |
| 52 | drivetrain_rear_torque | 116 | 0 |
| 53 | drivetrain_rear_power | 117 | 0 |
| 54 | drivetrain_rear_right_torque | 118 | 0 |
| 55 | drivetrain_rear_right_power | 119 | 0 |
| 56 | drivetrain_track_mode_stability | 120 | 0 |
| 57 | drivetrain_track_mode_handling | 121 | 0 |
| 58 | drivetrain_temp_front_stator | 122 | 0 |
| 59 | drivetrain_temp_rear_stator | 123 | 0 |
| 60 | drivetrain_inverters_count | 124 | 0 |
| 61 | driving_state_speed | 125 | 0 |
| 62 | driving_state_accel_pedal_pos | 126 | 0 |
| 63 | driving_state_turn_signal_left | 127 | 0 |
| 64 | driving_state_turn_signal_right | 144 | 0 |
| 65 | driving_state_brake_pressed | 145 | 0 |
| 66 | driving_state_gear | 146 | 0 |
| 67 | driving_state_regen_level | 147 | 0 |
| 68 | driving_state_drift_mode_state | 148 | 0 |
| 69 | driving_state_track_mode_state | 149 | 0 |
| 70 | driving_state_acceleration_mode | 150 | 0 |
| 71 | driving_state_motor_on_mode_state | 151 | 0 |
| 72 | driving_state_traction_control | 152 | 0 |
| 73 | driving_state_stopping_mode | 153 | 0 |
| 74 | driving_state_wiper_speed | 154 | 0 |
| 75 | charging_dc_current | 155 | 0 |
| 76 | charging_dc_voltage | 156 | 0 |
| 77 | charging_low_bus_voltage | 157 | 0 |
| 78 | charging_low_bus_current | 158 | 0 |
| 79 | charging_high_bus_voltage | 159 | 0 |
| 124 | drivetrain_ride_and_handling | 252 | 0 |
| 125 | sensors_front_left_brake_temp | 253 | 0 |
| 126 | sensors_front_right_brake_temp | 254 | 0 |
| 127 | sensors_rear_left_brake_temp | 255 | 0 |
| 128 | sensors_rear_right_brake_temp | 256 | 0 |

The high-number cases 124–128 are especially useful: their serializer constants are 2016, 2024, 2032, 2040 and 2176 respectively, giving protobuf fields 252, 253, 254, 255 and 272 where the serializer branch must be checked independently for the final case. The table above records the directly observed first key for cases 124–128 except that the last case requires one additional verification pass before promotion.

## 3. Typed raw values

DWARF/accessor evidence establishes:

- `PushVehicleDataHolder::bms_battery_voltage()` → `uint32`.
- `PushVehicleDataHolder::charging_dc_voltage()` → `int32`.
- `OptionalBmsBatteryVoltageCase::kBmsBatteryVoltage` = 28.
- `OptionalBmsCellMaxVoltageCase::kBmsCellMaxVoltage` = 43.

For `bms_battery_voltage`, the serializer path is therefore consistent with a protobuf varint carrying the raw `uint32` value. The observed key is `480 = 60 << 3`, so the generated protobuf field number is **60**, not 28. This is the concrete demonstration that semantic ID and protobuf field number are separate namespaces.

## 4. Important boundary

This layer is now substantially reconstructed:

`GetReqSubscribeVehicleData()` → repeated semantic-field request → `ReqSubscribeVehicleData` protobuf → protobuf field key/type → `PushVehicleDataHolder` protobuf serialization/deserialization → typed application value.

It is **not yet** a CAN decoder. No CAN arbitration ID, DLC, byte offset, bit offset, endianness, scale, offset or physical-unit conversion is promoted by this artifact.

## Evidence addresses

Native library: `libS3XYButtons_arm64-v8a.so`

- `CMessageHelper::GetReqSubscribeVehicleData(bool)`: `0x4b94afc`
- `ReqSubscribeVehicleData::_InternalSerialize`: `0x4abbe70`
- `RespSubscribeVehicleData::_InternalSerialize`: `0x4abcb7c`
- `PushVehicleDataHolder::_InternalSerialize`: `0x4ac3a8c`
- `PushVehicleDataHolder::GetMetadata()`: `0x4acf0b0`
- `CBLECommander::ProcessPushVehicleDataHolder(...)`: `0x47fcaf0`

Status: `SERIALIZATION_CORRELATED` for the protobuf layer; CAN mapping remains `UNVERIFIED`.
