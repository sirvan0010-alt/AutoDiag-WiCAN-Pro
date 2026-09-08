# S3XY 6.8.2 vehicle-data surface

The embedded `enhapi_vehicle_data.proto` descriptor was recovered from the native binary and parsed as protobuf descriptor data. This upgrades the evidence from symbol presence to **exact protobuf field number + protobuf type + subscription enum value**. Units/scaling and vehicle/firmware scope are still not asserted.

## `PushVehicleDataHolder` fields

| field | protobuf # | type | subscription enum |
|---|---:|---|---:|
| nav_car_latitude | 1 | float | 1 |
| nav_car_longitude | 2 | float | 2 |
| nav_car_location_name | 3 | string | 3 |
| nav_destination_latitude | 4 | float | 4 |
| nav_destination_longitude | 5 | float | 5 |
| nav_destination_location_name | 6 | string | 6 |
| nav_distance_in_miles | 7 | float | 7 |
| nav_minutes_to_arrival | 8 | float | 8 |
| nav_energy_at_arrival | 9 | float | 9 |
| nav_traffic_minutes_delay | 10 | float | 10 |
| media_playback_status | 11 | enum `.MediaPlaybackStatus` | 11 |
| media_now_playing_source | 12 | enum `.MediaSourceType` | 12 |
| media_now_playing_artist | 13 | string | 13 |
| media_now_playing_title | 14 | string | 14 |
| media_now_playing_source_string | 15 | string | 15 |
| media_now_playing_album | 16 | string | 16 |
| media_now_playing_station | 17 | string | 17 |
| media_a2dp_source_name | 18 | string | 18 |
| media_now_playing_duration | 19 | int32 | 19 |
| media_now_playing_elapsed | 20 | int32 | 20 |
| display_brightness | 24 | uint32 | 24 |
| display_state | 25 | enum `.CarDisplayState` | 25 |
| display_theme | 26 | enum `.CarDisplayTheme` | 26 |
| sensors_outside_temperature | 27 | sint32 | 27 |
| bms_battery_voltage | 28 | uint32 | 28 |
| bms_battery_current | 29 | sint32 | 29 |
| bms_charge_status | 30 | uint32 | 30 |
| bms_ideal_energy_remaining | 31 | uint32 | 31 |
| bms_max_regen_power | 32 | uint32 | 32 |
| bms_max_discharge_power | 33 | uint32 | 33 |
| bms_nominal_full_pack_new | 34 | uint32 | 34 |
| bms_nominal_full_pack_now | 35 | uint32 | 35 |
| bms_nominal_remaining | 36 | uint32 | 36 |
| bms_energy_buffer | 37 | uint32 | 37 |
| bms_temp_pt_inlet | 38 | sint32 | 38 |
| bms_temp_battery_inlet | 39 | sint32 | 39 |
| bms_temp_inlet_target | 40 | sint32 | 40 |
| bms_cell_max_temp | 41 | sint32 | 41 |
| bms_cell_min_temp | 42 | sint32 | 42 |
| bms_cell_max_voltage | 43 | uint32 | 43 |
| bms_cell_min_voltage | 44 | uint32 | 44 |
| bms_ac_charge_total | 45 | uint32 | 45 |
| bms_dc_charge_total | 46 | uint32 | 46 |
| bms_regen_total | 47 | uint32 | 47 |
| bms_discharge_total | 48 | uint32 | 48 |
| bms_battery_heating_state | 49 | enum `.BatteryPreheatingState` | 49 |
| drivetrain_front_torque | 50 | sint32 | 50 |
| drivetrain_front_power | 51 | sint32 | 51 |
| drivetrain_rear_torque | 52 | sint32 | 52 |
| drivetrain_rear_power | 53 | sint32 | 53 |
| drivetrain_rear_right_torque | 54 | sint32 | 54 |
| drivetrain_rear_right_power | 55 | sint32 | 55 |
| drivetrain_track_mode_stability | 56 | enum `.DashTrackModeStability` | 56 |
| drivetrain_track_mode_handling | 57 | enum `.DashTrackModeHandling` | 57 |
| drivetrain_temp_front_stator | 58 | sint32 | 58 |
| drivetrain_temp_rear_stator | 59 | sint32 | 59 |
| drivetrain_inverters_count | 60 | uint32 | 60 |
| driving_state_speed | 61 | uint32 | 61 |
| driving_state_accel_pedal_pos | 62 | uint32 | 62 |
| driving_state_turn_signal_left | 63 | enum `.TurnSignalStatus` | 63 |
| driving_state_turn_signal_right | 64 | enum `.TurnSignalStatus` | 64 |
| driving_state_brake_pressed | 65 | bool | 65 |
| driving_state_gear | 66 | uint32 | 66 |
| driving_state_regen_level | 67 | uint32 | 67 |
| driving_state_drift_mode_state | 68 | uint32 | 68 |
| driving_state_track_mode_state | 69 | uint32 | 69 |
| driving_state_acceleration_mode | 70 | uint32 | 70 |
| driving_state_motor_on_mode_state | 71 | uint32 | 71 |
| driving_state_traction_control | 72 | enum `.DashTractionControl` | 72 |
| driving_state_stopping_mode | 73 | enum `.DashStoppingMode` | 73 |
| driving_state_wiper_speed | 74 | uint32 | 74 |
| charging_dc_current | 75 | sint32 | 75 |
| charging_dc_voltage | 76 | sint32 | 76 |
| charging_low_bus_voltage | 77 | sint32 | 77 |
| charging_low_bus_current | 78 | sint32 | 78 |
| charging_high_bus_voltage | 79 | sint32 | 79 |
| lights_status_drl | 80 | uint32 | 80 |
| lights_status_low_beam | 81 | uint32 | 81 |
| lights_status_high_beam | 82 | uint32 | 82 |
| lights_status_fog_front | 83 | uint32 | 83 |
| lights_status_fog_rear | 84 | uint32 | 84 |
| lights_status_park | 85 | uint32 | 85 |
| lights_status_auto_high_beam_enabled | 86 | uint32 | 86 |
| lights_status_auto_lights | 87 | enum `.LightSwitchStatus` | 87 |
| climate_fan_speed | 88 | uint32 | 88 |
| climate_bioweapon_defence | 89 | uint32 | 89 |
| climate_keeper_mode | 90 | enum `.ClimateKeeperMode` | 90 |
| climate_hvac_on | 91 | uint32 | 91 |
| climate_vent_windows | 92 | uint32 | 92 |
| climate_heated_seats_fl | 93 | uint32 | 93 |
| climate_heated_seats_fr | 94 | uint32 | 94 |
| climate_heated_seats_rl | 95 | uint32 | 95 |
| climate_heated_seats_rc | 96 | uint32 | 96 |
| climate_heated_seats_rr | 97 | uint32 | 97 |
| climate_defog_defrost | 98 | enum `.DashDefogDefrost` | 98 |
| climate_steering_wheel_heater | 99 | uint32 | 99 |
| climate_rear_vent_toggle | 100 | enum `.DashDefaultMode` | 100 |
| climate_recirculation_toggle | 101 | enum `.DashRecirc` | 101 |
| climate_ac_toggle | 102 | enum `.DashDefaultMode` | 102 |
| climate_seat_cooling_fl | 103 | uint32 | 103 |
| climate_seat_cooling_fr | 104 | uint32 | 104 |
| autopilot_current_state | 105 | uint32 | 105 |
| autopilot_blind_spot_rear_left | 106 | enum `.BlindSpotState` | 106 |
| autopilot_blind_spot_rear_right | 107 | enum `.BlindSpotState` | 107 |
| autopilot_hands_on_state | 108 | enum `.AutopilotHandsOnState` | 108 |
| autopilot_follow_distance | 109 | uint32 | 109 |
| autopilot_speed_limit | 110 | uint32 | 110 |
| trip_data_usoe | 111 | uint32 | 111 |
| trip_data_range | 112 | uint32 | 112 |
| latch_status_front_left_door | 113 | enum `.LatchStatus` | 113 |
| latch_status_front_right_door | 114 | enum `.LatchStatus` | 114 |
| latch_status_rear_left_door | 115 | enum `.LatchStatus` | 115 |
| latch_status_rear_right_door | 116 | enum `.LatchStatus` | 116 |
| latch_status_frunk | 117 | enum `.LatchStatus` | 117 |
| latch_status_trunk | 118 | enum `.LatchStatus` | 118 |
| latch_status_car_locked | 119 | uint32 | 119 |
| latch_status_child_unlock_left | 120 | uint32 | 120 |
| latch_status_child_unlock_right | 121 | uint32 | 121 |
| others_hand_wash_state | 122 | uint32 | 122 |
| others_current_time_in_seconds | 123 | sint64 | 123 |
| drivetrain_ride_and_handling | 124 | uint32 | 124 |
| sensors_front_left_brake_temp | 125 | sint32 | 125 |
| sensors_front_right_brake_temp | 126 | sint32 | 126 |
| sensors_rear_left_brake_temp | 127 | sint32 | 127 |
| sensors_rear_right_brake_temp | 128 | sint32 | 128 |
| driver_or_passenger_present | 129 | bool | 129 |
| next_id | 130 | uint32 | 130 |

## Notable structural findings

- The vehicle-data descriptor contains **127 fields** in `PushVehicleDataHolder`.
- `SubscribeVehicleDataField` enumerates the same vehicle-data namespace and includes values 1–130 with deliberate gaps for fields not represented in this holder (notably media audio-volume fields 21–23).
- Many fields are proto3 `optional` fields implemented through synthetic oneofs; presence therefore matters and must not be converted to zero/default values by AutoDiag.
- The four brake-temperature fields use protobuf numbers 125–128 even though they occur earlier in the descriptor than the BMS block.

## AutoDiag verification status

**Schema: verified from embedded descriptor.**

**Vehicle meaning / units / scaling: unverified.** The descriptor establishes wire-level names, numbers and protobuf types. It does not establish the physical source, unit, scale, freshness, ECU origin or vehicle/firmware coverage. Those remain evidence-gated per `CAPABILITY_DISCOVERY.md`.
