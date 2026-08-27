# AI_CONTEXT.md — AutoDiag-WiCAN-Pro

## Purpose

AutoDiag-WiCAN-Pro is a long-term, open Android automotive diagnostics and automation project built around WiCAN PRO and, later, other vehicle interfaces.

The project is intentionally developed as a documented, testable platform rather than a one-off APK.

## Primary goals

- WiCAN PRO connectivity over Wi-Fi
- Safe raw CAN monitoring and logging
- OBD-II diagnostics
- Tesla Model 3/Y read-only diagnostics, with emphasis on battery and vehicle health
- Automated vehicle health tests
- Remote telemetry from a vehicle over a home network
- Extensible vehicle profiles
- Future VAG and other manufacturer support
- Automation and custom actions where protocol and safety are verified

## Transport assumptions

WiCAN PRO is the primary hardware interface.

The application must keep transport concerns separate from diagnostic logic. Expected WiCAN transports include:

- ELM327-compatible TCP transport (currently documented as TCP port 3333)
- SLCAN/raw CAN passthrough (currently documented as TCP port 23)
- mDNS discovery where supported
- HTTP/HTTPS configuration or telemetry interfaces where appropriate
- MQTT for telemetry/automation integrations where appropriate

Do not assume an undocumented protocol. Verify behavior against the current WiCAN firmware/documentation before implementation.

## Rules for AI contributors

1. Read `README.md`, `ROADMAP.md`, `ARCHITECTURE.md`, `SAFETY.md`, and this file before making architectural changes.
2. Never invent CAN IDs, bit layouts, PID meanings, ECU addresses, or Tesla-specific signals.
3. Every reverse-engineered signal must record its source and verification status.
4. Use explicit statuses such as `unverified`, `partially_verified`, and `verified`.
5. READ operations have priority over WRITE operations.
6. WRITE/CAN-control functionality must be isolated, explicitly marked experimental, disabled by default, and protected by deliberate user confirmation.
7. Never silently turn an unverified value into a confirmed diagnostic result.
8. Do not replace working code without a concrete reason and regression tests.
9. Significant changes require tests and relevant documentation updates.
10. Preserve backward compatibility of the transport and core APIs whenever practical.
11. Prefer small, reviewable commits.
12. When real vehicle data is unavailable, use the simulator and recorded captures instead of guessing.

## Current development state

Version: 0.1-dev

Initial project phase:

- Repository established
- Documentation-first architecture
- Android application to be built with Kotlin/Jetpack Compose
- Transport abstraction planned
- Simulator planned
- Real WiCAN hardware validation is a separate milestone

## Important distinction

WiCAN PRO firmware is not AutoDiag.

WiCAN PRO supplies the physical interface and existing firmware/network protocols. AutoDiag is the Android application and diagnostic/automation layer built on top of those interfaces.

The project should reuse stable WiCAN capabilities rather than unnecessarily reimplementing firmware functionality.

## Testing philosophy

The project should support three levels of validation:

1. Unit tests — parsers, transports, decoders, calculations.
2. Simulator/replay tests — deterministic CAN/OBD traffic without a vehicle.
3. Real-vehicle validation — only for signals and operations that can be safely and independently verified.

A real vehicle must never be the first place where an untested write operation is executed.

## Future compatibility

The Android project should use modern Android APIs and a modular architecture so that the diagnostic core is not tightly coupled to Android UI APIs. The goal is maintainability across future Android releases, not a guarantee that any specific Android API will remain unchanged for five years.
