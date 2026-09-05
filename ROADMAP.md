# SEOBD Roadmap

> Master specification: `docs/TODAY_MASTER_PLAN_2026-09-02.md`
> Repair/estimate architecture: `docs/REPAIR_KNOWLEDGE_ESTIMATE_ARCHITECTURE.md`
> Oscilloscope architecture: `docs/OSCILLOSCOPE_ARCHITECTURE.md`
> DTC memory architecture: `docs/DTC_MEMORY_AND_CLEAR_ARCHITECTURE.md`
> UI tokens: `docs/UI_TESLA_THEME.md`

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
- [x] SLCAN/raw-CAN transport hardening
- [ ] Automatic protocol detection
- [x] Transport health metrics
- [x] RX/TX counters and latency foundation
- [x] Adaptive polling foundation
- [ ] Wi-Fi/BLE transport where verified

## Phase 2 — CAN foundation
- [x] CAN frame model
- [x] CAN ID/mask/extended filters
- [x] CAN bus statistics
- [x] Raw CAN monitor UI
- [x] Capture/export
- [x] Replay/simulator
- [ ] Bus-load/error/drop visualization
- [x] SLCAN frame codec with TCP chunk reassembly

## Phase 3 — ISO-TP / UDS / OBD
- [x] ISO-TP frame classification
- [x] ISO-TP receive reassembly
- [x] UDS positive/negative response parsing
- [x] UDS risk classification
- [x] UDS capability gate foundation
- [x] Registry-driven Mode 01 PID decoder
- [x] OBD stored/pending/permanent DTC decoder foundation
- [x] OBD clear-request model
- [x] UDS 0x19/0x14 foundations
- [x] Local DTC history lifecycle model
- [x] ELM ISO-TP AT command builders
- [ ] ISO-TP transmit / flow-control executor
- [ ] Generic OBD-II PID/sensor coverage
- [ ] DTC/freeze-frame/readiness UI
- [ ] DTC clear executor + post-clear verification
- [ ] Evidence persistence integration
- [ ] Mode 06 TID/CID/scaling/unit model

## Phase 4 — ECU discovery and vehicle identification
- [ ] Functional ECU scan
- [ ] Physical ECU discovery
- [ ] ECU identification
- [ ] Software/hardware/calibration identification
- [ ] Exact vehicle/ECU capability cache
- [ ] Make/model/year identification
- [ ] Market identification with explicit source
- [ ] Vehicle profile resolver

## Phase 5 — Live Data / Dashboard / HUD
- [x] Evidence-aware live-data model
- [x] 1–16 selectable live-values UI shell
- [x] Rolling-history buffer model
- [x] Time-series/sparkline rendering shell
- [x] Communication-quality presentation shell
- [x] Profile-driven dashboard model
- [x] Mirrored HUD profile model
- [ ] Connect UI directly to live transport samples
- [ ] Verified generic OBD live-data coverage
- [ ] Landscape production layout
- [ ] Material3 production theme pass

## Phase 6 — Vehicle-specific READ diagnostics
- [ ] Exact vehicle/profile matching
- [ ] Verified signal database
- [ ] Model-specific identification where possible
- [ ] Battery telemetry
- [ ] Per-module/per-cell telemetry where exposed
- [ ] Charging telemetry and AC/DC analysis
- [ ] Thermal telemetry
- [ ] Drive-unit telemetry
- [ ] HV isolation/Riso where exposed
- [ ] DTC/status information
- [ ] Public service-source metadata

## Phase 7 — Automated EV health test
- [x] Evidence-first battery-health assessment foundation
- [ ] Data-quality checks
- [ ] STATIC test
- [ ] LOAD test
- [ ] RECOVERY test
- [ ] TREND test
- [ ] CONFIDENCE scoring
- [ ] Battery health report

## Phase 8 — Repair intelligence / estimates
- [ ] `needs_review` handling
- [ ] RepairSource / Procedure / Part models
- [ ] LaborEstimate / PriceEstimate
- [ ] RepairEstimateEngine
- [ ] Exact vehicle/ECU matching
- [ ] DTC → causes → checks → references
- [ ] Parts/labor provenance
- [ ] DIY / service UI modes

## Phase 9 — Pre-purchase automation/reporting
- [ ] CONNECT → IDENTIFY → DISCOVER → DTC → FREEZE FRAME → READINESS → LIVE DATA → MONITORS → EV TESTS → ANALYZE → REPAIR ESTIMATE → REPORT
- [ ] Evidence aggregation
- [ ] Report model
- [ ] Supported-only PASS/FAIL
- [ ] Repair-cost summary
- [ ] Vehicle scope and confidence display

## Phase 10 — Remote monitoring and automation
- [ ] Background monitoring
- [ ] Local/remote telemetry
- [ ] MQTT
- [ ] Home Assistant
- [ ] Notification rate limits/cooldowns
- [ ] JSON/YAML rule engine
- [ ] Rule replay/dry-run
- [ ] Audit log
- [ ] Historical telemetry
- [ ] User dashboard/widgets
- [ ] Verified-telemetry triggers
- [ ] One-tap profile-driven AUTO TEST
- [ ] Charge-cost / vampire-drain / geofencing

## Phase 11 — Other vehicles
- [ ] Generic OBD-II profile
- [ ] VAG profiles
- [ ] Hyundai/Kia
- [ ] BMW
- [ ] Mercedes
- [ ] Renault/Dacia
- [ ] Nissan
- [ ] Mitsubishi
- [ ] Toyota/Ford/GM/Stellantis/Volvo/Polestar

## Phase 12 — Isolated WRITE / service framework
- [x] UDS service risk classification
- [x] Capability gate foundation
- [x] DTC clear classified as state-changing
- [x] Experimental command dry-run scaffold
- [ ] UDS execution framework
- [ ] Session control / Security Access
- [ ] Read/write identifiers in isolated layer
- [ ] RoutineControl in isolated layer
- [ ] Explicit confirmation UI
- [ ] Per-vehicle capability matrix
- [ ] Safety review
- [ ] Simulator-first validation
- [ ] No unverified CAN control commands

## Phase 13 — Integrated automotive oscilloscope
- [x] Capability and capture model
- [x] Measurements/triggers/ring buffer
- [x] Frequency/period/duty/RMS foundations
- [x] Viewer interaction model
- [x] CAN/UDS/DTC correlation foundation
- [x] CSV/replay/multi-channel synchronization
- [ ] Direct DiagnosticEventStream adapter
- [ ] Verified measurement hardware path
- [ ] Electrical-limit enforcement
- [ ] Automotive-safe isolation profile

## Quality gate
A feature is complete only when code, unit tests, simulator/replay coverage, provenance, verification state and documentation agree. Unsupported data remains explicitly unavailable/unknown; it is never converted into a guessed value or diagnosis.
