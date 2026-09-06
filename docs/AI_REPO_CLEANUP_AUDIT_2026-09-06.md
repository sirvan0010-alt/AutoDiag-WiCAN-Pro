# Repository Cleanup Audit — 2026-09-06

## Status

Evidence-first cleanup audit. No production decoder, vehicle mapping, or verification status is changed by this audit.

## Verified

- The Android build is rooted at `android/settings.gradle.kts`.
- The active Android modules are `:app`, `:core`, and `:simulator`.
- Repository governance requires current-GitHub verification and prohibits irreversible cleanup based only on an unverified second-AI claim.
- Repository searches performed in this session found no textual references establishing a root-level `core/` tree as an active Gradle source set.

## Inference

The root-level `core/` tree is a strong duplicate/dead-code candidate because the active Gradle project is under `android/` and declares `:core`, which resolves to `android/core`.

This is an inference, not deletion authorization.

## Required before deletion

1. Enumerate every file under root `core/`.
2. Enumerate every file under `android/core`.
3. Compare paths and contents where names overlap.
4. Search repository imports/package names and build references.
5. Check Git history for provenance of root `core/`.
6. Confirm no scripts, CI, documentation generators, or packaging tasks consume root `core/`.
7. Only then consider deletion in a separate small commit.

## WiCAN capture track

WiCAN PRO supports ELM327 and `slcan/socketCAN`; recent firmware releases also document improved CAN-monitor/ATMA handling and ELM327 UDP logging. These external facts are contextual only and do not promote AutoDiag candidates to vehicle-verified status.

AutoDiag evidence chain remains:

`RAW CAPTURE -> STATIC EVIDENCE -> CANDIDATE -> MAPPING -> DECODER -> TESTED -> VEHICLE VERIFIED -> PRODUCTION`

No CAN ID, signal scale, Tesla signal, HV value, or Riso value is promoted merely because it appears in an APK or external firmware documentation.

## Next audit action

Continue root `core/` provenance/dependency enumeration and SLCAN capture architecture before destructive cleanup.
