# AutoDiag-WiCAN-Pro Roadmap

> Master specification: `docs/TODAY_MASTER_PLAN_2026-09-02.md`
> Repair/estimate architecture: `docs/REPAIR_KNOWLEDGE_ESTIMATE_ARCHITECTURE.md`
> Oscilloscope architecture: `docs/OSCILLOSCOPE_ARCHITECTURE.md`
> DTC memory architecture: `docs/DTC_MEMORY_AND_CLEAR_ARCHITECTURE.md`

## Phase 0 — Foundation

- [x] Vehicle-scope model
- [x] ECU capability model
- [x] Typed diagnostic evidence model
- [x] Diagnostic event stream foundation
- [x] Safety policy / capability-state vocabulary
- [ ] Android skeleton hardening
- [ ] CI build/test baseline

## Phase 1 — WiCAN connectivity

- [ ] TCP/ELM327 transport hardening
- [ ] SLCAN/raw-CAN transport hardening
- [ ] Automatic protocol detection
- [ ] Transport health metrics
- [ ] RX/TX counters and latency
- [ ] Adaptive polling
- [ ] Wi-Fi/BLE transport where verified

## Phase 2 — CAN foundation

- [x] CAN frame model
- [x] CAN ID/mask/extended filters
- [x] CAN bus statistics
- [ ] Raw CAN monitor UI
- [ ] Capture/export
- [ ] Replay/simulator
- [ ] Bus-load/error/drop visualization

## Phase 3 — ISO-TP / UDS / OBD

- [x] ISO-TP frame classification
- [x] ISO-TP receive reassembly
- [x] UDS positive/negative response parsing
- [x] UDS risk classification
- [x] UDS capability gate foundation
- [x] Registry-driven Mode 01 PID decoder
- [x] OBD stored/pending/permanent DTC decoder foundation
- [x] OBD Mode 04 clear-request model with state-changing classification
- [x] UDS 0x19 ReadDTCInformation request/response foundation
- [x] UDS 0x14 ClearDiagnosticInformation request/response foundation
- [x] Local DTC history lifecycle model
- [ ] ISO-TP transmit / flow-control
- [ ] Generic OBD-II PID/sensor coverage
- [ ] DTC/freeze-frame/readiness pipeline
- [ ] DTC clear executor + post-clear verification
- [ ] DTC diagnostic evidence persistence integration
- [ ] Mode 06 TID/CID/scaling/unit model

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
- [ ] STATIC test
- [ ] LOAD test
- [ ] RECOVERY test
- [ ] TREND test
- [ ] CONFIDENCE scoring
- [ ] Battery health report

## Phase 8 — Repair intelligence / estimates

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
- [x] OBD DTC clear classified as state-changing
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
- [x] Android waveform renderer
- [x] Zoom/pan/freeze interaction state model
- [x] CAN/UDS/DTC event correlation foundation
- [ ] Direct DiagnosticEventStream adapter
- [x] CSV export
- [x] Replay format
- [x] Multi-channel synchronization
- [ ] Verified WiCAN-compatible measurement hardware path
- [ ] Probe/input electrical-limit enforcement
- [ ] Automotive-safe isolation/protection hardware profile

## Long-term compatibility

- [ ] Current Android target SDK maintenance
- [ ] Expanded protocol coverage as verified
- [ ] Community vehicle profiles
- [ ] Reusable WiCAN adapter ecosystem
