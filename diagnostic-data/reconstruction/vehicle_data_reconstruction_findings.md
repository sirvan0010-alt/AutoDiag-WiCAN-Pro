# SEOBD — concrete reconstruction findings

These findings are stronger than string matches because they come from native symbols + DWARF type information.

## Finding A — semantic field enum exists

The native DWARF contains an enum named `SVDF_*` mapping vehicle-data semantic fields to numeric identifiers. Examples proven directly:

| Semantic field | SVDF value | Evidence class |
|---|---:|---|
| nav_car_latitude | 1 | DWARF enum |
| nav_car_longitude | 2 | DWARF enum |
| sensors_outside_temperature | 27 | DWARF enum |
| bms_battery_voltage | 28 | DWARF enum |
| bms_battery_current | 29 | DWARF enum |
| bms_charge_status | 30 | DWARF enum |
| bms_energy_buffer | 31 | DWARF enum |
| bms_temp_pt_inlet | 32 | DWARF enum |
| bms_nominal_remaining | 33 | DWARF enum |
| bms_nominal_full_pack_now | 34 | DWARF enum |
| bms_nominal_full_pack_new | 35 | DWARF enum |
| bms_ideal_energy_remaining | 36 | DWARF enum |
| bms_max_discharge_power | 37 | DWARF enum |
| bms_max_regen_power | 38 | DWARF enum |
| bms_battery_heating_state | 39 | DWARF enum |
| bms_cell_max_temp | 41 | DWARF enum |
| bms_cell_min_temp | 42 | DWARF enum |
| bms_cell_max_voltage | 43 | DWARF enum |
| bms_cell_min_voltage | 44 | DWARF enum |
| bms_ac_charge_total | 45 | DWARF enum |
| bms_dc_charge_total | 46 | DWARF enum |
| bms_discharge_total | 47 | DWARF enum |
| bms_regen_total | 48 | DWARF enum |
| charging_dc_current | 75 | DWARF enum |
| charging_dc_voltage | 76 | DWARF enum |
| charging_low_bus_voltage | 77 | DWARF enum |
| charging_low_bus_current | 78 | DWARF enum |

**Important:** these values are semantic field identifiers inside the vehicle-data API. They are **not yet proven CAN IDs** and must not be used as such.

## Finding B — generated protobuf accessors prove concrete field types

DWARF shows `PushVehicleDataHolder::bms_battery_voltage()` returning `uint32` through typedef `uint32`.

DWARF shows `PushVehicleDataHolder::charging_dc_voltage()` returning `int32` through typedef `int32`.

This is valuable because the reconstruction now has a semantic identifier + generated message accessor + scalar type, rather than only a string.

## Finding C — optional-field cases preserve the same semantic numbers

For `bms_battery_voltage`, the generated optional-case enum contains `kBmsBatteryVoltage = 28`.

For `bms_cell_max_voltage`, the generated optional-case enum contains `kBmsCellMaxVoltage = 43`.

This independently cross-checks the `SVDF_*` values for those fields.

## Finding D — subscription path exists

A native symbol `CMessageHelper::GetReqSubscribeVehicleData(bool)` exists, together with generated types:

- `ReqSubscribeVehicleData`
- `RespSubscribeVehicleData`
- `PushVehicleDataHolder`

and a processor:

- `CBLECommander::ProcessPushVehicleDataHolder(PushVehicleDataHolder const&)`

This establishes a concrete request → response/notification → data-holder chain at the application layer.

## Finding E — EV diagnostics target is unusually rich

The recovered field inventory contains battery voltage/current, cell min/max voltage, cell min/max temperature, charge state, charge totals, discharge/regen totals, nominal/ideal energy, charge DC current/voltage, low/high bus quantities and battery heating state.

These are high-priority reconstruction targets for SEOBD EV health, but their wire encoding still requires tracing serialization/transport and runtime capture.

## Verification status

`CANDIDATE_SEMANTIC_FIELD` — strong static evidence.

Not yet verified:

- CAN arbitration ID
- BLE characteristic UUID
- serialized field-number-to-wire-frame mapping beyond the semantic enum
- byte/bit offset inside a CAN payload
- scaling/offset
- physical unit conversion
- runtime value correctness

Therefore these findings are suitable for a reconstruction registry, not yet for an active manufacturer-specific decoder.
