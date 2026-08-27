# AutoDiag-WiCAN-Pro Roadmap

## Phase 0 — Foundation

- [x] Repository created
- [x] AI project context
- [ ] README and architecture documentation
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

## Phase 4 — Tesla READ diagnostics

- [ ] Tesla vehicle profiles
- [ ] Verified CAN signal database
- [ ] Model 3/Y identification where possible
- [ ] Battery telemetry
- [ ] Charging telemetry
- [ ] Thermal telemetry
- [ ] Drive-unit telemetry
- [ ] DTC/status information where safely available

## Phase 5 — Automated Tesla health test

- [ ] Data-quality checks
- [ ] Battery health calculations
- [ ] Cell-balance analysis where verified data exists
- [ ] Thermal checks
- [ ] Charging checks
- [ ] Drive-unit checks
- [ ] Diagnostic score with confidence level
- [ ] Exportable report

## Phase 6 — Remote monitoring and automation

- [ ] Background monitoring
- [ ] Local Wi-Fi operation
- [ ] MQTT integration
- [ ] Home Assistant integration
- [ ] Notifications
- [ ] Rule engine
- [ ] Historical telemetry

## Phase 7 — Custom actions

- [ ] Action framework
- [ ] Explicit confirmation UI
- [ ] Per-vehicle capability matrix
- [ ] Safe, verified actions only
- [ ] Experimental write operations isolated from READ core

## Phase 8 — Other vehicles

- [ ] Generic OBD-II expansion
- [ ] VAG profiles
- [ ] UDS layer
- [ ] KWP2000 layer
- [ ] K-Line-capable transport where required
- [ ] Additional manufacturers

## Development principle

A feature is not considered complete merely because the code compiles. For vehicle-specific diagnostics, completion requires reproducible tests and, where applicable, validation against real vehicle data.
