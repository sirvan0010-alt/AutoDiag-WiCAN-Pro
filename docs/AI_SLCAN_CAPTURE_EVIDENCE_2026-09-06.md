# SLCAN capture evidence — 2026-09-06

## Verified implementation

- `WiCanTransport` exposes `observeIncoming(): Flow<ByteArray>` and explicitly supports `TransportMode.SLCAN_RAW`.
- `SlcanCanFrameStream` consumes transport byte chunks, feeds them to `SlcanCodec.StreamDecoder`, and exposes decoded `CanFrame` values as a `SharedFlow`.
- `SlcanCodec` validates classic SLCAN frame types `t/T/r/R`, identifier width, DLC, payload length, and hexadecimal payload bytes. Its stream decoder preserves partial TCP lines between chunks.
- `CanCapture` provides bounded in-memory capture with relative monotonic timestamps and an explicit dropped-record counter. Default capacity is 50,000 records.
- `SlcanCaptureController` now explicitly wires the live `SlcanCanFrameStream` into `CanCapture`, with opt-in start/stop lifecycle and immutable `CanCaptureSession` output.
- `CanReplay` re-emits captured frames using their relative timing and supports replay speed scaling.
- `RawCanMonitorState` provides a UI-neutral live monitor state, filtering by CAN ID, pause handling, bounded visible-frame history, and bus statistics.
- `CapabilityDiscovery` now reads Mode 01 supported-PID bitmaps (`0100`, then `0120`/`0140` only when the preceding bitmap advertises the next range).
- `ObdMode01PidBitmap` parses ECU-advertised bitmap blocks and intersects them with `ObdPidRegistry`; the app does not expose a decoder merely because it exists locally.
- `CapabilitySnapshot.obdMode01SupportedPids` carries this discovery result into the Android connection state, and the Live Data UI consumes that set.

## Regression coverage

`SlcanCaptureControllerTest` verifies the complete application-side sequence:

`transport byte chunk → SLCAN decode → live frame stream → capture.record() → immutable session`

`ObdMode01PidBitmapTest` verifies bitmap decoding, compact/spaced payload handling, decoder-registry gating, and continuation-range semantics.

These tests use synthetic responses and do not claim physical vehicle evidence.

## Evidence boundary

These components prove the application-side SLCAN parsing/capture/replay path and the Mode 01 bitmap parser/discovery logic. They do **not** prove that a physical WiCAN device is currently delivering CAN RX traffic to the application, nor that any specific vehicle advertises a particular PID until a real ECU response is captured.

A transport connection, successful parser test, or successful build is not vehicle evidence. Vehicle verification requires an actual captured raw frame/ELM response session with source/transport metadata and subsequent replay/decoder tests.

## Current gates

SLCAN end-to-end evidence remains:

`real WiCAN transport → real RX bytes → real capture session → persisted evidence artifact → replay → decoder test → vehicle verification`

Standard OBD Live Data evidence is:

`real ELM327 session → ECU Mode 01 supported-PID bitmap → registry intersection → Mode 01 PID request/response → decoder → live sample → runtime/vehicle verification`

Do not promote any signal/PID/CAN mapping based solely on static APK extraction, parser tests, synthetic capture tests, or compilation.

## External WiCAN context

Official WiCAN documentation confirms WiCAN PRO supports `slcan/socketCAN`. A public firmware issue documents a case where WiCAN's own CAN monitor received live traffic while USB SLCAN forwarding delivered no frames to the host. This reinforces the requirement for end-to-end RX evidence rather than treating transport connection as proof of CAN data.
