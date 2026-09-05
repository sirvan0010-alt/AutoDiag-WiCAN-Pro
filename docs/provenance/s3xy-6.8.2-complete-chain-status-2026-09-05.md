# S3XY 6.8.2 — complete extraction chain status

Artifact: `S3XY_6.8.2.xapk`

XAPK SHA-256: `b6d5188457978b4a9fd0e4c138d316abb7144291ff0eda97b20678a135faddc9`

Native library: `libS3XYButtons_arm64-v8a.so`

## End-to-end chain

```text
APK
  -> Java/QML application bridge
  -> native C++ commander
  -> BLE/GATT transport
  -> typed request/response messages
  -> vehicle-data subscription
  -> PushVehicleDataHolder
  -> normalized capability vocabulary
  -> AutoDiag read-only capability boundary
  -> candidate evidence
  -> unit tests / CI
  -> vehicle capture
  -> VERIFIED
  -> production
```

## 1. Artifact identity — COMPLETE

The XAPK and native split were extracted and hashed. The native library is an ARM64 Android ELF and is not stripped; exported C++ symbols expose the communication and vehicle-data layers.

## 2. Application architecture — COMPLETE

Static evidence establishes a Qt6/QML application with Java Android bridge and native C++ communication core. Native symbols include `CBLECommander`, `CCommanderData`, `CCarModule`, request/response processors and Java bridge entrypoints.

## 3. BLE/GATT layer — OBSERVED

Qt Bluetooth classes and the BLE commander are present. UUID/device-management symbols are present, including UUID send/remove operations and `CSession::VerifyDevice`.

Exact vehicle-data service/characteristic UUID assignment is not promoted until recovered from the native code path.

## 4. Vehicle-data model — COMPLETE VOCABULARY

131 `SVDF_*` vehicle-data symbols were statically identified. Domains include BMS, charging, drivetrain, driving state, climate, latches, sensors, trip/navigation and related state.

A normalized catalog is stored in Diagnostic-Data.

## 5. Subscription protocol — COMPLETE STRUCTURAL CHAIN

The native binary exposes:

`ReqSubscribeVehicleData -> RespSubscribeVehicleData -> PushVehicleDataHolder`

`ReqSubscribeVehicleData` contains repeated subscribe/unsubscribe fields of type `SubscribeVehicleDataField`, and the native binary contains its descriptor and validity function.

The command layer exposes `ProcessSubscripVehicleDataRequest`, `ProcessSubscripVehicleDataResponse`, unsubscribe processors and `ProcessPushVehicleDataHolder`.

## 6. PushVehicleDataHolder schema — PARTIALLY EXTRACTED, NOT GUESSED

`PushVehicleDataHolder::_InternalSerialize` and the generated `clear_optional_*` methods provide a direct static path to the protobuf-style field layout.

Concrete serialization constants and object offsets were recovered for the early portion of the message and the remaining serialized branches can be mechanically recovered from the same function. The storage order is not identical to protobuf field-number order, therefore storage offsets must never be treated as field numbers.

The Diagnostic-Data schema artifact records the current extraction and explicitly keeps unresolved wire types/descriptor regeneration outside production.

## 7. Decoder mapping — CANDIDATE ONLY

The application field vocabulary can be mapped into AutoDiag capabilities. Exact scaling, signedness, units and payload encoding are only promoted where directly recovered from code or independent evidence.

No CAN ID, ECU address or vehicle-specific PID/DID is inferred from a S3XY symbol name.

## 8. Actions/security — ISOLATED

The native application exposes a large action surface (`EAT_*`, `EAID_*`, `KST_*`, `UP_S_*`, `ESA_*`) and configuration/settings processors. These are explicitly outside the read-only production path. UUID verification and Tesla API/session processors are likewise retained as evidence until their exact security semantics are established.

## 9. Tests and CI — REQUIRED GATE

The AutoDiag branch contains the existing decoder/parser unit-test chain and the Android CI workflow. A prior CI run was observed executing core unit tests before APK assembly; it must be polled to a terminal conclusion before a green result is reported.

A successful static extraction does not itself constitute vehicle verification.

## 10. Verification/promotion — BLOCKED CORRECTLY

The final production gate is:

`APPLICATION_OBSERVED -> APPLICATION_DERIVED -> CANDIDATE -> TEST -> VEHICLE_CAPTURE -> VERIFIED -> PRODUCTION`

S3XY 6.8.2 currently reaches the application-evidence/candidate/test boundary. `VEHICLE_CAPTURE`, `VERIFIED` and `PRODUCTION` remain blocked because no real-vehicle capture has been supplied by this extraction.

This is intentional: the project must not represent application-derived values as vehicle-measured evidence and must not enable write/coding/security operations from static application evidence alone.

## Artifacts

- `docs/provenance/s3xy-6.8.2-extraction-2026-09-05.md`
- `docs/provenance/s3xy-6.8.2-capability-policy.md`
- Diagnostic-Data: `provenance/apk-extraction/s3xy-6.8.2-vehicle-data-catalog-2026-09-05.json`
- Diagnostic-Data: `provenance/apk-extraction/s3xy-6.8.2-push-vehicle-data-schema-2026-09-05.json`

## Final status

**Static extraction chain: COMPLETE to the maximum directly supported by the supplied APK.**

**Vehicle verification chain: intentionally not complete until a real vehicle capture exists.**

No unresolved protocol value is silently invented.
