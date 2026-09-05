# SEOBD Implementation Tasks

Authoritative working backlog. Tasks are ordered by dependency, testability and safety.

## P0 — foundation
- [x] Android Kotlin/Jetpack Compose project with modular core
- [ ] CI: debug build, unit tests, lint
- [x] transport interfaces independent from Android UI
- [x] WiCAN TCP client for documented/read-only transport paths
- [x] mDNS discovery with explicit timeout/error states
- [x] simulator/mock transport
- [x] capture/replay data model
- [ ] timestamp index with binary-search lookup

## P0 — Capability Discovery
- [x] vehicle identity model
- [x] ECU capability model
- [x] available / partial / unavailable / unknown / error states
- [ ] capability cache keyed by VIN + firmware/software scope
- [ ] granular cell/module/temperature/isolation capability discovery
- [x] WiCAN hardware/firmware capability matrix foundation
- [ ] CAN/CAN-FD capability detection
- [ ] HS-CAN/MS-CAN/SW-CAN/K-Line/J1850 detection where exposed
- [ ] market/region indication when reliably decoded
- [ ] no assumptions from model/year alone

## P0 — diagnostic core
- [x] generic OBD-II framing/parsing foundation
- [x] CAN frame model and timestamping
- [x] diagnostic evidence/provenance model
- [x] verification states
- [x] session manager and phase boundaries
- [x] normalized DTC data model
- [ ] freeze-frame data model integration
- [ ] I/M readiness model
- [x] ISO-TP receive transport
- [x] read-only UDS service foundation

## P1 — vehicle read-only diagnostics
- [ ] exact vehicle profile framework
- [ ] read-only ECU/BMS decoder framework
- [ ] pack voltage/current/SOC where exposed
- [ ] battery temperature/cell/module data where exposed
- [ ] charging context AC/DC
- [ ] drive-unit data where verified
- [ ] isolation data where exposed
- [ ] vehicle-reported SOH where exposed

## P1 — EV Battery Health Test
- [x] evidence-first battery health analyzer foundation
- [ ] Quick / Standard / Full orchestration
- [ ] PRE-PURCHASE TEST profile
- [ ] REST baseline capture
- [ ] controlled LOAD capture
- [ ] RECOVERY capture with elapsed-time samples
- [ ] AC/DC charging capture
- [ ] continuous cell/module tracking during charging
- [ ] contextual SOC/temperature/voltage/current/power/phase samples
- [x] no universal hardcoded mV thresholds
- [ ] evidence-backed threshold resolver
- [ ] Battery Fingerprint/history
- [ ] persistent deviation detection
- [x] confidence calculation foundation
- [x] distinguish loaded minimum voltage from proof of weak cell
- [ ] limited-confidence short-loan mode

## P1 — replay / expert analysis
- [ ] synchronized timeline
- [ ] binary-search replay scrubber
- [ ] pack → module/brick → cell hierarchy
- [ ] historical cell inspection
- [ ] voltage/current/temperature/imbalance traces
- [ ] load/recovery/AC/DC event markers
- [ ] charge-curve replay
- [ ] driver view
- [ ] expert numerical view
- [ ] verified battery topology visualization

## P1 — ICE / Hybrid diagnostics
- [ ] generic OBD-II DTC + freeze-frame UI
- [ ] I/M readiness
- [ ] odometer cross-check
- [ ] DPF/OPF diagnostics where exposed
- [ ] SCR/AdBlue/NOx diagnostics where exposed
- [ ] per-cylinder misfire counters where exposed
- [ ] injector correction where exposed
- [ ] requested vs actual boost
- [ ] requested vs actual rail pressure
- [ ] thermal context and replay

## P1 — Diagnostic Knowledge Base
- [ ] DTC/alert knowledge schema
- [ ] OEM/community/engineering source separation
- [ ] verified OEM URL storage
- [ ] explanation/source/measurement navigation
- [ ] broken-link / `needs_review` state
- [ ] source and verification level beside repair explanations
- [ ] never invent repair procedures

## P1 — Bus health / auto-electrician tools
- [x] raw CAN capture
- [x] frame statistics foundation
- [ ] bus load
- [ ] error-frame observations
- [ ] bus-off/error-passive where accessible
- [ ] dropped/overrun counters
- [ ] bitrate/configuration evidence
- [ ] K-Line capture where supported
- [ ] separate physical termination test record

## P2 — Automation / AUTO TEST
- [ ] one-tap profile-driven AUTO TEST
- [ ] pre-test data-quality and safety gate
- [ ] automatic phase/session boundaries
- [ ] automatic analysis after each phase
- [ ] unsupported stages shown as NOT_AVAILABLE
- [ ] JSON/YAML rule engine
- [ ] replay dry-run
- [ ] audit log
- [ ] READ / ANALYZE / NOTIFY policy separation
- [ ] notification cooldown/rate limiting
- [ ] MQTT
- [ ] Home Assistant
- [ ] scheduled read-only telemetry
- [ ] stale-data detection

## P2 — additional vehicles
- [ ] Generic OBD-II profile
- [ ] VAG
- [ ] Hyundai/Kia
- [ ] BMW
- [ ] Mercedes
- [ ] Renault/Dacia
- [ ] Nissan
- [ ] Mitsubishi

## P3 — experimental control
- [ ] separate WRITE/COMMAND subsystem
- [ ] J2534/PassThru architecture
- [x] simulator-only command testing boundary
- [ ] explicit user confirmation
- [ ] separate safety review
- [ ] never enable automatically from automation
- [ ] Security Gateway only through verified/legal interfaces
- [ ] actuator/RoutineControl only after explicit safety review

## Quality gates
A task is not complete merely because code compiles. Vehicle-specific functionality needs unit test, simulator/replay test, source/provenance record, verification state, real-vehicle validation where practical, and documentation. Safety-relevant UI must expose source and scope; unsupported data is never substituted with a guess.
