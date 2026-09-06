# Repository Cleanup Audit — 2026-09-06

## Status

This document records an evidence-first cleanup audit. No production decoder, vehicle mapping, or verification status is changed by this audit.

## Verified

- The Android build is rooted at `android/settings.gradle.kts`.
- The active Android modules are `:app`, `:core`, and `:simulator`.
- Repository governance requires current-GitHub verification and prohibits irreversible cleanup based only on an unverified second-AI claim.
- Searches performed against the default branch found no repository references to `com.autodiag.wican.core` outside the expected module path and no textual references establishing the root-level `core/` tree as an active Gradle source set.

## Inference

The root-level `core/` tree is a strong duplicate/dead-code candidate because the active Gradle project is under `android/` and the declared module is `android/core`. This is an inference, not yet a deletion authorization.

## Required before deletion

1. Enumerate every file under root `core/`.
2. Enumerate every file under `android/core`.
3. Compare paths and contents where names overlap.
4. Search all repository text for imports/package names and build references.
5. Check Git history for provenance of root `core/`.
6. Confirm no scripts, CI, documentation generators, or packaging tasks consume root `core/`.
7. Only then consider deletion in a separate small commit.

## WiCAN capture track

The external WiCAN firmware documentation confirms WiCAN PRO supports both ELM327 and `slcan/socketCAN`; recent firmware also includes improved ATMA/CAN monitoring and ELM327 UDP logging. These external facts are contextual only and do not promote any AutoDiag candidate to vehicle-verified status.

The AutoDiag implementation must therefore keep the evidence chain:

`RAW CAPTURE -> STATIC EVIDENCE -> CANDIDATE -> MAPPING -> DECODER -> TESTED -> VEHICLE VERIFIED -> PRODUCTION`

No CAN ID, signal scale, Tesla signal, HV value, or Riso value is promoted merely because it appears in an APK or external firmware documentation.

## Next audit action

Continue with root `core/` provenance/dependency enumeration and the SLCAN capture architecture before making any destructive cleanup.
