# S3XY 6.8.2 — AutoDiag extraction record

Status: **initial extraction pass completed**

Input: `S3XY_6.8.2.xapk`
Package: `com.enhauto.buttons`
App label: `S3XY`
Native UI/runtime: Qt 6.8.2

## Scope

This extraction follows the AutoDiag evidence model: identify transport, data surfaces, security boundaries, read/write capabilities and provenance without inventing CAN IDs, PID meanings, units, thresholds or vehicle support.

## Key finding

The APK is not a simple Java/Kotlin application. The primary application logic is a large AArch64 native Qt library (`libS3XYButtons_arm64-v8a.so`) with DWARF debug information, exported symbols and generated protobuf code. The binary exposes a substantial Tesla vehicle-data model, BLE transport/session layer, Tesla API session flow, and separate OTA/write paths.

## Files

- `HASHES.md` — cryptographic hashes of the supplied container and contained APKs.
- `INVENTORY.md` — package/container inventory and manifest identifiers.
- `NATIVE_BINARY.md` — native binary provenance and symbol/debug characteristics.
- `SOURCE_MAP.md` — embedded build-time source-path map.
- `VEHICLE_DATA_SURFACE.md` — observed optional vehicle-data fields.
- `PROTOCOL_SURFACE.md` — transport, session, protobuf and write-surface evidence.

## Verification status

All vehicle-specific mappings remain **unverified** until a field's descriptor, runtime message flow and real/simulator evidence establish field number, type, unit, scaling and scope. Presence of a symbol is evidence of implementation surface, not proof that a particular Tesla generation exposes the value.

No proprietary source code is copied into this report. The report records independently useful interfaces, names, structures and evidence for AutoDiag reimplementation.
