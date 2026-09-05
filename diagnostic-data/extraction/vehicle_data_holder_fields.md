# Reconstructed vehicle-data field inventory

Source evidence: native symbol names for `PushVehicleDataHolder::clear_optional_*` in the analyzed ARM64 library.

**127 distinct optional field names were recovered.** This is a semantic field inventory, not a CAN decoder. No field below has an inferred CAN ID, byte offset or scaling unless separately proven.

## BMS / battery
- bms_battery_current
- bms_battery_voltage
- bms_battery_heating_state
- bms_cell_max_temp / bms_cell_min_temp
- bms_cell_max_voltage / bms_cell_min_voltage
- bms_charge_status
- bms_ac_charge_total / bms_dc_charge_total
- bms_discharge_total / bms_regen_total
- bms_energy_buffer
- bms_ideal_energy_remaining
- bms_nominal_remaining
- bms_nominal_full_pack_new / bms_nominal_full_pack_now
- bms_max_discharge_power / bms_max_regen_power
- bms_temp_battery_inlet / bms_temp_inlet_target / bms_temp_pt_inlet

## Charging
- charging_dc_current
- charging_dc_voltage
- charging_high_bus_voltage
- charging_low_bus_current
- charging_low_bus_voltage

## Drivetrain
- drivetrain_front_power / drivetrain_rear_power
- drivetrain_front_torque / drivetrain_rear_torque
- drivetrain_rear_right_power / drivetrain_rear_right_torque
- drivetrain_inverters_count
- drivetrain_temp_front_stator / drivetrain_temp_rear_stator
- drivetrain_ride_and_handling
- drivetrain_track_mode_handling / drivetrain_track_mode_stability

## Driving state
- driving_state_speed
- driving_state_accel_pedal_pos
- driving_state_brake_pressed
- driving_state_gear
- driving_state_regen_level
- driving_state_stopping_mode
- driving_state_traction_control
- driving_state_acceleration_mode
- driving_state_motor_on_mode_state
- driving_state_drift_mode_state
- driving_state_track_mode_state
- driving_state_turn_signal_left / driving_state_turn_signal_right
- driving_state_wiper_speed

## Climate
- climate_hvac_on
- climate_ac_toggle
- climate_fan_speed
- climate_keeper_mode
- climate_defog_defrost
- climate_bioweapon_defence
- climate_recirculation_toggle
- climate_rear_vent_toggle
- climate_vent_windows
- climate_heated_seats_fl / fr / rl / rr / rc
- climate_seat_cooling_fl / fr
- climate_steering_wheel_heater

## Latches / body
- latch_status_car_locked
- latch_status_frunk / latch_status_trunk
- latch_status_front_left_door / latch_status_front_right_door
- latch_status_rear_left_door / latch_status_rear_right_door
- latch_status_child_unlock_left / latch_status_child_unlock_right

## Lights
- lights_status_auto_lights
- lights_status_auto_high_beam_enabled
- lights_status_drl
- lights_status_fog_front / lights_status_fog_rear
- lights_status_high_beam / lights_status_low_beam
- lights_status_park

## Autopilot / assistance
- autopilot_current_state
- autopilot_hands_on_state
- autopilot_follow_distance
- autopilot_speed_limit
- autopilot_blind_spot_rear_left / autopilot_blind_spot_rear_right

## Sensors / navigation / display / media
- sensors_outside_temperature
- sensors_front_left_brake_temp / sensors_front_right_brake_temp
- sensors_rear_left_brake_temp / sensors_rear_right_brake_temp
- nav_car_latitude / nav_car_longitude / nav_car_location_name
- nav_destination_latitude / nav_destination_longitude / nav_destination_location_name
- nav_distance_in_miles / nav_energy_at_arrival / nav_minutes_to_arrival
- nav_traffic_minutes_delay
- display_state / display_theme / display_brightness
- media_playback_status
- media_now_playing_album / artist / title / source / source_string / station
- media_now_playing_duration / elapsed
- media_a2dp_source_name
- driver_or_passenger_present
- others_current_time_in_seconds
- others_hand_wash_state
- next_id

## Diagnostic significance
The BMS, charging, drivetrain, driving-state and sensor groups are high-value targets for the next reconstruction pass. The immediate objective is to locate the message field definitions and call sites that connect these semantic fields to serialized protobuf field numbers and then to the underlying transport. Only after that should a decoder candidate be created.
