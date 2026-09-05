# SEOBD — deep extraction record 01–12

Date: 2026-09-05
Source: `S3XY_6.8.2.xapk`
Native focus: `libS3XYButtons_arm64-v8a.so`

## Evidence inventory

Static analysis of the native library produced approximately **304,038 symbol records**, **84,304 feature/project candidate symbols**, **3,244 embedded source/QML-like paths**, **109,837 diagnostic-related string candidates**, and **2,206 protobuf/generated-message candidates**. These are inventory counts, not claims that every item is diagnostically relevant.

## 01 — ELF metadata
- ARM64/AArch64 Android native library.
- Native file size: 299,688,392 bytes.
- Debug/source information is sufficiently rich to expose application-level names.
- Exact source hash is recorded in `hash_manifest.txt`.

## 02 — Symbol graph
- Demangled symbols expose application modules, data holders, BLE components and generated protobuf routines.
- High-value names include battery preheat state, vehicle-data subscriptions, dashboard data filling, BLE device checking and Tesla BLE data types.
- Symbols are evidence anchors; they are not executable SEOBD protocol definitions.

## 03 — Source paths
- Thousands of embedded source/QML-like path strings were recovered.
- Module boundaries can therefore be reconstructed before looking for individual signals.
- Paths will be normalized into module/source indexes rather than copied source code.

## 04 — Protobuf/type system
- Generated-message signatures include descriptors, field validators, default instances and serialization/parsing routines.
- Concrete message families include vehicle-data subscription request/response candidates and push-notification data holders.
- Next step is field-number/type reconstruction and cross-reference to call sites.

## 05 — Transport
- BLE/GATT and transport-related symbols are present.
- CAN-related terminology is present in the native evidence.
- Transport identification alone does not prove a CAN identifier or payload format.

## 06 — Request builders
- `Req*`, subscribe and command-like generated types provide candidate request entry points.
- Requests must be connected to actual call sites and serialized fields before being entered into a diagnostic registry.

## 07 — Response parsers
- `Resp*`, notification and vehicle-data holder symbols provide candidate response paths.
- A response becomes useful only when its identifier/message type and field mapping can be followed end-to-end.

## 08 — Data models
- Vehicle, dashboard, battery and charging data holders are visible.
- These are especially valuable because they can reveal semantic field names even where wire-level identifiers are obscure.

## 09 — Signal decoders
- Candidate signals must be reconstructed as: request → response → identifier/type → byte/bit field → mask → signedness → scaling/offset → unit → semantic name.
- No name-only match is promoted to VERIFIED.

## 10 — Automation/state machines
- Action/settings modules expose a large automation surface: locks, doors, trunk/frunk, climate, charging, lights, windows, mirrors, seats, driver-assistance and related states.
- These findings are intentionally kept separate from SEOBD diagnostic writes.

## 11 — Candidate registries
- The extraction pipeline now has a machine-readable manifest and evidence-oriented output locations.
- Candidate registries retain provenance and confidence.
- `CANDIDATE` is the default for reconstructed-but-unverified relationships.

## 12 — Verification gate
A signal can become VERIFIED only when the complete relationship is supported by independent evidence. Until then:

- no invented live measurement;
- no invented CAN ID;
- no invented scaling/unit;
- no write/control activation;
- no manufacturer-specific profile presented as universal;
- no vendor binary/source copied into the SEOBD implementation.

## Current conclusion
The deep extraction is worthwhile. The native artifact contains substantially more structured evidence than a simple string scan. The next pass should therefore prioritize **protobuf field reconstruction, call-site tracing, transport entry points, request/response pairing, and decoder reconstruction**, rather than adding more UI based on assumptions.
