# AutoDiag-WiCAN-Pro Roadmap

## Phase 0 — Foundation

- [x] Repository created
- [x] AI project context
- [x] Architecture overview
- [x] Diagnostic knowledge-base architecture
- [x] Tesla market/region identification specification
- [ ] Safety policy
- [ ] Android project skeleton
- [ ] CI build/test pipeline

## Phase 1 — WiCAN connectivity

- [ ] WiCAN mDNS discovery
- [ ] Manual IP fallback
- [ ] TCP ELM327 transport (:3333)
- [ ] TCP SLCAN/raw CAN transport (:23)
- [ ] Connection state and reconnect handling
- [ ] Raw TX/RX logging

## Phase 2 — Simulator and CAN tools

- [ ] WiCAN TCP mock
- [ ] CAN frame simulator
- [ ] Capture replay
- [ ] CAN ID statistics
- [ ] Filtering
- [ ] Export

## Phase 3 — Generic OBD-II

- [ ] Standard OBD-II PIDs
- [ ] DTC reading
- [ ] Live data dashboard
- [ ] ELM327 compatibility layer

## Phase 4 — Vehicle identification and Tesla READ diagnostics

- [ ] Vehicle make/model/year identification
- [ ] Tesla market/region identification
- [ ] Vehicle profile: trim, drive unit, battery variant, supplier and chemistry where verifiable
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

## Phase 5 — Automated EV health test

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
- [ ] Simple human-readable results
- [ ] Expert battery/module/cell visualization
- [ ] Exportable report

## Phase 6 — Diagnostic Knowledge Base

- [ ] DTC/vehicle-alert normalization
- [ ] OEM description mapping
- [ ] OEM troubleshooting links
- [ ] OEM service/repair links
- [ ] Source and verification display
- [ ] Vehicle-generation-specific procedure matching
- [ ] Community references kept separate from OEM procedures
- [ ] `needs_review` handling for changed/removed OEM links

## Phase 7 — Remote monitoring and automation

- [ ] Background monitoring
- [ ] Local Wi-Fi operation
- [ ] Remote telemetry while parked at home
- [ ] MQTT integration
- [ ] Home Assistant integration
- [ ] Notifications
- [ ] Rule engine
- [ ] Historical telemetry
- [ ] User-defined dashboard/widgets
- [ ] Automation triggers based on verified telemetry

## Phase 8 — Custom actions

- [ ] Action framework
- [ ] Explicit confirmation UI
- [ ] Per-vehicle capability matrix
- [ ] Safe, verified actions only
- [ ] Experimental write operations isolated from READ core
- [ ] No unverified CAN control commands

## Phase 9 — Other vehicles

- [ ] Generic OBD-II expansion
- [ ] VAG profiles
- [ ] UDS layer
- [ ] KWP2000 layer
- [ ] K-Line-capable transport where required
- [ ] Hyundai/Kia profiles
- [ ] BMW profiles
- [ ] Mercedes profiles
- [ ] Renault/Dacia profiles
- [ ] Nissan profiles
- [ ] Mitsubishi profiles
- [ ] Additional manufacturers

## Long-term compatibility

- [ ] Current Android target SDK maintenance
- [ ] Automated compatibility/build testing
- [ ] Dependency update policy
- [ ] Migration tests for future Android releases

## Development principle

A feature is not considered complete merely because the code compiles. For vehicle-specific diagnostics, completion requires reproducible tests and, where applicable, validation against real vehicle data.
