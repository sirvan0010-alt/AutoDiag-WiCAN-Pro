# SLCAN capture evidence — 2026-09-06

## Verified implementation

- `WiCanTransport` exposes `observeIncoming(): Flow<ByteArray>` and explicitly supports `TransportMode.SLCAN_RAW`.
- `SlcanCanFrameStream` consumes transport byte chunks, timestamps receipt with `System.nanoTime()`, feeds them to `SlcanCodec.StreamDecoder`, and exposes decoded `CanFrame` values as a `SharedFlow`.
- `SlcanCodec` validates classic SLCAN frame types `t/T/r/R`, identifier width, DLC, payload length, and hexadecimal payload bytes. Its stream decoder preserves partial TCP lines between chunks.
- `CanCapture` provides bounded in-memory capture with relative monotonic timestamps and an explicit dropped-record counter. Default capacity is 50,000 records.
- `CanReplay` re-emits captured frames using their relative timing and supports replay speed scaling.
- `RawCanMonitorState` provides a UI-neutral live monitor state, filtering by CAN ID, pause handling, bounded visible-frame history, and bus statistics.

## Evidence boundary

These components prove the presence of an application-side SLCAN parsing/capture/replay path. They do **not** prove that a physical WiCAN device is currently delivering CAN RX traffic to the application.

A transport connection or successful parser test is not vehicle evidence. Vehicle verification requires an actual captured raw frame session with source/transport metadata and subsequent replay/decoder tests.

## Current implementation gap

A repository-wide search did not find a current call site constructing `CanCapture`. Therefore the capture recorder exists as a reusable component, but its production wiring into the live SLCAN stream is not yet established by source evidence.

The next implementation gate is therefore:

`WiCanTransport.observeIncoming()` → `SlcanCanFrameStream` → `CanCapture.record()` → immutable evidence artifact → `CanReplay` regression test.

Do not promote any signal/PID/CAN mapping based solely on static APK extraction or parser tests.

## External WiCAN context

Official WiCAN documentation confirms WiCAN PRO supports `slcan/socketCAN`. A public firmware issue documents a case where WiCAN's own CAN monitor received live traffic while USB SLCAN forwarding delivered no frames to the host. This reinforces the requirement for end-to-end RX evidence rather than treating transport connection as proof of CAN data.
