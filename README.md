# AutoDiag-WiCAN-Pro

**An open-source automotive diagnostics platform built around WiCAN PRO — one reusable interface, software that keeps evolving with your cars.**

> **Vision: make WiCAN for automotive diagnostics what USB-C became for phones: a reusable interface layer that should outlive a single device or vehicle brand, while an open software ecosystem keeps adding capabilities over time.**

> **Status: early development (v0.1-dev).** Real-vehicle support is not considered validated unless explicitly marked as verified in the project documentation.

## 🚗 Buy the interface once. Keep the software growing with your cars.

This project starts from a simple problem with today's vehicle-specific diagnostic tools:

**You buy a cable for the car you have today — then the car changes, and the cable becomes yesterday's tool.**

A cloned VAG cable plus cloned VCDS-style software can make sense if your only goal is to work on one supported VAG vehicle. But imagine owning a Škoda/VW today and replacing it five years later with a Tesla, BMW, Hyundai/Kia, Mercedes, Renault/Dacia, Nissan or another brand. A tool designed around the old vehicle ecosystem may no longer be the tool you want.

The idea behind **WiCAN PRO + AutoDiag-WiCAN-Pro** is different:

**Keep the physical diagnostic interface. Let the software evolve.**

WiCAN PRO is the reusable hardware/interface. AutoDiag-WiCAN-Pro is the open software layer that can grow with new protocols, vehicle profiles, decoders, diagnostic workflows, tests and evidence. When a new vehicle or capability requires something the current hardware cannot provide, the adapter itself can evolve too — without throwing away the whole software ecosystem.

### The USB-C idea for automotive diagnostics

USB-C succeeded as a concept because the connector became a reusable common interface while phones, computers, chargers and accessories continued to evolve around it.

We want to pursue a similar **open, practical interoperability model for vehicle diagnostics**:

```text
             TODAY                         TOMORROW

     ┌─────────────────┐          ┌─────────────────────────┐
     │    Škoda / VW   │          │ Tesla / BMW / VAG / ... │
     └────────┬────────┘          └────────────┬────────────┘
              │                                │
              └──────────────┬─────────────────┘
                             │
                      ┌──────▼──────┐
                      │   WiCAN PRO │  ← reusable interface
                      └──────┬──────┘
                             │
                  ┌──────────▼──────────┐
                  │ AutoDiag-WiCAN-Pro  │  ← open software
                  │ protocols / profiles│
                  │ decoders / tests    │
                  │ knowledge / tools   │
                  └─────────────────────┘
                             │
                   software keeps growing
```

This is **an architectural goal, not a claim of universal compatibility today**. Automotive diagnostics remain dependent on the actual vehicle, ECU, protocol, hardware/firmware, security access, regional configuration and verified implementation. The important difference is that **vehicle support belongs in an evolving open software ecosystem instead of being permanently tied to one disposable cable/software package.**

## Why WiCAN PRO instead of a vehicle-specific clone?

| Vehicle-specific clone approach | WiCAN PRO + AutoDiag-WiCAN-Pro |
|---|---|
| Built mainly around one vehicle family | Built as a multi-vehicle diagnostic platform |
| Clone cable + cloned/proprietary ecosystem | Reusable interface + open software project |
| Change car → the old tool may lose most of its value | Change car → keep the interface and add a vehicle profile |
| New functionality depends on the original tool vendor | Developers can add protocols, decoders and workflows |
| Knowledge is tied to one product ecosystem | Knowledge, tests and software can be shared across brands |
| You may buy another cable after changing vehicles | **The goal is to buy the reusable interface once and keep evolving the software** |

### A real-life example

Imagine this path over the years:

**Škoda/VAG → Tesla → BMW → Hyundai/Kia → Mercedes → Renault/Dacia → Nissan**

With a vehicle-specific clone, each change can mean finding another compatible cable, another software package and another ecosystem.

With the WiCAN approach, the goal is:

