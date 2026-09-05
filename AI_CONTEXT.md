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

## Authoritative project documents

Before architectural implementation, read:

1. `README.md`
2. `AI_HANDOFF.md`
3. `ROADMAP.md`
4. `docs/ARCHITECTURE_OVERVIEW.md`
5. `docs/CAPABILITY_DISCOVERY.md`
6. `docs/AUTOMATION_ENGINE.md`
7. `docs/DIAGNOSTIC_KNOWLEDGE_BASE.md`
8. `docs/LONG_TERM_FEATURE_PRESERVATION.md`
9. relevant battery/HV documents

`docs/IMPLEMENTATION_TASKS.md` is the active implementation backlog.

## Permanent feature-preservation rule

The complete target capability set must survive across future AI and human developers. A feature must NOT be deleted merely because it cannot be implemented today.

Use `BLOCKED: <reason>` and preserve the target in the catalog/roadmap. Valid capability states include `AVAILABLE`, `AVAILABLE_WITH_PREREQUISITES`, `REQUIRES_OEM_SECURITY`, `REQUIRES_ADDITIONAL_HARDWARE`, `NOT_SUPPORTED`, `UNKNOWN` and `BLOCKED: <reason>`.

Before changing a capability, cross-check hardware, firmware/API, transport, protocol, vehicle/ECU, VIN/model/year/region/software scope, OEM security, additional hardware, safety, simulator/replay coverage, tests and evidence/provenance.

A capability is not `VERIFIED` merely because code exists.

Vehicle-specific implementations must preserve compatibility scope such as VIN/make/model/generation/year/region/powertrain/ECU/HW/SW/protocol/firmware. Do not generalize a verified result beyond its evidence scope.

Future VAG functionality may include outcomes comparable to established diagnostic tools, including measuring values, basic settings, adaptations, service functions, gateway topology and coding/long coding. Direct copying of proprietary VCDS binaries, encrypted label databases (CLB/LBL/XPL) or protected coding databases is forbidden. Independent reverse-engineering, observation of live bus traffic, public documentation, licensed data and independently verified engineering work are allowed and expected. All derived signal maps, coding definitions and thresholds must carry explicit provenance and verification status.

All planned service/coding functions remain visible even if currently blocked by protocol, security, hardware or missing evidence. High-risk WRITE functions remain isolated, default-off and gated by exact scope, backup/recovery, simulator/replay validation and explicit confirmation.

See `docs/LONG_TERM_FEATURE_PRESERVATION.md` for the permanent developer contract.

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

1. Read the authoritative project documents before making architectural changes.
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
13. Do not hardcode a battery/Riso threshold from a single community post without evidence. Community values may be used as research input.
14. Every production diagnostic threshold requires an evidence record, defined vehicle/test scope and verification status. Thresholds derived from observed vehicle behaviour, Watchdog/PHEV logs or independent testing are permitted when provenance is recorded.
15. Distinguish measured vehicle data, OEM-reported status, physical test results, AutoDiag calculations and AutoDiag inferences.
16. A missing OEM explanation must remain missing; do not replace it with generated text that looks authoritative.
17. OEM repair/service links must be verified before being stored or exposed as official procedures.
18. Safety-critical HV procedures must defer to the complete OEM safety/service documentation.
19. A single low cell voltage during acceleration is not, by itself, proof of a weak or defective cell.
20. Cell-level charging analysis must preserve time, current, temperature, SOC and cell/module identity where available.
21. Capability Discovery must distinguish unavailable data from failed communication and unknown decoding.
22. Capability cache scope must include VIN and relevant software/firmware identity where available.
23. Automation rules must be data-driven, exportable and replayable; notification actions have rate limits and audit logs.
24. Unsupported automatic-test stages are `NOT_AVAILABLE`, not `PASS` or `FAIL`.
25. Never remove a planned capability because it is currently blocked; document the blocker and keep the target visible.
26. Before declaring a new capability supported, perform the project audit/cross-check and record evidence and scope.
27. Hardware-dependent features must be explicitly separated from Android-only features.
28. Competitor applications (PHEV Watchdog, Torque, Car Scanner, Mitsubishi Remote Ctrl, LCode, etc.) may be used as behavioural reference and for capability discovery. Signal maps, PID lists and coding tables extracted or reverse-engineered from them are allowed provided they are re-implemented with provenance, not binary-copied, and marked with verification status.
29. **Repository synchronization rule:** `AI_CONTEXT.md`, `AI_HANDOFF.md`, manifests and evidence status are descriptive state, not authority over Git history. Before work, verify the actual current branch/HEAD and reconcile documentation if it differs.
30. **Evidence-layer rule:** static APK/DEX extraction can establish an extracted decoder candidate (including response indexes, scale and unit when directly evidenced), but does not establish physical meaning, ECU/CAN binding or vehicle verification.
31. **Candidate/verification rule:** `candidate` + `unverified` is a valid state and must not be rewritten as `unresolved` merely because vehicle capture is missing. Use `BLOCKED: vehicle verification required` for promotion gates.

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

## Capability Discovery

Discovery is the gatekeeper for diagnostic UI and Automatic Health Check. It must be granular enough to say, for example, that cell voltage is available while cell temperature is only partially available. Market/region warnings, such as a US-market indicator, require reliable vehicle-derived evidence and must not be guessed from user location or connector type.

## Automation

Automation has three action classes:

- `READ_LOG_ANALYZE`
- `NOTIFY_ALERT`
- `WRITE_COMMAND`

The first two can be developed in the initial project. `WRITE_COMMAND` is isolated and disabled by default. Rules are stored as data and must support replay/dry-run before activation.

## Testing philosophy

The project should support three levels of validation:

1. Unit tests — parsers, transports, decoders, calculations.
2. Simulator/replay tests — deterministic CAN/OBD traffic without a vehicle.
3. Real-vehicle validation — only for signals and operations that can be safely and independently verified.

A real vehicle must never be the first place where an untested write operation is executed.

## Current development state

Version: 0.1-dev

Current repository state is actively evolving on `main`. Do not rely on a hard-coded commit SHA in this file; verify `main` at the start of each task. The current work includes the Android OBD foundation plus evidence/promotion-gate work for SEOBD/S3XY and Car Scanner research. These evidence additions are **not** vehicle verification.

The separate `AutoDiag-WiCAN-Diagnostic-Data` repository is the canonical home for diagnostic evidence/candidate datasets. Its manifest must agree with the actual candidate files and must not expose candidate-internal ECU/signal counts as production records.

## Important distinction

WiCAN PRO firmware is not AutoDiag.

WiCAN PRO supplies the physical interface and existing firmware/network protocols. AutoDiag is the Android application and diagnostic/automation layer built on top of those interfaces.

The project should reuse stable WiCAN capabilities rather than unnecessarily reimplementing firmware functionality.

## Future compatibility

The Android project should use modern Android APIs and a modular architecture so that the diagnostic core is not tightly coupled to Android UI APIs. The goal is maintainability across future Android releases, not a guarantee that any specific Android API will remain unchanged for five years.
