# WiCAN Platform Vision

## The idea in one sentence

**WiCAN should become the USB-C of automotive diagnostics: a reusable interface standard and ecosystem that can stay with the owner while the vehicle changes and the software keeps evolving.**

This is a project vision, not a claim that one adapter can currently diagnose every vehicle or perform every workshop function.

## Why this matters

The automotive diagnostic market is often fragmented by manufacturer, model generation, protocol and proprietary software ecosystem.

A driver can buy a vehicle-specific cable and software package, use it successfully for several years, then change vehicles and discover that the old tool has little value for the new car.

The alternative proposed by WiCAN is a longer-lived relationship between hardware and software:

```text
Vehicle changes
      |
      v
Keep the WiCAN interface
      |
      v
Install/update the evolving open software
      |
      +--> new vehicle profile
      +--> new protocol support
      +--> new ECU decoder
      +--> new diagnostic workflow
      +--> new battery/EV analysis
      +--> new repair knowledge
```

The adapter can evolve too. New WiCAN hardware can add capabilities, while the software architecture remains able to support older compatible hardware where practical.

## The USB-C analogy

USB-C is a useful analogy because the value is not only the connector itself. It represents a reusable interface that can survive multiple generations of devices.

WiCAN should aim for the same **product philosophy**, not literally become a universal automotive bus standard overnight:

| USB-C philosophy | WiCAN philosophy |
|---|---|
| One reusable physical interface | One reusable diagnostic interface |
| Many device types | Many vehicle manufacturers |
| Capabilities evolve | Diagnostic capabilities evolve |
| New devices can coexist with the ecosystem | New cars and new WiCAN hardware can coexist with the software |
| Users do not want to replace the connector for every device | Users should not need a different diagnostic adapter for every car when one compatible interface can do the job |

The analogy has limits: automotive buses, ECUs, security access, service procedures and physical interfaces are far more diverse than consumer USB. WiCAN therefore needs explicit capability discovery and verification rather than pretending that every vehicle is identical.

## Open-source flywheel

The most important asset should become the ecosystem, not a single APK version.

```text
More users
   -> more real-world evidence
   -> more captures/replay vectors
   -> more verified vehicle profiles
   -> more contributors
   -> more diagnostic functions
   -> more useful software
   -> more reasons to keep the same adapter
   -> more users
```

Contributions should be reusable across brands whenever the underlying protocol or diagnostic concept is shared.

Examples:

- CAN frame infrastructure can serve VAG, Tesla, BMW and many other vehicles.
- ISO-TP can serve multiple UDS-based ECUs.
- UDS response handling can be shared across manufacturers.
- OBD-II Mode 01–0A support can serve generic emissions diagnostics across compatible vehicles.
- Evidence/provenance models can serve every vehicle profile.
- Replay and simulator infrastructure can test new decoders without requiring a vehicle for every developer.

## What we are building

AutoDiag-WiCAN-Pro is the open software layer around this idea.

The project is intended to grow into:

1. **Transport** — WiCAN connectivity, TCP, ELM327, SLCAN/raw CAN and future compatible interfaces.
2. **Protocol core** — CAN, ISO-TP, UDS, KWP and generic OBD-II foundations.
3. **Vehicle profiles** — generic OBD, Tesla, VAG, BMW, Hyundai/Kia, Mercedes, Renault/Dacia, Nissan, Mitsubishi and others as evidence becomes available.
4. **ECU discovery** — identify the vehicle network and build a topology/capability view.
5. **Read diagnostics** — live data, DTCs, freeze frames, readiness, identifiers and measured values.
6. **Vehicle health** — battery, thermal, charging, HV isolation and trend analysis where data is exposed.
7. **Service workflows** — resets, adaptations, basic settings and other functions only when the exact vehicle/procedure is verified.
8. **Repair intelligence** — DTC → possible causes → diagnostic checks → parts/OEM numbers → labor/price → repair references, with provenance and licensing boundaries.
9. **Automation** — notifications, MQTT, Home Assistant and remote telemetry.
10. **Developer infrastructure** — simulator, replay, test vectors, evidence tracking and deterministic validation.

## What makes this different from a clone-cable project

The project is explicitly **not** trying to reproduce proprietary VCDS, ODIS, ISTA, XENTRY or other manufacturer software by copying protected code or databases.

The engineering goal is functional interoperability where technically and legally possible, using:

- public standards,
- public technical documentation,
- independently engineered protocol implementations,
- licensed data,
- OEM information where access and licensing permit it,
- and documented reverse engineering where lawful and appropriate.

This creates a sustainable open-source foundation rather than a temporary clone.

## Hardware + software must evolve together

The long-term architecture treats the adapter and application as two independently evolving layers:

```text
                 WiCAN ecosystem
                       |
          +------------+------------+
          |                         |
     Hardware layer            Software layer
          |                         |
   CAN transceivers          Android application
   Wi-Fi / BLE / USB         protocol libraries
   logging/storage           vehicle profiles
   firmware                  diagnostic workflows
   future bus hardware       knowledge base
                              simulator/replay
```

A new adapter revision may add a better transceiver, another physical bus, improved processing, storage or future CAN-FD/Ethernet capability. The application should discover capabilities instead of assuming them.

Likewise, software can gain support for a new vehicle without requiring a new adapter when the existing hardware already provides the necessary physical and protocol capabilities.

## Capability-first, not marketing-first

A central principle is:

> **Never say “works with all cars” when the evidence only proves one vehicle, ECU or protocol.**

Every capability should be associated with its scope:

- vehicle make/model/generation/year,
- VIN where appropriate,
- ECU,
- hardware/firmware,
- protocol and addressing,
- region where relevant,
- security requirements,
- required additional hardware,
- verification level.

The UI should be able to say:

- AVAILABLE
- AVAILABLE WITH PREREQUISITES
- REQUIRES OEM SECURITY
- REQUIRES ADDITIONAL HARDWARE
- NOT SUPPORTED
- UNKNOWN

That honesty is part of the product, not a weakness.

## Why developers should join

A contributor does not need to own every vehicle in the world.

A developer can add one durable building block:

- a protocol parser,
- a transport implementation,
- an ECU decoder,
- a vehicle profile,
- a replay capture,
- a test vector,
- a DTC explanation,
- a battery-health algorithm,
- a UI component,
- or a source/provenance record.

That building block can then be reused by every later vehicle profile that needs it.

**The goal is to turn automotive diagnostics from a collection of disposable vehicle-specific tools into a continuously growing open platform.**

## Long-term success criterion

The project succeeds if a user can think:

> “I bought WiCAN once. Five years later I changed from a Škoda to a Tesla. I did not throw the adapter away. I updated the open software, selected the new vehicle profile, and kept building on the same diagnostic platform.”

And a developer can think:

> “I implemented this once. It is not locked to one car. The next manufacturer can reuse it.”

That is the ecosystem we want to build.

## Non-regression rule

A missing capability is not deleted from the vision because it is difficult today.

Instead:

**KEEP TARGET → MARK STATUS → DOCUMENT BLOCKER → CREATE/UPDATE ISSUE → IMPLEMENT WHEN EVIDENCE, HARDWARE, PROTOCOL, LICENSING AND SAFETY REQUIREMENTS ARE MET.**

This keeps the roadmap ambitious without turning the README into unsupported marketing claims.
