# Repository Cleanup Audit — 2026-09-06

## Status

Evidence-first cleanup audit. No production decoder, vehicle mapping, or verification status is changed by this audit.

## Current HEAD

`main` currently points to `ab768a79c120b46fcf24475dc140bb094642bd67`.

## Verified

- The Android build is rooted at `android/settings.gradle.kts` and declares `:app`, `:core`, and `:simulator`.
- The active Android core is under `android/core/src/main/java/com/autodiag/core` and uses package `com.autodiag.core`.
- The root-level `core/` tree is separate and uses package `com.autodiag.wican.core`.
- The root-level tree contains 12 Kotlin source files across automation, CAN, capture, diagnostics, OBD and transport.
- Repository search found no current textual imports/references of `com.autodiag.wican.core`.
- Git history shows recent `feat(core): ...` commits touching the root `core/` path on 2026-09-05.
- The active Android core already contains SLCAN/CAN capture infrastructure including `SlcanCodec.kt`, `SlcanCanFrameStream.kt`, `CanCapture.kt`, `CanCaptureCsv.kt`, `CanReplay.kt`, `RawCanMonitorState.kt`, plus corresponding tests.

## Correct conclusion about root `core/`

The root-level `core/` tree is **not an active Gradle module**, but it also cannot currently be classified as disposable historical debris. It is a distinct implementation that was modified by recent commits and therefore requires provenance/build-intent resolution.

**Disposition: `BLOCKED: provenance/build-intent resolution required`.**

Do not delete it merely because the Android settings do not include it.

## Required next actions

1. Inspect the recent root-core commits and their diffs.
2. Compare semantic ownership of root-core CAN/capture/diagnostics/transport code against `android/core`.
3. Determine whether unique root-core work was intended for migration, archival or abandonment.
4. Search CI/scripts/docs for path-based references.
5. If proven obsolete, delete or archive it in a dedicated cleanup commit. Otherwise migrate any unique useful implementation into the active Android module first.

## SLCAN finding

The active Android core already has a SLCAN parser/stream/capture/replay foundation. The next engineering gate is **raw transport capture → immutable evidence → deterministic replay**, not another parallel parser.

`SlcanCodec` accepts classic SLCAN frame types `t/T/r/R`, validates identifier width, DLC and payload length, supports remote frames and preserves partial TCP chunks through `StreamDecoder`. This proves parser behavior only; it does not prove vehicle reception or signal meaning.

External WiCAN issue #739 documents a case where WiCAN CAN Monitor showed live traffic while USB SLCAN host RX was empty. Therefore:

`transport connected != CAN RX proven`

and

`SLCAN parser accepted frame != vehicle signal proven`.

## Governance

`RAW CAPTURE -> STATIC EVIDENCE -> CANDIDATE -> MAPPING -> DECODER -> TESTED -> VEHICLE VERIFIED -> PRODUCTION`

No CAN ID, signal scale, Tesla mapping, HV value or Riso value is promoted merely from APK evidence or external firmware documentation.
