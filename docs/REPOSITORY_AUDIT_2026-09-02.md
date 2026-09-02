# AutoDiag-WiCAN-Pro — Repository Audit

Date: 2026-09-02
Scope: current `main` branch inspected through GitHub repository contents and source files.

## Executive summary

The repository has progressed beyond the older documentation-only description. The Android core already contains a working foundation for:

- WiCAN transport abstraction
- TCP transport and simulator transport
- mDNS discovery
- ELM327 initialization and serialized command execution
- buffered ELM response collection until `>`
- Capability Discovery for adapter communication, protocol, VIN, Mode 03 and Mode 01
- a first Mode 01 decoder for RPM, vehicle speed, coolant temperature and MAP
- simulator-path testing

The main gap is no longer basic connectivity. The next architectural step is to turn individual protocol operations into a coherent diagnostic data/evidence pipeline that can support replay, automated tests, EV analysis and reports without losing provenance or verification state.

## Important status correction

`AI_HANDOFF.md` describes Mode 01 value parsing as missing, but the current tree contains `android/core/src/main/java/com/autodiag/core/obd/Mode01Decoder.kt`. It currently decodes PID `0x0C`, `0x0D`, `0x05` and `0x0B`; it is therefore **partially implemented**, not absent.

The audit should therefore be used as the current implementation baseline rather than assuming every item in the older handoff is still accurate.

## Current implementation inventory

### Transport

Present:

- `WiCanTransport`
- `TcpWiCanTransport`
- `SimulatorWiCanTransport`
- WiCAN mDNS discovery
- reconnect-oriented TCP transport foundation

Still needed:

- explicit transport health metrics
- RX/TX counters and dropped/overrun accounting
- raw CAN/SLCAN frame stream implementation
- transport capability model (CAN, CAN-FD and other buses only when actually exposed)
- deterministic connection lifecycle/session ownership

### ELM327

Present:

- AT initialization
- command serialization with a mutex
- TCP chunk buffering until ELM prompt `>`
- configurable command timeout
- read-only command path

Still needed:

- typed ELM response model rather than returning only `String`
- protocol/header normalization
- multi-response/multi-ECU handling
- ISO-TP aware diagnostic response path
- negative response/error classification
- command/response trace objects for replay and diagnostics

### Capability Discovery

Present:

- adapter communication probe (`ATI`)
- protocol probe (`ATDP`)
- VIN probe (`0902`)
- Mode 03 probe
- Mode 01 probe
- explicit capability status and verification fields

Still needed:

- capability cache with VIN + vehicle software/firmware scope
- ECU-level capability records
- granular metric capabilities (cell voltage vs cell temperature etc.)
- CAN/CAN-FD/bus capability discovery
- discovery results persisted as evidence
- discovery timeout and partial-result policy
- distinction between unknown decoding and unavailable vehicle data throughout the UI

### Generic OBD-II

Partially present:

- Mode 01 decoder exists for a small PID subset

Missing/insufficient:

- PID metadata registry
- Mode 01 supported-PID bitmap decoding
- full standard PID decoder set as supported by the project scope
- polling/live-data engine
- DTC decoder for Mode 03/07/0A
- Mode 02 freeze frame
- Mode 04 clear-DTC operation with explicit confirmation
- I/M readiness
- Mode 05/06 support where applicable
- Mode 09 VIN/CALID/CVN normalization
- multi-ECU response aggregation
- consistent negative-response handling

### Raw CAN / SLCAN

The repository currently has a transport/link foundation, but the core diagnostic monitor functionality is still missing.

Needed:

- SLCAN/raw frame parser
- CAN frame model with timestamp, direction and source
- CAN ID filtering
- bitrate/protocol metadata
- frame-rate statistics
- bus-load estimate where measurable
- error-frame observations where exposed
- dropped/overrun counters
- capture writer
- searchable frame timeline
- safe replay of recorded frames as data, never as implicit TX

