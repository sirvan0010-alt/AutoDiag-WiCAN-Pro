# Long-Term Feature Preservation & Developer Contract

## Purpose

This document is a permanent project contract for human and AI contributors.

The project must preserve the complete diagnostic target even when a capability is currently impossible, unavailable, unsafe, undocumented, blocked by OEM security, limited by WiCAN PRO hardware/firmware, or waiting for future contributors.

**A blocked feature is still a feature. Do not delete it.**

Use explicit states such as:

- `AVAILABLE`
- `AVAILABLE_WITH_PREREQUISITES`
- `REQUIRES_OEM_SECURITY`
- `REQUIRES_ADDITIONAL_HARDWARE`
- `NOT_SUPPORTED`
- `UNKNOWN`
- `BLOCKED: <reason>`

## Source of truth

The long-term target is distributed across:

- `README.md` — public vision and user-facing value
- `AI_CONTEXT.md` — permanent AI contributor rules
- `ROADMAP.md` — implementation order
- `docs/ADAPTER_AND_SERVICE_CAPABILITY_MATRIX.md` — adapter/protocol/service capability matrix
- `docs/REPAIR_KNOWLEDGE_ESTIMATE_ARCHITECTURE.md` — repair intelligence model
- `docs/TODAY_MASTER_PLAN_2026-09-02.md` — comprehensive implementation plan
- GitHub Issues/PRs — active implementation work and review

When a new feature is discovered, it must be recorded in the appropriate catalog/document before implementation is deferred.

## Audit and cross-check contract

Before declaring a capability implemented or supported, contributors should cross-check:

1. WiCAN PRO hardware capability
2. WiCAN firmware/API capability and version
3. transport availability and limits
4. automotive protocol requirements
5. vehicle/ECU capability
6. VIN/model/year/region/software scope
7. OEM security/authentication requirements
8. additional hardware requirements
9. safety implications
10. simulator/replay coverage
11. unit/integration test coverage
12. independent evidence/provenance

A feature is not `VERIFIED` merely because code exists.

## Compatibility matrix principle

Every vehicle-specific implementation must be scoped as precisely as practical:

`VIN / make / model / generation / year / region / powertrain / ECU / HW / SW / protocol / firmware`

Do not generalize one verified vehicle result to every vehicle of the same badge.

## Function preservation

The target catalog includes both read-only and future service functions, including coding/adaptation/service resets and other OEM-dependent operations.

Examples include:

- ECU coding
- injector coding
- BMS adaptation
- DPF/GPF regeneration
- EGR learning
- gearbox/gear adaptation
- SAS/AFS/suspension calibration
- TPMS reset
- oil/service reset
- brake service functions
- gateway calibration
- component activation
- immobilizer-related functions
- parameterization/programming
- lighting configuration such as short/long beam-related coding where legally and technically supported

Their presence in the roadmap **does not mean they are currently executable**. Each must pass the capability/security/safety gates before execution is enabled.

## VCDS/VAG feature strategy

The project may implement VAG diagnostic functionality comparable in user outcome to established VAG tools where the underlying protocol, procedure, data and legal/licensing basis can be independently verified.

The objective is **not to copy proprietary VCDS software or proprietary code/data**. Instead, implement interoperable diagnostic behavior from documented standards, legally obtained information, public documentation, licensed databases, or independently verified engineering work.

Potential VAG feature families to preserve in the target catalog include:

- ECU identification
- fault-code reading/clearing
- measuring values/live data
- basic settings
- adaptations
- output/component tests
- service resets
- gateway installation/topology
- coding/long coding
- adaptation channels
- actuator/service procedures
- supported UDS/KWP diagnostic functions

Coding must be treated as a high-risk capability. It requires exact ECU/vehicle scope, known coding structure, reversible backup where possible, explicit user confirmation, simulator/replay validation and a verified recovery path.

## Adapter value proposition

WiCAN PRO should be presented as a reusable physical diagnostic interface rather than a one-car cable.

The product story should emphasize that AutoDiag is intended to grow with the vehicle owner:

- buy one capable interface
- keep the Android application and diagnostic core evolving
- change from VAG to Tesla, BMW, Hyundai/Kia, Mercedes, Renault/Dacia, Nissan, etc. without automatically throwing away the physical interface
- add vehicle profiles and verified capabilities over time
- benefit from open development and community contributions

Do **not** promise universal compatibility. State clearly that actual functions depend on vehicle protocol, ECU support, security, firmware, hardware and verified implementation.

## Hardware LED / activity indication

WiCAN PRO has a permanently illuminated blue indicator under the enclosure according to the current user hardware observation. A future activity indicator is desirable:

- blue steady = powered/idle
- blue blink/pulse = active communication or processing
- optional faster activity indication = high traffic
- fault indication must be distinct if hardware/firmware permits

This is a **hardware/firmware feasibility item**, not an Android-only feature. The Android app cannot directly make an onboard LED blink unless the WiCAN firmware exposes a controllable LED or the hardware is modified.

### Verified feasibility evidence

The upstream WiCAN firmware project documents the OBD hardware LED as **Blue LED → GPIO7**. Its recent WiCAN PRO release notes also document a console LED command with RGB color and blink options. This makes an activity LED technically plausible, but the exact command/API and safe integration point still need to be verified against the firmware version installed on the adapter. citeturn0search1turn0search2

Implementation path:

1. inspect current WiCAN PRO hardware LED wiring and GPIO assignment;
2. verify whether the installed firmware exposes the LED command/API;
3. verify current firmware behavior and update mechanism;
4. if controllable, add a minimal firmware activity state machine;
5. expose it only if it does not interfere with CAN timing, power or diagnostics;
6. keep a firmware-version compatibility matrix;
7. never assume the LED can be controlled from Android until the adapter exposes that capability.

## Licensing and interoperability

Do not copy proprietary VCDS/ODIS/ISTA/XENTRY/other OEM software, proprietary binaries, copyrighted databases, security credentials or protected datasets.

Implement compatible functionality through legitimate protocols and licensed/public/independently verified data. Link to or integrate licensed sources where required.

## Developer contribution model

Future developers should be able to pick up a GitHub Issue and work without losing project context.

Each substantial feature should contain:

- problem statement
- intended capability
- current state
- blocking reason, if any
- protocol assumptions
- vehicle scope
- evidence/source
- safety classification
- implementation plan
- tests
- simulator/replay case
- validation status
- known limitations

A contributor may improve or replace an implementation, but must preserve the capability record and update compatibility/evidence documentation.

## Non-regression rule

Before deleting or substantially narrowing a planned capability, a contributor must record why the target changed. Temporary inability is not a valid reason to remove it.

The preferred action is:

`KEEP TARGET → MARK BLOCKED → DOCUMENT WHY → CREATE/UPDATE ISSUE → IMPLEMENT WHEN EVIDENCE/HARDWARE/PROTOCOL BECOMES AVAILABLE`
