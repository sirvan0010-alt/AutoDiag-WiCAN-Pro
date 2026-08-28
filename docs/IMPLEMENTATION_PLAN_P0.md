# AutoDiag-WiCAN-Pro — P0 Implementation Plan

## Goal

Turn the documentation-first foundation into the first **real, testable Android application** without pretending that unsupported vehicle signals exist.

## Milestone P0.1 — transport foundation

- [ ] Kotlin/JVM transport interfaces remain platform-independent.
- [ ] Implement `TcpElm327Transport` for WiCAN PRO TCP `:3333`.
- [ ] Implement `TcpSlcanTransport` for WiCAN PRO TCP `:23`.
- [ ] Explicit connection lifecycle: disconnected / connecting / connected / error / closed.
- [ ] Socket timeouts and cancellation must be deterministic.
- [ ] No blocking network work on the Android main thread.
- [ ] Every received/transmitted payload can optionally be captured for diagnostics.

## Milestone P0.2 — mDNS discovery

- [ ] Android NSD/mDNS discovery for WiCAN devices.
- [ ] Display hostname/IP/service metadata.
- [ ] Resolve `wican_*.local` where supported.
- [ ] Discovery failure must not be confused with device absence.
- [ ] Manual IP connection remains available.

## Milestone P0.3 — simulator

- [ ] Fake WiCAN TCP endpoint for automated tests.
- [ ] Deterministic ELM327 responses.
- [ ] Deterministic SLCAN frames.
- [ ] Malformed/timeout/disconnect scenarios.
- [ ] Replay recorded captures without a vehicle.

## Milestone P0.4 — Capability Discovery

Discovery runs before detailed diagnostics.

For each capability store:

- vehicle identity scope
- VIN when available
- ECU/module identity
- firmware/software version when available
- capability name
- status: `available`, `partial`, `unavailable`, `unknown`, `error`
- source/provenance
- timestamp
- probe result

Cache key:

`vehicle identity + VIN (when available) + ECU + firmware/software scope + decoder version`

A timeout is not equivalent to `unavailable`.

## Milestone P0.5 — capture and replay

Every test session must have a stable session ID and timestamped samples.

Required context where available:

- pack voltage
- battery current
- SOC
- battery temperature
- cell/module voltage
- cell/module temperature
- charging power
- phase
- source
- verification state

Replay must support:

`Pack → Module/Brick → Cell → timestamp`

The expert view must allow scrubbing to an exact timestamp and inspecting the values available at that instant. Large captures must use indexed timestamps/binary search rather than scanning the complete dataset for every UI update.

## Milestone P0.6 — safe AUTO TEST orchestration

Initial automatic test is **read-only**:

1. Connection check
2. Vehicle identification
3. Capability Discovery
4. DTC/alert read
5. Battery snapshot
6. Cell/module snapshot when available
7. HV isolation/Riso data when available
8. Thermal snapshot
9. Charging data when available
10. Drive-unit data when available
11. Optional controlled road capture
12. Analysis
13. Report
14. Replay

Unsupported stages become `NOT_AVAILABLE`.

## Milestone P0.7 — diagnostic explanations

A recognized DTC/alert should open a structured entry:

`code → meaning → affected system → symptoms → sourced checks → official explanation → official troubleshooting/service reference → related measurements`

Official OEM sources and community reverse-engineering sources must remain visually separated.

If an official repair procedure cannot be verified, AutoDiag must say so rather than generating one and presenting it as OEM procedure.

## Milestone P0.8 — battery analysis

The engine uses:

- STATIC
- LOAD
- RECOVERY
- TREND
- CONFIDENCE

No universal hardcoded mV threshold.

During acceleration the app must **not** label the lowest-voltage cell as the weakest cell solely because it is the minimum at peak load.

During AC/DC charging, if cell-level data exist, record and replay each available cell/module over time.

## Milestone P0.9 — Riso/HV isolation

Distinguish:

- vehicle-reported numerical isolation
- vehicle-reported status
- physical test result
- raw/undecoded response

Never manufacture an MΩ value from a status or generic assumption.

## Milestone P0.10 — remote automation

Read-only automation comes after stable transport/capture:

- scheduled telemetry
- home Wi-Fi
- MQTT/Home Assistant
- notifications
- rate limiting
- audit log
- JSON/YAML rules
- replay/dry-run before activation

No silent CAN write operations.

## Definition of done for P0

P0 is complete only when:

- the Android app builds reproducibly in CI;
- unit tests cover transport, parsing and capability state transitions;
- the simulator can exercise success, timeout, malformed data and disconnect paths;
- a real WiCAN PRO can be discovered or manually connected;
- TCP `:3333` and `:23` traffic is captured without blocking the UI;
- Capability Discovery produces a persisted, scoped snapshot;
- a capture can be saved and replayed;
- AUTO TEST can run entirely read-only;
- no unsupported Tesla signal is presented as verified.

## Explicitly NOT P0

- CAN write/control
- vehicle coding
- immobilizer/security bypass
- arbitrary UDS routines
- claiming OEM service procedures without verified sources
- universal battery-health percentages
- pretending that every EV exposes per-cell data