### Capture / replay

A replay index model exists in the core foundation, but the complete capture/replay pipeline is not yet represented by the current implementation inventory.

Needed:

- canonical capture format
- event + measurement + raw-frame records
- monotonic and wall-clock timestamps
- indexed seeking
- replay clock
- deterministic replay tests
- integrity/version metadata
- import/export
- explicit separation of replayed data from live vehicle data

## Critical architecture addition: Diagnostic Evidence Model

Before adding large numbers of vehicle-specific decoders, introduce a common immutable evidence model.

Suggested conceptual model:

```text
DiagnosticSession
  ├── SessionIdentity
  ├── VehicleIdentity
  ├── AdapterIdentity
  ├── CapabilitySnapshot
  ├── TransportContext
  ├── Measurements[]
  ├── DiagnosticEvents[]
  ├── DtcRecords[]
  ├── TestPhases[]
  ├── EvidenceReferences[]
  └── CaptureReference
```

Every measured/derived item should preserve, as applicable:

- value
- unit
- timestamp
- source
- ECU/module identity
- raw representation
- quality
- availability state
- verification state
- vehicle/profile scope
- calculation/inference marker

Required state semantics:

- `AVAILABLE`
- `PARTIAL`
- `UNAVAILABLE`
- `UNKNOWN`
- `ERROR`
- `UNVERIFIED`
- `PARTIALLY_VERIFIED`
- `VERIFIED`

Do not collapse communication failure, missing vehicle data and unknown decoding into one state.

## Second critical architecture addition: Diagnostic Event Stream

Introduce a typed event stream shared by live sessions, simulator and replay.

Examples:

```text
SESSION_STARTED
TRANSPORT_CONNECTED
ELM_INITIALIZED
CAPABILITY_DISCOVERED
VEHICLE_IDENTIFIED
ECU_DISCOVERED
MEASUREMENT_RECEIVED
DTC_RECEIVED
TEST_PHASE_STARTED
TEST_PHASE_COMPLETED
LOAD_STARTED
LOAD_STOPPED
RECOVERY_STARTED
BUS_HEALTH_CHANGED
CAPTURE_STARTED
CAPTURE_STOPPED
ANALYSIS_COMPLETED
SESSION_ENDED
```

This becomes the common backbone for replay, AUTO TEST, audit logs, debugging and report generation.

## Third critical architecture addition: typed diagnostic protocol layers

Do not put vehicle-specific decoding directly into transport classes.

Recommended dependency direction:

```text
UI
 ↓
Use cases / session orchestration
 ↓
Diagnostic analysis
 ↓
Vehicle profile decoders
 ↓
UDS / ISO-TP / OBD protocol layers
 ↓
CAN / ELM / SLCAN framing
 ↓
Transport
```

The transport must remain unaware of Tesla/VAG/OBD business meaning.

## Mode 01 implementation gap

`Mode01Decoder` is a good start but needs to become a registry-driven decoder rather than a growing `when` statement.

Recommended types:

```text
ObdPidDefinition
  pid
  request
  minimumBytes
  decoder
  unit
  label
  verification
  source

ObdPidResult
  definition
  rawBytes
  value
  timestamp
  quality
  availability
  provenance
```

The registry should also support supported-PID bitmap interpretation so the polling engine does not blindly request unavailable PIDs.

## DTC implementation gap

Implement a protocol-level DTC parser first. Do not attach repair advice to the parser.

Pipeline:

```text
raw response
 → normalized DTC record
 → vehicle/ECU scope
 → knowledge-base lookup
 → sourced explanation
 → optional analysis
```

DTC records need:

- code
- status/presence context
- ECU/source
- raw response
- timestamp
- protocol/mode
- verification/provenance

## ISO-TP / UDS

These are major missing infrastructure pieces for the planned Tesla/vehicle-specific work.

Implement read-only foundations before manufacturer decoders:

1. CAN addressing model
2. ISO-TP single/first/consecutive/flow-control frames
3. reassembly and timeout handling
4. UDS positive/negative response model
5. read-only services needed by verified profiles

No write/control service should be enabled merely because a protocol layer exists.

## Live-data engine

Needed as a separate subsystem, not inside `Mode01Decoder`.

Responsibilities:

- PID scheduling
- per-PID period
- request serialization
- response timeout
- retry/backoff
- stale-data detection
- timestamping
- quality calculation
- adaptive sampling hooks
- cancellation
- capture integration

The scheduler should consume capability information and avoid requesting known-unavailable metrics.

## EV / Tesla layer

Do not begin with hard-coded UI values.

Build:

```text
VehicleProfile
  → Capability mapping
  → Verified decoder registry
  → Typed EV measurements
  → Time-series store
  → Analysis
```

The existing project requirements correctly demand scope and verification for Tesla/CAN/battery information. Preserve that rule.

Required future EV measurement model should be able to represent:

- pack voltage/current/power
- SOC
- temperatures and thermal gradients
- cell/module/brick voltage
- cell/module identity
- charging context
- drive-unit measurements
- vehicle-reported isolation
- vehicle-reported SOH where exposed

Missing data must remain `UNAVAILABLE`, not be synthesized.

## Battery health test

The repository specifications are already strong here. Implementation should follow the defined stages:

```text
REST → LOAD → RECOVERY → CHARGE → ANALYZE
```

with contextual samples and confidence/provenance.

Missing implementation pieces:

- test orchestrator
- phase state machine
- time-series persistence
- adaptive sampling engine
- threshold/evidence resolver
- fingerprint/history store
- confidence calculation
- replayable test result
- report model

Do not implement a universal cell-voltage mV failure threshold.

## PRE-PURCHASE / AUTO TEST

The repository already has a clear specification but not the complete runtime orchestration.

Recommended implementation:

```text
PreTestGate
 → Identify
 → DiscoverCapabilities
 → CollectBasicDiagnostics
 → CollectLiveData
 → CollectEV/HV (if available)
 → BusHealth
 → TestPhases
 → Analyze
 → GenerateReport
```

Each stage returns an explicit result including `NOT_AVAILABLE` where unsupported.

## Reporting

A report model should be introduced before UI report rendering.

Suggested structure:

```text
DiagnosticReport
  vehicle
  session
  scope
  summary
  findings[]
  measurements[]
  unavailableChecks[]
  confidence
  provenance[]
  evidence[]
  timelineReference
```

This is important for the intended pre-purchase workflow because the report must say both what was found and what could not be tested.

## Knowledge Base

The repository has the documentation architecture but needs runtime integration.

Needed:

- normalized DTC/alert key
- vehicle-generation scope
- source type (`OEM`, `ENGINEERING`, `COMMUNITY`)
- source URL/reference
- verification state
- validity/review state
- explanation
- troubleshooting reference
- service reference
- related measurement IDs

Community or generated text must never be displayed as OEM procedure.

## ICE / Hybrid expansion

The implementation backlog already identifies useful future diagnostics:

- odometer cross-check
- DPF/OPF
- SCR/AdBlue/NOx
- misfire counters
- injector corrections
- requested vs actual boost
- requested vs actual rail pressure
- thermal context

These should be implemented through the same typed measurement/evidence pipeline rather than as special-case screens.

## Automation

`AutomationEngine` exists as a foundation, but the runtime ecosystem still needs:

- persistent rule representation
- trigger evaluation against typed measurements/events
- dry-run/replay
- cooldown/rate limiting
- audit log
- notification abstraction
- MQTT adapter
- Home Assistant adapter
- stale-data policy

Keep `READ_LOG_ANALYZE` and `NOTIFY_ALERT` separate from `WRITE_COMMAND`.

## UI gaps

After the core pipeline exists, the UI should expose:

