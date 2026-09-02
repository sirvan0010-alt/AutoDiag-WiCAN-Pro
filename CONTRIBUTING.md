# Contributing to AutoDiag-WiCAN-Pro

Welcome. The project is building an open, long-lived Android automotive diagnostics platform around **WiCAN PRO**.

## Why contribute?

The project is intentionally designed around a reusable interface rather than a vehicle-specific diagnostic cable.

A contributor can add a decoder, protocol layer, test vector, vehicle profile, simulator capture, knowledge-base source, UI component or automation feature once and help make the same software useful across many vehicles over time.

The long-term idea is:

> **Buy WiCAN PRO once. Keep the software growing as you change cars.**

Someone may start with a Škoda/VW, then move to Tesla, BMW, Hyundai/Kia, Mercedes, Renault/Dacia or Nissan. A vehicle-specific clone cable can become the wrong tool after that change; an open multi-vehicle project can continue evolving instead.

This repository does **not** promise universal vehicle compatibility. Every feature must be tied to the vehicle, ECU, protocol, hardware/firmware and verification evidence that actually supports it.

## Good contribution areas

- Kotlin / Android architecture
- WiCAN TCP, ELM327 and SLCAN transport
- raw CAN capture, filtering and replay
- OBD-II Mode 01-0A
- ISO-TP / UDS / KWP foundations
- ECU discovery and topology
- Tesla read-only diagnostics
- VAG diagnostics and future coding/adaptation research
- BMW, Hyundai/Kia, Mercedes, Renault/Dacia, Nissan and other profiles
- EV battery and charging analysis
- DTC and diagnostic knowledge
- OEM-source provenance and verification tooling
- simulator and deterministic test vectors
- Compose UI, dashboards and graphs
- MQTT / Home Assistant / remote telemetry

## Evidence-first rule

Do not add invented CAN IDs, signals, scaling formulas, security procedures or service instructions as facts.

Use these states where appropriate:

- `UNVERIFIED`
- `PARTIALLY_VERIFIED`
- `VERIFIED`

A verified result must have a defined scope. Evidence from one vehicle generation is not automatically valid for another.

## Safety

The project is READ-first. Write/control functions must remain isolated, disabled by default until verified, explicitly confirmed by the user, and tested in simulator/replay before real-vehicle validation.

Do not copy proprietary VCDS binaries, source code, protected databases, OEM security credentials or other protected material. The target is functional interoperability based on standards, public documentation, licensed information and independent engineering.

## Before opening a pull request

1. Read `AI_CONTEXT.md` and `docs/LONG_TERM_FEATURE_PRESERVATION.md`.
2. Read `docs/ARCHITECTURE_OVERVIEW.md` before changing core architecture.
3. Keep transport, protocol, vehicle profile and UI responsibilities separated.
4. Add deterministic tests or replay coverage for new decoding logic where possible.
5. Document verification scope and evidence.
6. Preserve blocked future features in documentation/issues rather than silently deleting them.

## Contribution principle

Think beyond one car.

The best contribution is not only "make this work on my vehicle" but "make this reusable infrastructure so the next vehicle can benefit too."
