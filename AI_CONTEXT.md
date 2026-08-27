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
- OEM-linked diagnostic explanations and service-procedure references

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
2. Read `docs/ARCHITECTURE_OVERVIEW.md` before changing diagnostic architecture.
3. Never invent CAN IDs, bit layouts, PID meanings, ECU addresses, or Tesla-specific signals.
4. Every reverse-engineered signal must record its source and verification status.
5. Use explicit statuses such as `unverified`, `partially_verified`, and `verified`.
6. READ operations have priority over WRITE operations.
7. WRITE/CAN-control functionality must be isolated, explicitly marked experimental, disabled by default, and protected by deliberate user confirmation.
8. Never silently turn an unverified value into a confirmed diagnostic result.
9. Do not replace working code without a concrete reason and regression tests.
10. Significant changes require tests and relevant documentation updates.
11. Preserve backward compatibility of the transport and core APIs whenever practical.
12. Prefer small, reviewable commits.
13. When real vehicle data is unavailable, use the simulator and recorded captures instead of guessing.
14. Never hardcode a battery/Riso threshold merely because a community post reports it.
15. Every production diagnostic threshold requires an evidence record and defined vehicle/test scope.
16. Distinguish measured vehicle data, OEM-reported status, physical test results, AutoDiag calculations and AutoDiag inferences.
17. A missing OEM explanation must remain missing; do not replace it with generated text that looks authoritative.
18. OEM repair/service links must be verified before being stored or exposed as official procedures.
19. Safety-critical HV procedures must defer to the complete OEM safety/service documentation.
20. A single low cell voltage during acceleration is not, by itself, proof of a weak or defective cell.
21. Cell-level charging analysis must preserve time, current, temperature, SOC and cell/module identity where available.

## Diagnostic Knowledge Base

`docs/DIAGNOSTIC_KNOWLEDGE_BASE.md` defines the source-of-truth model for DTCs, alerts, explanations and OEM procedures.

Each knowledge entry must distinguish OEM material from engineering/reverse-engineering and community information. The application may show all of them, but never label community material as OEM guidance.

## Battery diagnostics

Battery analysis uses contextual samples and the STATIC / LOAD / RECOVERY / TREND / CONFIDENCE model.

The engine must not use universal mV thresholds. Thresholds are evidence-backed and vehicle/profile scoped. Community values remain research evidence until resolved through the evidence system.

Cell-level monitoring should support both driving/load tests and AC/DC charging whenever the vehicle exposes those measurements. Replay must reproduce the captured measurements and events rather than fabricate missing cells.

## Riso / HV isolation

Isolation is a safety-critical subsystem. Distinguish vehicle-reported numerical isolation, vehicle-reported status, physical insulation-test results and raw/undecoded data.

Never derive an MΩ value from an OK/fault status. Never replace manufacturer safety procedures with simplified instructions.

## Testing philosophy

The project should support three levels of validation:

1. Unit tests — parsers, transports, decoders, calculations.
2. Simulator/replay tests — deterministic CAN/OBD traffic without a vehicle.
3. Real-vehicle validation — only for signals and operations that can be safely and independently verified.

A real vehicle must never be the first place where an untested write operation is executed.

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

## Future compatibility

The Android project should use modern Android APIs and a modular architecture so that the diagnostic core is not tightly coupled to Android UI APIs. The goal is maintainability across future Android releases, not a guarantee that any specific Android API will remain unchanged for five years.