**same reusable interface → new vehicle → new verified software support.**

If a future vehicle requires a new physical capability, the **adapter can evolve alongside the software**. The project therefore has two layers that can grow independently but are designed to work together:

1. **WiCAN hardware/firmware** — the physical bridge into automotive networks.
2. **AutoDiag software** — the open diagnostic platform that turns that bridge into vehicle-aware tools.

That separation is important. We do not want a future hardware revision to make the entire software ecosystem obsolete, and we do not want a software project to be locked forever to the capabilities of its first adapter revision.

## 🌱 An independent open-source ecosystem

The long-term goal is bigger than an Android app.

**AutoDiag-WiCAN-Pro should become an independent community project around an open diagnostic interface concept.** Developers should be able to contribute without having to own the same car as everyone else. A contributor working on ISO-TP should help Tesla, VAG, BMW and future vehicles. A contributor improving DTC evidence handling should improve the whole platform. A contributor adding a VAG profile should not make the architecture less useful for the next contributor working on another manufacturer.

The project is therefore designed around reusable layers:

- WiCAN transport and connectivity
- raw CAN and capture/replay
- ELM327 / OBD-II
- ISO-TP / UDS / KWP foundations
- vehicle and ECU discovery
- generic OBD-II live data and DTCs
- Tesla read-only diagnostics
- VAG and other manufacturer profiles
- EV battery, charging and thermal analysis
- diagnostic tests and pre-purchase checks
- DTC → diagnosis → repair knowledge
- source/provenance and verification tooling
- MQTT / Home Assistant / remote telemetry
- simulator and deterministic replay tests
- Android UI and dashboards
- future hardware capability detection and adapter revisions

### Why developers should contribute

A closed vehicle-specific tool gives a developer a feature for one product.

An open multi-brand platform can turn the same engineering work into infrastructure for many vehicles.

For example:

- **ISO-TP implementation** → reusable by many ECUs and manufacturers
- **UDS read-only foundation** → reusable across multiple vehicle profiles
- **CAN capture/replay** → useful for every new protocol investigation
- **DTC evidence model** → common diagnostic language across brands
- **battery-health algorithms** → reusable across EV profiles
- **vehicle-scope matching** → prevents one vehicle's data being incorrectly applied to another
- **simulator/replay tests** → contributors can develop without owning every vehicle

Every verified contribution should therefore have a chance to live for years instead of disappearing when a particular car model is replaced.

> **One reusable interface. One growing open ecosystem. Many vehicles over time.**

## Open development, not cloned software

This project is intentionally **not** based on copying proprietary diagnostic software, binaries, protected databases, credentials or manufacturer-internal security material.

For VAG and other manufacturers, the goal is **functional interoperability**: implement protocols, public standards, independently developed decoders, licensed data and verified procedures without copying proprietary source code or protected databases.

That means the project can pursue capabilities such as measuring values, basic settings, adaptations, service functions, gateway/topology and eventually carefully controlled coding/long-coding workflows **when the protocol, data, safety, legal basis and vehicle scope are actually verified**.

The project should be independent enough that its value comes from its architecture, engineering, testing, evidence and community — not from a copied commercial application.

## Built for developers, useful for drivers

The project should ultimately serve two audiences at once.

**For the driver:** connect WiCAN PRO, identify the vehicle, read faults, understand what they mean, monitor live data and receive useful evidence-based health information.

**For the developer:** use a modular open codebase, simulator/replay data, typed diagnostic models, vehicle scopes and verification metadata to add new capabilities without rewriting the entire application.

## Vision

Create a long-lived Android application and diagnostic core that can use WiCAN PRO over Wi-Fi for:

