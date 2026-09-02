# WiCAN Open Ecosystem Vision

## The idea

The long-term objective of AutoDiag-WiCAN-Pro is to help establish an independent, open automotive diagnostic ecosystem around a reusable WiCAN interface.

A useful analogy is **USB-C**: the value is not that every device is identical, but that a reusable interface can remain useful while devices, software and capabilities evolve around it.

For automotive diagnostics, the desired model is:

**vehicle changes → keep the interface → install/update software → add or improve the vehicle profile.**

## Two things can evolve independently

### 1. Adapter / firmware

WiCAN hardware and firmware can gain new physical interfaces, protocol support, performance improvements, logging capabilities and activity indicators.

A future hardware revision should not require throwing away the software ecosystem.

### 2. Open diagnostic software

AutoDiag-WiCAN-Pro can add:

- generic OBD-II
- CAN / SLCAN
- ISO-TP / UDS / KWP foundations
- manufacturer profiles
- ECU discovery and topology
- DTC and live-data decoding
- EV battery and charging analysis
- diagnostic tests
- evidence and verification
- repair knowledge integration
- automation and telemetry

A future vehicle should therefore be primarily a **software/profile expansion problem**, unless it genuinely requires hardware that the current adapter cannot provide.

## What this is not

This vision does not mean that one adapter is guaranteed to diagnose every vehicle or perform every workshop operation. Modern vehicles can require different buses, physical layers, diagnostic protocols, security access, OEM data, additional hardware and legally controlled procedures.

The project must never hide those limitations. Capability is always reported with its real scope and verification state.

## Why this is better than vehicle-specific clones

A vehicle-specific clone can be a useful short-term tool, but its value is strongly tied to the vehicle ecosystem it was built for.

The WiCAN/open-software model aims for a longer lifecycle:

```text
Car #1 ──┐
Car #2 ──┼──> reusable WiCAN interface ──> evolving open software
Car #3 ──┤                                      │
Car #4 ──┘                                      ├─ profiles
                                               ├─ protocols
                                               ├─ decoders
                                               ├─ tests
                                               └─ knowledge
```

The investment is therefore not just in today's diagnostic function. It is in a reusable interface plus an ecosystem that can continue to learn.

## Open-source development principle

The project should maximize reuse of engineering work across manufacturers. Protocol layers, parsers, capture/replay, evidence models, test infrastructure and UI components should not be tied unnecessarily to one brand.

Manufacturer-specific knowledge must remain properly scoped, sourced and licensed. The project must not copy proprietary binaries, source code, protected databases, credentials or security secrets.

## Community effect

The most important long-term asset is not a single APK. It is a shared body of tested infrastructure:

- one contributor improves ISO-TP;
- another adds a VAG profile;
- another validates a Tesla signal;
- another improves EV battery analysis;
- another adds replay fixtures;
- another improves DTC knowledge and provenance.

Those contributions can then benefit future vehicles and future contributors.

> **WiCAN is the reusable bridge. AutoDiag is the open diagnostic ecosystem. The car can change; the project keeps growing.**
