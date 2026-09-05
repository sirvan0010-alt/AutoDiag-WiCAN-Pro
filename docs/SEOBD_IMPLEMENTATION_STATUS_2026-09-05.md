# SEOBD implementation status — 2026-09-05

## Completed in this implementation pass

- Real `ObdLiveDataEngine` → Android bridge via `LiveDataViewModel`.
- Maximum 16 selectable Mode 01 PIDs retained at UI boundary.
- Poll plans are produced by the existing core policy layer.
- Read-only DTC/freeze-frame integration contract documented.
- ECU discovery/profile acceptance contract documented.
- EV health orchestration contract documented.
- Repair intelligence provenance chain documented.
- Pre-purchase state machine added.
- Profile-driven read-only AUTO TEST plan added.
- Read-only MQTT/Home Assistant telemetry contract added with per-signal rate limiting.

## Still deliberately pending

- Wiring `LiveDataViewModel` into the existing `MainActivity` navigation after a concrete UI integration point is selected.
- Full DTC/freeze-frame screen backed by the repository's existing DTC store and evidence models.
- Physical/functional ECU discovery executors.
- Verified manufacturer CAN signal databases.
- Concrete EV health stage executors using vehicle-specific evidence.
- Repair source ingestion and price/labor providers.
- Report persistence/export.
- Actual MQTT client transport and Home Assistant discovery payloads.
- CI verification of the complete Android build after the new files are compiled.

No pending item is represented as completed merely because its contract exists.
