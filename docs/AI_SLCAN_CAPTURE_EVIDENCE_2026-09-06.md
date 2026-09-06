# SLCAN capture evidence — 2026-09-06

## Verified implementation

- `WiCanTransport` exposes `observeIncoming(): Flow<ByteArray>` and explicitly supports `TransportMode.SLCAN_RAW`.
- `SlcanCanFrameStream` consumes transport byte chunks, feeds them to `SlcanCodec.StreamDecoder`, and exposes decoded `CanFrame` values as a `SharedFlow`.
- `SlcanCodec` validates classic SLCAN frame types `t/T/r/R`, identifier width, DLC, payload length, and hexadecimal payload bytes. Its stream decoder preserves partial TCP lines between chunks.
- `CanCapture` provides bounded in-memory capture with relative monotonic timestamps and an explicit dropped-record counter. Default capacity is 50,000 records.
- `SlcanCaptureController` now explicitly wires the live `SlcanCanFrameStream` into `CanCapture`, with opt-in start/stop lifecycle and immutable `CanCaptureSession` output.
- `CanReplay` re-emits captured frames using their relative timing and supports replay speed scaling.
- `RawCanMonitorState` provides a UI-neutral live monitor state, filtering by CAN ID, pause handling, bounded visible-frame history, and bus statistics.

## Regression coverage

`SlcanCaptureControllerTest` verifies the complete application-side sequence:

`transport byte chunk → SLCAN decode → live frame stream → capture.record() → immutable session`

The test uses a fake transport and a known SLCAN frame. It does not claim physical vehicle evidence.

## Evidence boundary

These components prove the application-side SLCAN parsing/capture/replay path and its unit-test wiring. They do **not** prove that a physical WiCAN device is currently delivering CAN RX traffic to the application.

A transport connection or successful parser test is not vehicle evidence. Vehicle verification requires an actual captured raw frame session with source/transport metadata and subsequent replay/decoder tests.

## Current gate

The previous production-wiring gap is closed at the core-library level by `SlcanCaptureController` and its regression test. The remaining evidence gate is physical/runtime:

`real WiCAN transport → real RX bytes → real capture session → persisted evidence artifact → replay → decoder test → vehicle verification`

Do not promote any signal/PID/CAN mapping based solely on static APK extraction, parser tests, or synthetic capture tests.

## External WiCAN context

Official WiCAN documentation confirms WiCAN PRO supports `slcan/socketCAN`. A public firmware issue documents a case where WiCAN's own CAN monitor received live traffic while USB SLCAN forwarding delivered no frames to the host. This reinforces the requirement for end-to-end RX evidence rather than treating transport connection as proof of CAN data.