- raw CAN monitoring and logging
- generic OBD-II diagnostics
- Tesla Model 3/Y read-only diagnostics
- battery and vehicle-health analysis
- automated diagnostic tests
- remote telemetry while the vehicle is parked on the home network
- automation, notifications and Home Assistant/MQTT integration
- future VAG and other manufacturer-specific diagnostics
- carefully controlled custom actions where the required protocol and behavior are verified
- DTC/alert explanations linked to verified OEM documentation and service procedures
- future coding, adaptation and service functions when technically, legally and safely supportable

The goal is **not** to make a cosmetic WiCAN controller. WiCAN PRO remains the hardware/interface and firmware platform; AutoDiag provides the Android UI, diagnostic core, vehicle profiles and automation layer.

## Documentation entry point

Before changing diagnostic architecture, read `docs/ARCHITECTURE_OVERVIEW.md`.

For DTC/alert explanations, OEM procedures and source provenance, read `docs/DIAGNOSTIC_KNOWLEDGE_BASE.md`.

For permanent AI/developer rules and feature preservation, read `AI_CONTEXT.md` and `docs/LONG_TERM_FEATURE_PRESERVATION.md`.

For the physical WiCAN PRO LED/activity target, read `docs/WICAN_LED_ACTIVITY_INDICATOR.md`.

## Architecture

```text
Android / AutoDiag
        |
        +-- Transport layer
        |     +-- WiCAN ELM327 TCP :3333
        |     +-- WiCAN SLCAN/raw CAN TCP :23
        |     +-- mDNS discovery
        |     +-- future OBD Bluetooth/Wi-Fi adapters
        |     +-- simulator
        |
        +-- CAN / OBD / diagnostic core
        |
        +-- Vehicle profiles
        |     +-- Tesla
        |     +-- VAG
        |     +-- Generic OBD-II
        |
        +-- Diagnostics
        |     +-- battery
        |     +-- charging
        |     +-- thermal
        |     +-- drive unit
        |     +-- HV isolation / Riso
        |     +-- DTC / alerts
        |
        +-- Diagnostic Knowledge Base
        |     +-- OEM explanations
        |     +-- troubleshooting procedures
        |     +-- service/repair references
        |     +-- evidence + verification
        |
        +-- Automation
              +-- rules
              +-- notifications
              +-- MQTT / Home Assistant
              +-- verified custom actions
```

## Repository layout

```text
README.md
AI_CONTEXT.md
ROADMAP.md
ARCHITECTURE.md
SAFETY.md
CONTRIBUTING.md
android/
core/
vehicles/
simulator/
captures/
docs/
tests/
.github/workflows/
```

The exact implementation layout may evolve, but the separation between transport, diagnostic core, vehicle profiles and UI should remain.

## WiCAN PRO integration

AutoDiag is intended to use capabilities already provided by WiCAN PRO rather than reimplementing its firmware.

The initial transport targets are:

- **TCP :3333** — ELM327-compatible interface
- **TCP :23** — SLCAN/raw CAN passthrough
- **mDNS** — convenient device discovery where supported
- **MQTT/HTTP(S)** — future telemetry and integration paths where useful

These details must always be checked against the current WiCAN firmware/documentation before being treated as stable API guarantees.

## Diagnostic philosophy

AutoDiag is deliberately more than a generic OBD reader. The long-term objective is to combine:

1. real measurements from the vehicle,
2. vehicle-specific decoding,
3. automated repeatable tests,
4. history and replay,
5. evidence-based analysis,
6. and source-linked technical documentation.

For EVs this includes battery cell/module behavior during rest, acceleration/load, recovery, AC charging and DC fast charging where the vehicle exposes the required data. The application should be able to move from a simple driver-facing result to an expert numerical/replay view.

A low-voltage cell during a high-current load is not automatically classified as a failed cell. Context, load response, recovery and repeated observations are required.

## Diagnostic Knowledge Base

When AutoDiag recognizes a supported DTC or alert, the result can expose:

- what the code means,
- affected system,
- severity,
- sourced possible causes,
- recommended checks,
- official troubleshooting procedure,
- official service/repair procedure,
- parts and labor information where legally/licensed data is available,
- and the source/verification status.

