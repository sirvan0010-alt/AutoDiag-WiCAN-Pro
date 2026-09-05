# SEOBD — concrete reconstruction findings

These findings are stronger than string matches because they come from native symbols + DWARF type information.

## Finding A — semantic field enum exists

The native DWARF contains an enum named `SVDF_*` mapping vehicle-data semantic fields to numeric identifiers. The EV-relevant block below was reconstructed directly:

| Semantic field | SVDF value | Evidence class |
|---|---:|---|
| sensors_outside_temperature | 27 | DWARF enum |
| bms_battery_voltage | 28 | DWARF enum |
| bms_battery_current | 29 | DWARF enum |
| bms_charge_status | 30 | DWARF enum |
| bms_ideal_energy_remaining | 31 | DWARF enum |
| bms_max_regen_power | 32 | DWARF enum |
| bms_max_discharge_power | 33 | DWARF enum |
| bms_nominal_full_pack_new | 34 | DWARF enum |
| bms_nominal_full_pack_now | 35 | DWARF enum |
| bms_nominal_remaining | 36 | DWARF enum |
| bms_energy_buffer | 37 | DWARF enum |
| bms_temp_pt_inlet | 38 | DWARF enum |
| bms_temp_battery_inlet | 39 | DWARF enum |
| bms_temp_inlet_target | 40 | DWARF enum |
| bms_cell_max_temp | 41 | DWARF enum |
| bms_cell_min_temp | 42 | DWARF enum |
| bms_cell_max_voltage | 43 | DWARF enum |
| bms_cell_min_voltage | 44 | DWARF enum |
| bms_ac_charge_total | 45 | DWARF enum |
| bms_dc_charge_total | 46 | DWARF enum |
| bms_regen_total | 47 | DWARF enum |
| bms_discharge_total | 48 | DWARF enum |
| bms_battery_heating_state | 49 | DWARF enum |
| drivetrain_front_torque | 50 | DWARF enum |
| drivetrain_front_power | 51 | DWARF enum |
| drivetrain_rear_torque | 52 | DWARF enum |
| drivetrain_rear_power | 53 | DWARF enum |
| drivetrain_rear_right_torque | 54 | DWARF enum |
| drivetrain_rear_right_power | 55 | DWARF enum |
| drivetrain_track_mode_stability | 56 | DWARF enum |
| drivetrain_track_mode_handling | 57 | DWARF enum |
| drivetrain_temp_front_stator | 58 | DWARF enum |
| drivetrain_temp_rear_stator | 59 | DWARF enum |
| drivetrain_inverters_count | 60 | DWARF enum |
| driving_state_speed | 61 | DWARF enum |
| driving_state_accel_pedal_pos | 62 | DWARF enum |
| driving_state_turn_signal_left | 63 | DWARF enum |
| driving_state_turn_signal_right | 64 | DWARF enum |
| driving_state_brake_pressed | 65 | DWARF enum |
| driving_state_gear | 66 | DWARF enum |
| driving_state_regen_level | 67 | DWARF enum |
| driving_state_drift_mode_state | 68 | DWARF enum |
| driving_state_track_mode_state | 69 | DWARF enum |
| driving_state_acceleration_mode | 70 | DWARF enum |
| driving_state_motor_on_mode_state | 71 | DWARF enum |
| driving_state_traction_control | 72 | DWARF enum |
| driving_state_stopping_mode | 73 | DWARF enum |
| driving_state_wiper_speed | 74 | DWARF enum |
| charging_dc_current | 75 | DWARF enum |
| charging_dc_voltage | 76 | DWARF enum |
| charging_low_bus_voltage | 77 | DWARF enum |
| charging_low_bus_current | 78 | DWARF enum |
| charging_high_bus_voltage | 79 | DWARF enum |
| drivetrain_ride_and_handling | 124 | DWARF enum |
| sensors_front_left_brake_temp | 125 | DWARF enum |
| sensors_front_right_brake_temp | 126 | DWARF enum |
| sensors_rear_left_brake_temp | 127 | DWARF enum |
| sensors_rear_right_brake_temp | 128 | DWARF enum |

**Important:** these values are semantic field identifiers inside the vehicle-data API. They are **not CAN IDs** and must not be used as such.

## Finding B — generated protobuf accessors prove concrete field types

DWARF shows `PushVehicleDataHolder::bms_battery_voltage()` returning `uint32` through typedef `uint32`.

DWARF shows `PushVehicleDataHolder::charging_dc_voltage()` returning `int32` through typedef `int32`.

This gives semantic identifier + generated message accessor + scalar type.

## Finding C — optional-field cases cross-check semantic numbers

For `bms_battery_voltage`, the generated optional-case enum contains `kBmsBatteryVoltage = 28`.

For `bms_cell_max_voltage`, the generated optional-case enum contains `kBmsCellMaxVoltage = 43`.

These independently corroborate the semantic field IDs.

## Finding D — subscription path exists

Native symbols prove the presence of:

- `CMessageHelper::GetReqSubscribeVehicleData(bool)`
- `ReqSubscribeVehicleData`
- `RespSubscribeVehicleData`
- `PushVehicleDataHolder`
- `CBLECommander::ProcessSubscripVehicleDataRequest(...)`
- `CBLECommander::ProcessPushVehicleDataHolder(PushVehicleDataHolder const&)`
- `CBLECommander::slotDataPushReceived(QString const&, QByteArray const&)`

This establishes a concrete application-layer request → response/notification → data-holder path.

## Finding E — EV diagnostics target is unusually rich

The recovered semantic block contains battery voltage/current, cell min/max voltage, cell min/max temperature, charge state, charge totals, discharge/regen totals, nominal/ideal energy, charge DC current/voltage, low/high bus quantities, battery heating and drivetrain telemetry.

## Verification status

`CORROBORATED_SEMANTIC_FIELD` = strong static evidence only.

Still unresolved:

- CAN arbitration ID
- BLE characteristic UUID
- wire framing
- serialization mapping to transport payload
- byte/bit offset in a CAN frame
- scaling/offset
- physical unit conversion
- runtime value correctness

The data is therefore suitable for continued decoder reconstruction, not yet for an active manufacturer-specific decoder.
