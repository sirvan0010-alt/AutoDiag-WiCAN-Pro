# S3XY 6.8.2 vehicle-data surface

`PushVehicleDataHolder` exposes optional vehicle-data fields through protobuf-generated native symbols. This is a capability/field inventory only; exact field numbers, units, scaling and vehicle availability require descriptor/runtime evidence and are not guessed here.

## Battery / BMS
- `battery_current`
- `battery_voltage`
- `charge_status`
- `ideal_energy_remaining`
- `max_regen_power`
- `max_discharge_power`
- `nominal_full_pack_new`
- `nominal_full_pack_now`
- `nominal_remaining`
- `energy_buffer`
- `temp_pt_inlet`
- `temp_battery_inlet`
- `temp_inlet_target`
- `cell_max_temp`
- `cell_min_temp`
- `cell_max_voltage`
- `cell_min_voltage`
- `ac_charge_total`
- `dc_charge_total`
- `regen_total`
- `discharge_total`
- `battery_heating_state`

## Charging
- `dc_current`
- `dc_voltage`
- `low_bus_voltage`
- `low_bus_current`
- `high_bus_voltage`

## Drivetrain
- `front_torque`
- `front_power`
- `rear_torque`
- `rear_power`
- `rear_right_torque`
- `rear_right_power`
- `track_mode_stability`
- `track_mode_handling`
- `temp_front_stator`
- `temp_rear_stator`
- `inverters_count`
- `ride_and_handling`

## Driving state
- `speed`
- `accel_pedal_pos`
- `turn_signal_left`
- `turn_signal_right`
- `brake_pressed`
- `gear`
- `regen_level`
- `drift_mode_state`
- `track_mode_state`
- `acceleration_mode`
- `motor_on_mode_state`
- `traction_control`
- `stopping_mode`
- `wiper_speed`

## Climate
- `fan_speed`
- `bioweapon_defence`
- `keeper_mode`
- `hvac_on`
- `vent_windows`
- `heated_seats_fl`
- `heated_seats_fr`
- `heated_seats_rl`
- `heated_seats_rc`
- `heated_seats_rr`
- `defog_defrost`
- `steering_wheel_heater`
- `rear_vent_toggle`
- `recirculation_toggle`
- `ac_toggle`
- `seat_cooling_fl`
- `seat_cooling_fr`

## Autopilot
- `current_state`
- `blind_spot_rear_left`
- `blind_spot_rear_right`
- `hands_on_state`
- `follow_distance`
- `speed_limit`

## Latches / doors
- `front_left_door`
- `front_right_door`
- `rear_left_door`
- `rear_right_door`
- `frunk`
- `trunk`
- `car_locked`
- `child_unlock_left`
- `child_unlock_right`

## Lights
- `drl`
- `low_beam`
- `high_beam`
- `fog_front`
- `fog_rear`
- `park`
- `auto_high_beam_enabled`
- `auto_lights`

## Sensors / trip / other
- outside temperature
- front-left/right and rear-left/right brake temperatures
- trip range
- trip USOE
- hand-wash state
- current time in seconds
- driver/passenger present
- navigation car/destination coordinates and location names
- navigation distance, ETA, energy-at-arrival and traffic delay
- media playback/source/artist/title/album/station/duration/elapsed
- display brightness/state/theme
- `next_id`

## AutoDiag status

**Evidence status: unverified capability surface.** The symbols prove that the application has a model for these fields. They do not prove that every Tesla vehicle exposes every field, nor do they establish decoding, units or scaling. Capability state must remain vehicle/firmware scoped according to `docs/CAPABILITY_DISCOVERY.md`.