OEM information is kept separate from community reverse engineering. Missing documentation is shown as missing; the app must not invent a repair procedure.

## Safety model

The first development stages are **READ-first**.

Unverified CAN IDs, signals and vehicle-specific behaviors must not be presented as confirmed facts.

WRITE/control functionality is intentionally out of scope for the initial milestones. When introduced, it must be:

- explicitly identified as experimental where appropriate,
- disabled by default until verified,
- isolated from the read-only core,
- protected by deliberate user confirmation,
- tested in the simulator before real-vehicle validation.

See `SAFETY.md` for the project safety rules.

## Verification levels

Vehicle data and capabilities should carry a verification state:

- `unverified` — source exists but behavior has not been independently confirmed
- `partially_verified` — some independent validation exists
- `verified` — reproducible validation exists for a defined vehicle/HW/software configuration

A verified result is always tied to its scope. A signal verified on one vehicle generation is not automatically verified for another.

## Development strategy

The project deliberately supports development without a vehicle:

1. Build and test parsers and transport code with unit tests.
2. Use the WiCAN simulator/mock and recorded captures.
3. Validate the decoder deterministically.
4. Compare against independent reference data where possible.
5. Only then validate on a real vehicle.

This is intended to prevent the common failure mode where an APK appears to work until it is connected to a real car.

## Android

Planned baseline:

- Kotlin
- Jetpack Compose
- modern AndroidX APIs
- modular diagnostic core
- minSdk 26 initially
- current target SDK, updated as Android evolves

The project aims for long-term maintainability across future Android releases. No software project can guarantee unchanged behavior for a fixed five-year period, so compatibility will be maintained through current APIs, automated builds/tests and regular dependency updates.

## Hardware activity LED

WiCAN PRO hardware documentation identifies the blue LED on GPIO7, and recent WiCAN PRO firmware release notes document an LED command with blink support. Therefore an adapter activity indication is a realistic future target, but AutoDiag cannot control the physical LED by itself unless the installed firmware exposes a compatible control path.

There is an important distinction between **LED command control** and an **automatic communication-activity state machine**. Recent firmware evidence establishes the command/blink primitive, but the project does not assume that firmware already exposes a stable automatic CAN/ELM327 activity mode.

Desired behavior:

- steady blue = powered/idle
- slow pulse = connected
- short pulse = diagnostic/CAN communication
- faster bounded pulse = sustained high traffic
- separate fault pattern = firmware-defined fault state
- separate update pattern = firmware-defined update state

The implementation must prioritize fault/update states and must never send one LED command per CAN frame from Android. See `docs/WICAN_LED_ACTIVITY_INDICATOR.md` and GitHub Issue #5 for the verification and implementation plan.

## Roadmap

See `ROADMAP.md` for the detailed milestone plan.

Initial milestones:

1. Android foundation and CI
2. WiCAN mDNS discovery
3. real TCP ELM327 connection on :3333
4. raw/SLCAN connection on :23
5. CAN monitor and capture logging
6. simulator and replay
7. generic OBD-II
8. Tesla read-only decoder
9. automated Tesla health test
10. battery charge/load analysis and replay
11. HV isolation/Riso data model
12. diagnostic knowledge base with OEM references
13. remote monitoring and automation
14. carefully verified custom actions
15. additional vehicle manufacturers
16. VAG coding/adaptation/service-function research and implementation where verified
17. WiCAN PRO activity LED firmware integration if supported safely

## Current status

**v0.1-dev / foundation**

The repository is currently being established as a documentation-first project. Real WiCAN PRO and real Tesla validation will be added as separate milestones and will not be claimed before testing.

## License

License will be selected and documented before the first public software release.

## Disclaimer

This is experimental automotive software. Vehicle-specific diagnostic and control behavior can be safety-critical. Do not execute unverified write/control operations on a real vehicle.
