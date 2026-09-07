# canair → AutoDiag-WiCAN-Pro import map

Status: **reference extraction / implementation plan**

Source repository: https://github.com/philipkocanda/canair
Source branch: `main`
Source tree SHA inspected: `92f742d9deaa69be77af1d16d08cf6a3436e1627`

## Rule

`canair` is a **reference architecture, not a runtime dependency**. Its Python implementation is not copied into Android. Concepts are independently reimplemented in Kotlin and must pass AutoDiag unit, simulator/replay and vehicle-verification gates.

## Transport layer

| canair source | Concept | AutoDiag target | Action |
|---|---|---|---|
| `canlib/transport/config.py` | explicit transport registry, precedence, fallback, bitrate/port resolution | `android/core` transport configuration | REIMPLEMENT |
| `canlib/transport/channel.py` | normalized CAN channel abstraction | CAN transport/channel API | REIMPLEMENT |
| `canlib/transport/isotp_params.py` | ISO-TP parameter model | ISO-TP configuration model | REIMPLEMENT |
| `canlib/transport/isotp_stack.py` | client-side ISO-TP stack | ISO-TP engine | REIMPLEMENT |
| `canlib/transport/protocol.py` | protocol abstraction | diagnostic protocol interfaces | REIMPLEMENT |
| `canlib/transport/fallback.py` | ordered transport fallback | WiCAN connection manager | ADAPT |
| `canlib/transport/elm327_session.py` | ELM terminal session semantics | existing `Elm327Session` | COMPARE / REGRESSION |
| `canlib/transport/elm327_tcp.py` | ELM327 TCP | existing TCP transport | COMPARE / REGRESSION |
| `canlib/transport/elm327_terminal.py` | terminal framing/command handling | `Elm327Session` + terminal parser | REIMPLEMENT / MERGE CONCEPTS |
| `canlib/transport/elm327_pipe.py` | pipelined terminal I/O | future concurrent diagnostic I/O | REFERENCE |
| `canlib/transport/elm327_frame_count.py` | response/frame accounting | capture statistics | ADAPT |

## Diagnostic layer

The next extraction pass must inspect and map these areas before implementation:

- UDS client and service handling
- KWP support
- request/response timing
- negative response handling
- ECU addressing
- passive CAN sniffing
- capture/replay
- signal hunt/correlation/investigation
- DBC import/export
- profile management
- tests and conformance checks

No ECU address, CAN ID, PID, signal meaning, scale or threshold is promoted merely because it exists in canair.

## Evidence pipeline

The architectural pipeline to adopt is:

```text
raw CAN capture
    ↓
frame normalization
    ↓
ISO-TP reassembly (when applicable)
    ↓
UDS/KWP diagnostic response
    ↓
decoder candidate
    ↓
signal observation
    ↓
evidence/provenance
    ↓
verification gate
    ↓
production diagnostic value
```

This integrates with AutoDiag's existing distinction between extracted/static evidence, candidate decoders, replay-tested behaviour and vehicle verification.

## Transport decision

AutoDiag should support at least these logical transport modes:

1. WiCAN SLCAN/raw CAN over TCP — primary raw diagnostic path; client-side ISO-TP is preferred where the firmware exposes raw SLCAN.
2. WiCAN ELM327 terminal — compatibility path for existing OBD functionality.
3. Simulator/replay — first-class transport for deterministic tests.

Transport selection must not silently change protocol semantics. Failed connection, unavailable capability and unknown decoding remain separate states.

## WiCAN HTTP

The canair model of querying WiCAN live configuration is useful for device discovery/configuration, but HTTP configuration must remain separate from the diagnostic CAN stream. Device configuration is not evidence that a vehicle capability exists.

## Safety boundary

No WRITE/flash implementation is imported as part of this work. Any future write path remains isolated, default-off, scope-gated, backed up, replay/simulator tested and explicitly confirmed by the user.

## Licensing/provenance

This file records architectural findings from the public repository. It does not vendor or copy the canair source tree. Any future source-derived implementation must be independently written and retain attribution/provenance where required by the source license.
