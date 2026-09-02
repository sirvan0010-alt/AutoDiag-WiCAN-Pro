# AutoDiag-WiCAN-Pro

Open, modular Android automotive diagnostics and automation platform built around **WiCAN PRO** and designed to support additional automotive interfaces in the future.

> **Status: early development (v0.1-dev).** Real-vehicle support is not yet considered validated unless explicitly marked as verified in the project documentation.

## Why WiCAN PRO + AutoDiag?

The idea is simple: **buy the reusable diagnostic interface once and keep the software growing with your cars.**

A vehicle-specific clone cable and cloned proprietary software can be useful for one manufacturer, but its value can drop sharply when the owner changes vehicles. AutoDiag takes the opposite approach: WiCAN PRO is the physical interface, while the open Android software is the long-term diagnostic layer that can gain verified support for multiple manufacturers and protocols.

For example, an owner can start with a VAG vehicle and later move to Tesla, BMW, Hyundai/Kia, Mercedes, Renault/Dacia, Nissan or another supported vehicle family without automatically replacing the diagnostic interface. The exact functions always depend on the vehicle, ECU, protocol, security, WiCAN firmware/hardware and verified implementation — **there is no claim of universal compatibility**.

The project also aims to implement diagnostic outcomes comparable to established manufacturer-specific tools where the underlying protocol, procedure, data and legal/licensing basis can be independently verified. That includes future VAG functions such as measuring values, basic settings, adaptations, service functions, gateway/topology work and coding/long coding. We will build interoperability, not copy proprietary VCDS binaries, source code or protected databases.

The long-term target is deliberately larger than today's implementation. A feature that is currently blocked by hardware, OEM security, missing documentation, missing vehicle data or lack of verification remains in the project as a documented future capability rather than being deleted.

See `docs/LONG_TERM_FEATURE_PRESERVATION.md` for the permanent developer/AI contract.

## Vision

Create a long-lived Android application that can use WiCAN PRO over Wi-Fi for:

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

Before changing diagnostic architecture, read [`docs/ARCHITECTURE_OVERVIEW.md`](docs/ARCHITECTURE_OVERVIEW.md).

For DTC/alert explanations, OEM procedures and source provenance, read [`docs/DIAGNOSTIC_KNOWLEDGE_BASE.md`](docs/DIAGNOSTIC_KNOWLEDGE_BASE.md).

For permanent AI/developer rules and feature preservation, read [`AI_CONTEXT.md`](AI_CONTEXT.md) and [`docs/LONG_TERM_FEATURE_PRESERVATION.md`](docs/LONG_TERM_FEATURE_PRESERVATION.md).

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

WiCAN PRO hardware documentation identifies the blue LED on GPIO7, and recent WiCAN PRO firmware release notes document an LED command with blink support. Therefore an adapter activity indication is a realistic future target, but AutoDiag cannot control the physical LED by itself unless the installed firmware exposes a compatible control path. Exact behavior must be verified per firmware version before enabling it.

Desired behavior:

- steady blue = powered/idle
- blink/pulse = active communication
- optional faster activity = high traffic
- separate fault indication if the hardware/firmware supports it

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
