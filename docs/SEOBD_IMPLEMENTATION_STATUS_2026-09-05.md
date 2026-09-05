# SEOBD implementation + research status — 2026-09-05

## Deep extraction completed in this pass

- Re-extracted the supplied XAPK locally from the conversation file.
- Verified XAPK SHA-256: `b6d5188457978b4a9fd0e4c138d316abb7144291ff0eda97b20678a135faddc`.
- Verified native library SHA-256: `0de3963bced2f22c859b673cc34eed7b7734925c9fd75a92b28a5ff1cb9079ca`.
- Native library size: 299,688,392 bytes, AArch64/ARM64.
- Captured ELF headers, sections, symbol table, demangled symbols, strings and DWARF information.
- Reconstructed a large semantic vehicle-data field inventory.
- Reconstructed 58 EV-relevant `SVDF_*` semantic field identifiers directly from DWARF.
- Proven examples include `bms_battery_voltage=28`, `bms_battery_current=29`, `bms_cell_max_voltage=43`, `charging_dc_voltage=76`.
- Proven protobuf accessor types include `bms_battery_voltage() -> uint32` and `charging_dc_voltage() -> int32`.
- Proven application-layer request/response/notification anchors: `GetReqSubscribeVehicleData`, `ReqSubscribeVehicleData`, `RespSubscribeVehicleData`, `PushVehicleDataHolder`, `ProcessSubscripVehicleDataRequest`, `ProcessPushVehicleDataHolder`.
- Added reproducible `tools/extract_seobd_evidence.py` so the extraction can be repeated against the same artifact.

## Existing implementation foundations

- Real `ObdLiveDataEngine` → Android bridge via `LiveDataViewModel`.
- Maximum 16 selectable Mode 01 PIDs retained at UI boundary.
- Read-only DTC/freeze-frame integration contract documented.
- ECU discovery/profile acceptance contract documented.
- EV health orchestration contract documented.
- Repair intelligence provenance chain documented.
- Pre-purchase state machine added.
- Profile-driven read-only AUTO TEST plan added.
- Read-only MQTT/Home Assistant telemetry contract added with per-signal rate limiting.

## Deliberately NOT promoted to verified

- CAN arbitration IDs from semantic field IDs.
- BLE characteristic UUIDs.
- Wire framing.
- Byte/bit offsets.
- Scaling and physical units for the reconstructed manufacturer-specific fields.
- Runtime vehicle measurements.
- Manufacturer-specific diagnostic profiles.
- Any write/control behavior.

## Next research gate

The next pass should trace **protobuf field serialization and native call sites**, then connect the semantic field IDs to transport framing and, only where possible, to concrete wire identifiers. Runtime captures/replay should then be used as independent corroboration.

## Quality rule

No inferred value becomes a live diagnostic measurement until its provenance is explicit and independently corroborated. Contracts remain contracts; static extraction remains evidence.
