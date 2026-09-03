# Open-source diagnostic landscape

This document turns the reviewed public projects into implementation requirements for AutoDiag-WiCAN-Pro. It is not a license to copy third-party databases, OEM data, credentials or restricted vehicle mappings.

## Projects reviewed

| Project | Reusable idea for AutoDiag |
|---|---|
| OBDium | Offline-first OBD, deterministic replay/demo data, simple shareable session files |
| OpenXC | Raw CAN + translated signals, declarative signal definitions, recurring diagnostic requests, writable signals separated explicitly |
| OpenVehicleDiag | Transport/protocol separation, ECU descriptors, JSON schema, J2534/Pass-Thru adapter concept, provenance/legal boundary |
| o3DIAG | Small diagnostic core, emulator, explicit DTC clearing, timestamped logs |
| Libre Diagnostic | Local/session data, modular manufacturer commands, simulator/tests, privacy-first design |
| FreeDiag | Historical layered scantool/protocol organization and CLI-oriented diagnostics |
| PyOBD / OBDTester | Simple high-level OBD API patterns; legacy reference only until implementation/license is verified |

## Architecture adopted by AutoDiag

```text
WiCAN / other VCI
        |
        +-- transport
        |     +-- TCP / Wi-Fi
        |     +-- BLE where verified
        |     +-- SLCAN / raw CAN
        |     +-- future J2534 / Pass-Thru adapters
        |
        +-- raw CAN / ISO-TP
        |
        +-- protocol layer
        |     +-- OBD-II
        |     +-- UDS
        |     +-- KWP
        |     +-- future verified protocols
        |
        +-- vehicle + ECU scope
        |
        +-- typed signals / DTC / live data / service functions
        |
        +-- evidence / session / replay
        |
        +-- knowledge / repair / community
```

## P0 requirements

1. Raw CAN remains first-class data.
2. Typed signals sit above raw frames rather than replacing them.
3. Vehicle mappings are declarative data, not duplicated hand-written logic.
4. Each mapping records message ID, bus, bit position/size, byte order, factor, offset, unit, decoder, validity and sampling limits where applicable.
5. One-time and recurring diagnostic requests are separate concepts.
6. Reads, service writes, configuration writes, actuator control, programming and security-critical operations have distinct capability classes.
7. Every diagnostic session can be captured and replayed deterministically after anonymisation.
8. DTC clear is an explicit state-changing operation with verification after the action.
9. Simulator/emulator paths are part of the development architecture, not an afterthought.
10. Transport adapters are independent of vehicle diagnostic protocols.

## P1 requirements

- Composable mappings by platform/generation.
- Multi-ECU discovery and exact ECU scope.
- Session metadata and provenance.
- Local-first privacy and explicit export.
- Knowledge entries carrying source, scope, verification and license status.
- Generated code/data derived from canonical mapping definitions.
- J2534/Pass-Thru treated as an adapter/API capability, never as a vehicle protocol.

## P2 requirements

- Community contribution pipeline.
- Sanitised diagnostic trace sharing.
- Import/export tooling for mapping and replay formats.
- Additional vehicle profiles only when evidence supports them.
- Plugin/provider boundaries for licensed databases.

## Non-goals

AutoDiag must not copy proprietary OEM databases, copyrighted service manuals, paid repair databases, security credentials or legally restricted files into the public repository merely because they are technically accessible. Private archives and public reference metadata remain separate.

## Tesla consequence

Tesla source material belongs in the private/reference workflow documented in `docs/TESLA_LOCAL_ARCHIVE.md`. The public repository stores URLs, provenance and independently authored structured knowledge. The Tesla reference registry lives under `diagnostics/sources/`.

## Relationship to current roadmap

This landscape confirms the existing direction: finish the transport and diagnostic pipeline, then expand exact vehicle/ECU discovery, Tesla READ, generic live data, DTC evidence, repair intelligence, replay/simulator, and finally controlled WRITE capabilities.
