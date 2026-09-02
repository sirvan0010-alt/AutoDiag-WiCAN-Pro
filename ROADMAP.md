# AutoDiag-WiCAN-Pro Roadmap

## Master specification

The complete set of architecture decisions, requirements and implementation stages agreed on 2026-09-02 is preserved in:

- `docs/TODAY_MASTER_PLAN_2026-09-02.md`
- `docs/REPAIR_KNOWLEDGE_ESTIMATE_ARCHITECTURE.md`
- `docs/OSCILLOSCOPE_ARCHITECTURE.md`

The master plan is the detailed source of truth; this roadmap is the execution checklist.

## Phase 0 — Foundation

- [x] Repository created
- [x] AI project context
- [x] Architecture overview
- [x] Diagnostic knowledge-base architecture
- [x] Repair knowledge / parts / labor / price architecture
- [x] Tesla public DIY source captured
- [x] Tesla market/region identification specification
- [x] Capability Discovery specification
- [x] Automation Engine specification
- [x] Implementation task backlog
- [x] Master plan for 2026-09-02 requirements
- [ ] Safety policy
- [ ] Android project skeleton hardening
- [ ] CI build/test pipeline
- [x] Vehicle identity/scope model
- [x] ECU identity/capability model foundation
- [x] Typed measurement/evidence model foundation
- [x] Diagnostic Event Stream

## Phase 1 — WiCAN connectivity

- [ ] WiCAN mDNS discovery
- [ ] Manual IP fallback
- [ ] TCP ELM327 transport (:3333)
- [ ] TCP SLCAN/raw CAN transport (:23)
- [ ] WebSocket/UDP/BLE paths where verified for the target firmware
- [ ] Connection state and reconnect handling
- [ ] Raw TX/RX logging
- [ ] Communication latency/sampling metrics
- [ ] AUTO protocol detection
- [ ] Manual protocol selection

## Phase 2 — Simulator and CAN tools

- [ ] WiCAN TCP mock
- [x] CAN frame model
- [x] CAN ID filtering foundation
- [x] CAN frame/bus statistics foundation
- [ ] CAN frame simulator
- [ ] Capture replay
- [ ] CAN ID statistics UI
- [ ] Export
- [ ] Replay safety isolation

## Phase 3 — Protocol stack and Generic OBD-II

- [ ] Typed ELM327 response layer
- [x] ISO-TP frame classification/reassembly foundation
- [x] UDS positive/negative response foundation
- [x] UDS service risk classification
- [x] UDS capability gate foundation
- [ ] Multi-ECU response handling
- [x] Registry-driven Mode 01 PIDs
- [ ] Supported-PID discovery
- [ ] Mode 02 freeze frame
- [ ] Mode 03 DTC reading
- [ ] Mode 04 clear DTC (explicitly gated)
- [ ] Mode 05 oxygen sensor monitoring
- [ ] Mode 06 non-continuous monitor/test-result decoding
- [ ] Mode 07 pending DTC
- [ ] Mode 09 VIN/CALID/CVN
- [ ] Mode 0A permanent DTC
- [ ] Readiness
- [ ] Live Data scheduler
- [ ] Adaptive sampling 10/20/50 Hz/MAX/AUTO
- [ ] Generic OBD-II PID/sensor coverage

## Phase 4 — ECU discovery and vehicle identification

- [ ] Functional ECU scan
- [ ] Physical ECU discovery
- [ ] ECU identification
- [ ] Software/hardware/calibration identification
- [ ] Capability cache keyed by exact vehicle/ECU scope
- [ ] Vehicle make/model/year identification
- [ ] Tesla market/region identification with explicit source
- [ ] US-market warning when reliably identified
- [ ] Vehicle profile resolver
- [ ] Vehicle-specific capability matrix

## Phase 5 — Live Data / Dashboard / HUD

- [ ] 1–16 selectable live values
- [ ] Stacked time-series graphs
- [ ] Rolling buffer
- [ ] Landscape phone layout
- [ ] Dashboard gauges
- [ ] Mirrored HUD mode
- [ ] Communication speed/latency/sampling display
- [ ] Measurement quality/verification indicators

## Phase 6 — Tesla READ diagnostics

- [ ] Exact vehicle/profile matching
- [ ] Verified CAN signal database
- [ ] Model 3/Y identification where possible
- [ ] Battery telemetry
- [ ] Per-module/per-cell telemetry where the vehicle exposes it
- [ ] Charging telemetry
- [ ] AC charging analysis
- [ ] DC fast-charging analysis
- [ ] Thermal telemetry
- [ ] Drive-unit telemetry
- [ ] HV isolation/Riso data where exposed
- [ ] Contactor/HV state data where safely available
- [ ] DTC/status information where safely available
- [ ] Public Tesla service/DIY source metadata

## Phase 7 — Automated EV health test

- [ ] Data-quality checks
- [ ] Battery health analysis engine
- [ ] STATIC / LOAD / RECOVERY / TREND / CONFIDENCE pipeline
- [ ] Context-aware cell imbalance analysis
- [ ] Battery Fingerprint / vehicle self-history
- [ ] Acceleration/load test
- [ ] AC charging test
- [ ] DC fast-charging test
- [ ] Cell/module tracking during charging
- [ ] Temperature and thermal-gradient analysis
- [ ] HV isolation/Riso analysis with source provenance
- [ ] Drive-unit checks
- [ ] Diagnostic score with confidence level
- [ ] Replayable test log
- [ ] Timestamp-indexed replay scrubber
- [ ] Click-through Pack → Module/Brick → Cell inspection
- [ ] Simple human-readable results
- [ ] Expert battery/module/cell visualization
- [ ] Exportable report

