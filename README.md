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
- **integrated automotive oscilloscope / signal analysis**
- diagnostic tests and pre-purchase checks
- DTC → diagnosis → repair knowledge
- source/provenance and verification tooling
- MQTT / Home Assistant / remote telemetry
- simulator and deterministic replay tests
- Android UI and dashboards
- future hardware capability detection and adapter revisions

## 🔬 Integrated automotive oscilloscope

The oscilloscope is a **first-class planned capability**, not a claim that the current WiCAN PRO hardware already contains an analog oscilloscope.

The software foundation now models timestamped analog samples, captures, trigger detection and basic measurements. The long-term tool is intended to provide waveform viewing, time/volts divisions, zoom, cursors, frequency/period, duty cycle, RMS, recording/replay and correlation with CAN/UDS/DTC events.

A real oscilloscope requires a verified measurement front end, electrical protection, suitable probes, firmware/data transport and hardware capability advertisement. High-voltage EV measurements require dedicated safety-rated/isolation hardware and must never be inferred from an ordinary low-voltage oscilloscope input.

See `docs/OSCILLOSCOPE_ARCHITECTURE.md` for the preserved architecture and implementation roadmap.

### Why developers should contribute

A closed vehicle-specific tool gives a developer a feature for one product.

An open multi-brand platform can turn the same engineering work into infrastructure for many vehicles.

For example:

- **ISO-TP implementation** → reusable by many ECUs and manufacturers
- **UDS foundation** → reusable across multiple vehicle profiles
- **CAN capture/replay** → useful for every new protocol investigation
- **oscilloscope capture/replay** → useful for sensor and actuator investigations across brands
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
- **future integrated oscilloscope/signal analysis with verified measurement hardware**
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

For the integrated oscilloscope architecture, read `docs/OSCILLOSCOPE_ARCHITECTURE.md`.

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
        |     +-- CAN capture/replay
        |     +-- ISO-TP / UDS
        |     +-- controlled service/config writes
        |
        +-- Measurement layer
        |     +-- oscilloscope samples/captures
        |     +-- triggers / measurements
        |     +-- waveform viewer
        |     +-- CAN/UDS correlation
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
android/
core/
diagnostics/
docs/
src/
tools/
```
