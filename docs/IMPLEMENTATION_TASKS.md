# AutoDiag Implementation Tasks

Authoritative working backlog. Tasks are ordered by dependency, testability and safety.

## P0 — foundation

- [ ] Android Kotlin/Jetpack Compose project with modular core
- [ ] CI: debug build, unit tests, lint
- [ ] transport interfaces independent of Android UI
- [ ] WiCAN TCP client for documented/read-only transport paths
- [ ] mDNS discovery with explicit timeout/error states
- [ ] simulator/mock transport
- [ ] capture/replay data model
- [ ] timestamp index with binary-search lookup

## P0 — Capability Discovery

- [ ] vehicle identity model
- [ ] ECU/BMS capability model
- [ ] `available / partial / unavailable / unknown / error` states
- [ ] capability cache keyed by VIN + firmware/software scope where available
- [ ] granular cell/module/temperature/Riso capability discovery
- [ ] preserve exactly which metric is available per module
- [ ] Tesla market/region indication when reliably decoded
- [ ] visible ⚠ US-market warning when US configuration is reliably identified
- [ ] `UNKNOWN` when market cannot be established
- [ ] no assumptions from model/year alone

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
- [ ] PRE-PURCHASE TEST profile
- [ ] REST baseline capture
- [ ] controlled LOAD capture
- [ ] RECOVERY capture with explicit elapsed-time samples
- [ ] AC charging capture
- [ ] DC charging capture
- [ ] cell/module tracking continuously during charging
- [ ] contextual samples: SOC, temperature, voltage, current, power, phase
- [ ] no universal hardcoded mV thresholds
- [ ] evidence-backed threshold resolver
- [ ] Battery Fingerprint/history
- [ ] persistent deviation detection
- [ ] confidence calculation
- [ ] distinguish loaded minimum voltage from evidence of a weak cell

## P1 — Riso / HV isolation

- [ ] vehicle-reported numeric isolation values where exposed
- [ ] vehicle-reported status-only mode
- [ ] raw-undecoded mode without invented MΩ values
- [ ] positive-to-chassis / negative-to-chassis / overall values where exposed
- [ ] threshold provenance
- [ ] physical isolation test results stored separately from vehicle-reported diagnostics
- [ ] isolation trend across sessions

## P1 — replay / expert analysis

- [ ] synchronized timeline
- [ ] replay scrubber using timestamp index/binary search
- [ ] pack → module/brick → cell hierarchy
- [ ] click/inspect any available cell at any timestamp
- [ ] show exact historical voltage for the selected cell
- [ ] voltage/current/temperature/imbalance traces
- [ ] event markers for load, recovery, AC and DC charging
- [ ] driver view with simple language and restrained status colors
- [ ] expert numerical view
- [ ] verified battery topology visualization only when supported

## P1 — Diagnostic Knowledge Base

- [ ] DTC/alert knowledge schema
- [ ] OEM/community/engineering source separation
- [ ] verified OEM URL storage
- [ ] Tesla explanation links where official sources are available
- [ ] troubleshooting and service references where legitimately public
- [ ] broken-link / `needs_review` state
- [ ] finding → explanation → source → related measurements navigation
- [ ] show source and verification level next to every repair explanation
- [ ] never invent a repair procedure when no verified source exists

## P2 — Automation / AUTO TEST

- [ ] one-tap `AUTO TEST` / profile-driven “Sexy Button”
- [ ] pre-test data-quality and safety gate
- [ ] automatic phase/session boundaries
- [ ] automatic analysis after each phase
- [ ] unsupported stages shown as `NOT_AVAILABLE`, not failed
- [ ] rule engine with JSON/YAML representation
- [ ] replay dry-run for rules
- [ ] audit log for rule execution
- [ ] separate READ / ANALYZE / NOTIFY policy
- [ ] notification cooldown/rate limiting
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

A task is not complete merely because code compiles. Vehicle-specific functionality needs:

1. unit test,
2. simulator/replay test,
3. source/provenance record,
4. verification state,
5. real-vehicle validation where practical,
6. documentation update.
