# AutoDiag Implementation Tasks

This is the working task list for the first implementation cycle. Tasks are deliberately ordered so that the application can be tested without a real vehicle before real-vehicle validation.

## P0 — foundation

- [ ] Android Kotlin/Jetpack Compose project with modular core
- [ ] CI: debug build, unit tests, lint
- [ ] transport interfaces independent of Android UI
- [ ] WiCAN TCP client for documented/read-only transport paths
- [ ] mDNS discovery with explicit timeout/error states
- [ ] simulator/mock transport
- [ ] capture/replay data model

## P0 — Capability Discovery

- [ ] vehicle identity model
- [ ] ECU/BMS capability model
- [ ] `available / partial / unavailable / unknown / error` states
- [ ] capability cache keyed by VIN + firmware/software scope where available
- [ ] granular cell/module/temperature capability discovery
- [ ] Tesla market/region indication when reliably decoded
- [ ] no automatic assumptions from model/year alone

## P0 — diagnostic core

- [ ] generic OBD-II framing/parsing
- [ ] CAN frame model and timestamping
- [ ] diagnostic evidence/provenance model
- [ ] verification states
- [ ] session manager and phase boundaries
- [ ] DTC/alert data model

## P1 — Tesla read-only

- [ ] Tesla vehicle profile framework
- [ ] Model 3/Y identification
- [ ] read-only ECU/BMS decoder framework
- [ ] pack voltage/current/SOC where exposed
- [ ] battery temperature and temperature delta where exposed
- [ ] cell/brick/module voltage where exposed
- [ ] cell/module identity preservation
- [ ] charging context: AC/DC
- [ ] drive-unit data where verified
- [ ] Riso/isolation data where exposed

## P1 — EV Battery Health Test

- [ ] Quick / Standard / Full test orchestration
- [ ] REST baseline capture
- [ ] controlled LOAD capture
- [ ] RECOVERY capture
- [ ] AC charging capture
- [ ] DC charging capture
- [ ] contextual samples: SOC, temperature, voltage, current, power, phase
- [ ] no universal hardcoded mV thresholds
- [ ] evidence-backed threshold resolver
- [ ] Battery Fingerprint/history
- [ ] persistent deviation detection
- [ ] confidence calculation

## P1 — replay / expert analysis

- [ ] synchronized timeline
- [ ] replay scrubber using timestamp index/binary search
- [ ] pack → module/brick → cell hierarchy
- [ ] click/inspect any available cell at any timestamp
- [ ] voltage/current/temperature traces
- [ ] event markers
- [ ] driver view with simple language
- [ ] expert numerical view
- [ ] visual battery topology only when verified

## P1 — Diagnostic Knowledge Base

- [ ] DTC/alert knowledge schema
- [ ] OEM/community/engineering source separation
- [ ] verified OEM URL storage
- [ ] Tesla explanation links where official sources are available
- [ ] troubleshooting and service references where legitimately public
- [ ] broken-link / needs-review state
- [ ] finding → explanation → source → related measurements navigation

## P2 — Automation / Sexy Button

- [ ] one-tap `AUTO TEST`
- [ ] profile-driven test selection
- [ ] unsupported stages shown as `NOT_AVAILABLE`, not failed
- [ ] rule engine with JSON/YAML representation
- [ ] replay dry-run for rules
- [ ] audit log for rule execution
- [ ] notification abstraction
- [ ] rate limiting/cooldowns
- [ ] MQTT telemetry
- [ ] Home Assistant integration
- [ ] scheduled read-only remote telemetry
- [ ] stale-data detection

## P2 — additional vehicles

- [ ] Generic OBD-II profile
- [ ] VAG framework
- [ ] VW/Škoda/SEAT/Audi profiles as evidence permits
- [ ] Hyundai/Kia
- [ ] BMW
- [ ] Mercedes
- [ ] Renault/Nissan
- [ ] Mitsubishi

## P3 — experimental control

- [ ] separate WRITE/COMMAND subsystem
- [ ] simulator-only command tests
- [ ] explicit user confirmation
- [ ] separate safety review
- [ ] never enable automatically from an automation rule

## Quality gates

A task is not considered complete merely because code compiles. For vehicle-specific functionality it needs:

1. unit test,
2. simulator/replay test,
3. source/provenance record,
4. verification state,
5. real-vehicle validation where practical,
6. documentation update.
