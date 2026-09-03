# Open-source automotive diagnostics research — 2026-09-03

This document records reusable architectural ideas found in public projects. It is a research reference, not a license to copy proprietary databases, vehicle mappings, binaries, or OEM material.

## OBDium

OBDium is a Rust/Tauri diagnostic application focused on ELM327, offline operation, live OBD-II data, DTCs, VIN decoding, readiness, graphs, PID lists and replay/demo data. Its repository explicitly supports sharing recorded request files for community use. The useful pattern for AutoDiag is a deterministic local data/replay layer and a shareable, sanitised diagnostic-session format.

Source: https://github.com/provrb/obdium

## OpenXC

OpenXC separates a reusable vehicle interface from application-level interpretation. Its firmware can expose raw CAN traffic as well as translated signals. Its mapping model describes bus, message ID, signal bit position/size, factor, offset, decoder, frequency limits and optional writable signals. Message sets can be composed and generated from JSON rather than hand-editing generated source.

Useful AutoDiag concepts:
- raw CAN as a first-class diagnostic stream;
- typed/translated vehicle signals above raw frames;
- declarative message/signal definitions;
- explicit sampling-frequency limits;
- one-time and recurring diagnostic requests;
- separate read and write capabilities;
- generated code/data should be derived from a canonical mapping, not hand-edited.

Sources:
- https://github.com/openxc/vi-firmware
- https://openxcplatform.com/
- https://vi-firmware.openxcplatform.com/en/latest/config/reference.html
- https://vi-firmware.openxcplatform.com/en/master/advanced/lowlevel.html

## OpenVehicleDiag

OpenVehicleDiag is a Rust cross-platform ECU diagnostic platform using Pass-Thru/J2534 and exposes an ECU JSON schema. Its repository also contains tooling around Mercedes CBF conversion; a separate SMR parser was removed after a DMCA request. This is a strong warning for AutoDiag: vehicle-specific databases and reverse-engineered OEM files require explicit provenance and licensing controls.

Useful AutoDiag concepts:
- protocol/transport abstraction;
- ECU definitions as structured data;
- JSON schema for diagnostic definitions;
- Pass-Thru as an adapter/API layer, not a vehicle protocol;
- strict separation between open code and restricted OEM data.

Source: https://github.com/rnd-ash/OpenVehicleDiag

## Libre Diagnostic

Libre Diagnostic is an open-source ELM327/OBD-II application with DTC and brand-specific command support. Its project emphasizes local/session data, explicit export, privacy and future modular/plugin and IoT directions. Its safety disclaimer is also useful as a model for diagnostics UI documentation.

Useful AutoDiag concepts:
- simulator and test directories;
- explicit local export rather than hidden telemetry;
- modular brand-specific commands;
- privacy-by-default diagnostic sessions;
- future hardware abstraction.

Source: https://github.com/Libre-Diagnosctic/libre-automotive-diagnostic

## FreeDiag

FreeDiag is an older mostly-OBD-II-compliant vehicle diagnostics suite. It is useful historically as evidence that a broad diagnostic tool can be organized around protocol/scantool abstractions, but its age means its implementation should not be treated as a modern architecture reference without verification.

Source: https://sourceforge.net/projects/freediag/

## PyOBD / OBDTester

The referenced PyOBD/OBDTester material should be treated as historical/reference material unless a specific implementation and license are verified. AutoDiag should prefer its own typed protocol models and tests over copying legacy implementation details.

## AutoDiag architectural conclusions

The strongest common pattern is a layered architecture:

```text
WiCAN / other VCI
        |
transport + raw frames
        |
protocol layer (CAN / ISO-TP / UDS / OBD-II / KWP / ...)
        |
vehicle + ECU capability scope
        |
typed diagnostic data / DTC / live data / service functions
        |
evidence + session + replay
        |
knowledge / repair intelligence / community contributions
```

### Important additions to the AutoDiag roadmap

1. Keep raw CAN and typed signals side-by-side.
2. Add a declarative vehicle signal definition format with bit position, bit size, byte order, factor, offset, unit, decoder, validity and sampling limits.
3. Make mappings composable by vehicle platform/generation instead of duplicating whole files.
4. Add a canonical diagnostic session/replay format that can be anonymised and shared.
5. Keep transport adapters separate from diagnostic protocols.
6. Treat J2534/Pass-Thru as an adapter/API capability, not a protocol.
7. Keep write capability explicit and gated; do not design the core as permanently read-only.
8. Store provenance/license/verification with every imported or community knowledge item.
9. Never import a third-party proprietary database merely because it is technically accessible.
10. Preserve a clear distinction between code that can be reused under its license and data that may not be redistributed.

## License and provenance rule

AutoDiag may learn architectural ideas from public projects and may reuse code only where the applicable license permits it and attribution/notice requirements are preserved. Proprietary OEM databases, paid repair databases, copyrighted manuals, credentials, security secrets and material removed for legal reasons are not to be copied into the public knowledge base.