## Phase 8 — Diagnostic Knowledge + Repair Intelligence

- [ ] DTC/vehicle-alert normalization
- [ ] OEM description mapping
- [ ] Verified OEM explanation links
- [ ] OEM troubleshooting links
- [ ] OEM service/repair links where legitimately public
- [ ] Source and verification display
- [ ] Vehicle-generation-specific procedure matching
- [ ] Community references kept separate from OEM procedures
- [ ] `needs_review` handling for changed/removed OEM links
- [ ] RepairSource model
- [ ] RepairProcedure model
- [ ] RepairPart model
- [ ] LaborEstimate model
- [ ] PriceEstimate model
- [ ] RepairEstimateEngine
- [ ] Exact vehicle/ECU matching
- [ ] DTC → possible causes → diagnostic checks → repair references
- [ ] Parts and OEM part-number provenance
- [ ] Labor and parts cost ranges
- [ ] DIY / service UI modes
- [ ] Licensed provider integration boundary

## Phase 9 — Pre-purchase automation/reporting

- [ ] CONNECT → IDENTIFY → DISCOVER → DTC → FREEZE FRAME → READINESS → LIVE DATA → MONITORS → EV TESTS → ANALYZE → REPAIR ESTIMATE → REPORT state machine
- [ ] Evidence aggregation
- [ ] Report model
- [ ] PASS/FAIL only where supported
- [ ] Repair-cost summary
- [ ] Parts/labor/procedure references
- [ ] Vehicle-scope and confidence display

## Phase 10 — Remote monitoring and automation

- [ ] Background monitoring
- [ ] Local Wi-Fi operation
- [ ] Remote telemetry while parked at home
- [ ] MQTT integration
- [ ] Home Assistant integration
- [ ] Notifications with rate limits/cooldowns
- [ ] Rule engine stored as JSON/YAML
- [ ] Rule replay/dry-run simulator
- [ ] Rule execution audit log
- [ ] Historical telemetry
- [ ] User-defined dashboard/widgets
- [ ] Automation triggers based on verified telemetry
- [ ] One-tap profile-driven AUTO TEST / "Sexy Button"
- [ ] Charge-cost analysis
- [ ] Vampire-drain analysis
- [ ] Geofencing

## Phase 11 — Other vehicles

- [ ] VAG profiles: VW / Audi / Škoda / SEAT / CUPRA
- [ ] UDS layer
- [ ] KWP2000 layer
- [ ] K-Line-capable transport where required
- [ ] Hyundai/Kia profiles
- [ ] BMW profiles
- [ ] Mercedes profiles
- [ ] Renault/Dacia profiles
- [ ] Nissan profiles
- [ ] Mitsubishi profiles
- [ ] Toyota/Ford/GM/Stellantis/Volvo/Polestar profiles
- [ ] Additional manufacturers

## Phase 12 — Isolated WRITE / service framework

- [x] UDS service risk classification
- [x] Capability gate foundation
- [ ] UDS service execution framework
- [ ] Diagnostic Session Control 0x10
- [ ] Controlled Security Access 0x27
- [ ] Read Data By Identifier 0x22
- [ ] Write Data By Identifier 0x2E in isolated layer only
- [ ] Routine Control 0x31 in isolated layer only
- [ ] VAG coding / Long Coding / adaptations with exact-scope evidence
- [ ] Explicit confirmation UI
- [ ] Per-vehicle capability matrix
- [ ] Safety review
- [ ] Dry-run/simulator first
- [ ] No unverified CAN control commands

## Phase 13 — Integrated automotive oscilloscope

- [x] Oscilloscope capability model
- [x] Timestamped sample/capture model
- [x] Basic measurements: min/max/peak-to-peak/mean
- [x] Rising/falling threshold trigger foundation
- [x] Streaming capture engine
- [x] Ring buffer + pre/post-trigger capture
- [x] Frequency/period measurement
- [x] Duty-cycle measurement
- [x] RMS measurement foundation
- [x] Waveform viewer scale/offset model
- [x] Cursor measurement model
- [ ] Android waveform renderer
- [ ] Zoom/pan/freeze interaction
- [ ] CAN/UDS/DTC event correlation
- [ ] CSV export
- [ ] Replay format
- [ ] Multi-channel synchronization
- [ ] Verified WiCAN-compatible measurement hardware path
- [ ] Probe/input electrical-limit enforcement
- [ ] Automotive-safe isolation/protection hardware profile

## Long-term compatibility

- [ ] Current Android target SDK maintenance
- [ ] Automated compatibility/build testing
- [ ] Dependency update policy
- [ ] Migration tests for future Android releases

## Definition of done

A feature is not considered complete merely because the code compiles. Vehicle-specific features require reproducible tests and, where applicable, validation against real vehicle data. The implementation must preserve evidence, source, vehicle/ECU scope, uncertainty and safety state.
