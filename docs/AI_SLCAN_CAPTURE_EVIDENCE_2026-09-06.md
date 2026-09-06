# SLCAN capture evidence — 2026-09-06

## Verified implementation

- `WiCanTransport` exposes `observeIncoming(): Flow<ByteArray>` and explicitly supports `TransportMode.SLCAN_RAW`.
- `SlcanCanFrameStream` consumes transport byte chunks, feeds them to `SlcanCodec.StreamDecoder`, and exposes decoded `CanFrame` values as a `SharedFlow`.
- `SlcanCodec` validates classic SLCAN frame types `t/T/r/R`, identifier width, DLC, payload length, and hexadecimal payload bytes. Its stream decoder preserves partial TCP lines between chunks.
- `CanCapture` provides bounded in-memory capture with relative monotonic timestamps and an explicit dropped-record counter. Default capacity is 50,000 records.
- `SlcanCaptureController` explicitly wires the live `SlcanCanFrameStream` into `CanCapture`, with opt-in start/stop lifecycle and immutable `CanCaptureSession` output.
- `ConnectionViewModel` starts the SLCAN live stream for the real SLCAN transport and exposes capture lifecycle/session state to the Android layer.
- `CanCaptureJson` defines the versioned `autodiag-can-capture-v1` evidence format and supports encode/decode round trips.
- `CanCaptureArtifactStore` writes a complete capture session to UTF-8 JSON, providing the persistence boundary between live capture and replay analysis.
- `CanReplay` re-emits captured frames using their relative timing and supports replay speed scaling.
- `RawCanMonitorState` provides a UI-neutral live monitor state, filtering by CAN ID, pause handling, bounded visible-frame history, and bus statistics.

## Regression coverage

Tests now cover the complete application-side sequence:

`transport byte chunk → SLCAN decode → live frame stream → capture.record() → immutable session`

and the evidence/replay sequence:

`CanCaptureSession → JSON artifact → JSON decode → CanReplay.flow()`

The tests use fake/synthetic frames. They do not claim physical vehicle evidence.

The Android CI workflow runs the core unit tests and the application debug assembly on Android-source changes. A successful CI run is required before treating the changed source as build/test verified; source inspection alone is not treated as a passing build.

## Evidence artifact contract

Persisted artifacts use:

`format = autodiag-can-capture-v1`

Each record retains relative monotonic timestamp, CAN identifier, classic/extended flag, remote-frame flag, and payload bytes. Session metadata retains capture start timestamp, frame count, and dropped-record count. This makes the artifact suitable for independent replay without re-reading the live transport.

## Evidence boundary

These components prove the application-side SLCAN parsing/capture/persistence/replay path and its unit-test wiring. They do **not** prove that a physical WiCAN device is currently delivering CAN RX traffic to the application.

A transport connection or successful parser test is not vehicle evidence. Vehicle verification requires an actual captured raw frame session with source/transport metadata and subsequent replay/decoder tests.

## Current gate

The core implementation now covers:

`real WiCAN transport → real RX bytes → real capture session → persisted evidence artifact → replay → decoder test`

The remaining gate is physical/runtime acquisition and vehicle verification. The first real artifact must retain enough provenance to identify the transport/session and must not be promoted into a decoder or vehicle-verified signal mapping merely because its bytes parse successfully.

Do not promote any signal/PID/CAN mapping based solely on static APK extraction, parser tests, synthetic capture tests, replay tests, or a successful compilation.

## External WiCAN context

Official WiCAN documentation confirms WiCAN PRO supports `slcan/socketCAN`. A public firmware issue documents a case where WiCAN's own CAN monitor received live traffic while USB SLCAN forwarding delivered no frames to the host. This reinforces the requirement for end-to-end RX evidence rather than treating transport connection as proof of CAN data.