1. connection/transport state
2. capability matrix
3. live data dashboard
4. DTC list + source/provenance
5. raw CAN monitor
6. capture/replay timeline
7. diagnostic session history
8. AUTO TEST progress and stage results
9. driver summary
10. expert numerical view
11. report export

The driver UI must not hide important uncertainty. Expert UI should expose raw values, timestamps and provenance.

## Testing gaps

Current simulator-path coverage is useful but insufficient for the planned scope.

Add test suites for:

### Unit

- ELM response normalization
- OBD framing
- Mode 01 PID registry
- DTC parsing
- ISO-TP
- UDS negative responses
- CAN frame parsing
- timestamp/index behavior
- data-quality transitions
- confidence calculations

### Simulator/replay

- split TCP chunks
- delayed prompt
- NO DATA
- malformed response
- multiple ECU responses
- multi-frame DTC/UDS
- CAN bursts
- dropped frames
- load/recovery phase transitions
- unavailable EV signals

### Safety regression

- no write command from read-only session
- no unverified signal marked verified
- no unavailable stage marked PASS/FAIL
- no Riso MΩ derived from status-only data
- no battery failure conclusion from one low loaded cell

## Recommended implementation order

### Sprint 1 — foundation correctness

1. Update `AI_HANDOFF.md` to match current code.
2. Add typed diagnostic evidence/provenance model.
3. Add diagnostic event model.
4. Add typed ELM response classification.
5. Expand simulator fixtures and parser tests.

### Sprint 2 — generic OBD

6. PID registry.
7. Supported-PID bitmap.
8. Mode 01 scheduler/live-data engine.
9. DTC parser.
10. Freeze Frame.
11. Readiness.
12. Mode 09 identity normalization.

### Sprint 3 — raw CAN and replay

13. SLCAN parser.
14. CAN frame model.
15. capture writer.
16. replay clock.
17. timeline/index UI.
18. bus-health metrics.

### Sprint 4 — protocol infrastructure

19. ISO-TP.
20. read-only UDS model/services.
21. multi-ECU addressing/aggregation.

### Sprint 5 — test orchestration

22. DiagnosticSession manager.
23. PRE-PURCHASE state machine.
24. AUTO TEST state machine.
25. time-series store.
26. adaptive sampling.
27. report model/export.

### Sprint 6 — verified EV profiles

28. VehicleProfile framework.
29. verified Tesla decoder registry.
30. EV measurement models.
31. battery fingerprint/history.
32. battery health analysis.
33. Riso evidence model.
34. charging analysis.

### Sprint 7 — integrations

35. Knowledge Base runtime.
36. MQTT.
37. Home Assistant.
38. remote parked telemetry.
39. automation audit/dry-run.

### Sprint 8 — manufacturer expansion

40. VAG/UDS profiles.
41. Hyundai/Kia.
42. BMW.
43. Mercedes.
44. Renault/Nissan.
45. Mitsubishi.

## Do not implement yet

- arbitrary CAN write commands
- generic coding/adaptation UI
- actuator tests without a safety review
- hard-coded Tesla signal assumptions without verification
- universal battery/Riso thresholds
- automatic repair instructions without verified sources

## Definition of done

A feature is complete only when:

1. implementation exists,
2. unit tests exist,
3. simulator/replay coverage exists where applicable,
4. source/provenance is recorded,
5. verification scope is explicit,
6. unsupported data is represented explicitly,
7. documentation is updated,
8. real-vehicle validation is completed when required by the feature.

## Conclusion

The project does not primarily need more UI screens. It needs a robust evidence-centric diagnostic core between transport and the future vehicle-specific analysis.

The highest-value architectural work is therefore:

**Diagnostic Evidence Model → Event Stream → typed protocol layers → Live Data engine → capture/replay → test orchestration → verified vehicle decoders.**

This sequence preserves the project's READ-first safety model while creating the foundation required for the planned EV, Tesla, pre-purchase and automation functionality.
