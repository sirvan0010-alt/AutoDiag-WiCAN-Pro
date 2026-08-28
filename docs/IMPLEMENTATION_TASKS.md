# AutoDiag Implementation Tasks

Authoritative working backlog. Tasks are ordered by dependency, testability and safety.

## P0 — foundation

- [ ] Android Kotlin/Jetpack Compose project with modular core
- [ ] CI: debug build, unit tests, lint
- [ ] transport interfaces independent from Android UI
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
- [ ] WiCAN hardware/firmware capability matrix
- [ ] CAN/CAN-FD capability detection
- [ ] HS-CAN/MS-CAN/SW-CAN/K-Line/J1850 capability detection where exposed
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
- [ ] freeze-frame data model
- [ ] I/M readiness model
- [ ] ISO-TP transport
- [ ] read-only UDS service framework

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
- [ ] vehicle-reported SOH where exposed

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
- [ ] one-hour/short-loan mode with explicit limited-confidence result

## P1 — PRE-PURCHASE forensic audit

- [ ] ECU/Gateway VIN identity comparison where both values are exposed
- [ ] ECU replacement/identity mismatch finding with non-definitive wording
- [ ] odometer cross-check from documented/verified sources
- [ ] crash/airbag evidence inventory from available DTCs/status/history
- [ ] BMS/HV pyrotechnic-disconnect evidence where exposed
- [ ] explicitly distinguish HV pyrofuse/PBDU from airbag squib circuits
- [ ] do not treat generic squib resistance as pyrofuse resistance
- [ ] suspicious equal-resistance pattern as `SUSPICIOUS_CONFIGURATION`, never automatic proof of emulator
- [ ] evidence manifest for every forensic finding
- [ ] `NO_RELEVANT_EVIDENCE_FOUND / EVIDENCE_FOUND / INSUFFICIENT_COVERAGE / UNVERIFIED` states
- [ ] Czech explanations and source/verification state for every forensic result

## P1 — Riso / HV isolation

- [ ] vehicle-reported numeric isolation values where exposed
- [ ] vehicle-reported status-only mode
- [ ] raw-undecoded mode without invented MΩ values
- [ ] positive-to-chassis / negative-to-chassis / overall values where exposed
- [ ] threshold provenance
- [ ] physical isolation test results stored separately from vehicle-reported diagnostics
- [ ] isolation trend across sessions
- [ ] apply DC/AC isolation criteria according to verified vehicle architecture; do not hard-code 200 kΩ as a universal ISO minimum

## P1 — replay / expert analysis

- [ ] synchronized timeline
- [ ] replay scrubber using timestamp index/binary search
- [ ] pack → module/brick → cell hierarchy
- [ ] click/inspect any available cell at any timestamp
- [ ] show exact historical voltage for the selected cell
- [ ] voltage/current/temperature/imbalance traces
- [ ] event markers for load, recovery, AC and DC charging
- [ ] charge-curve replay
- [ ] driver view with simple language and restrained status colors
- [ ] expert numerical view
- [ ] verified battery topology visualization only when supported

## P1 — Diagnostic Knowledge Base / Czech UI

- [ ] central `InfoTooltip` component for diagnostic metrics
- [ ] `?` tooltip on every non-obvious metric, status and evaluated result
- [ ] Czech tooltip content with what/why/source/interpretation/verification
- [ ] Czech user-facing error messages with stable internal error codes
- [ ] tooltip text sourced centrally, not duplicated per screen
- [ ] tooltip content scope-aware for vehicle/firmware/evidence profile
- [ ] show `NOT_AVAILABLE`, `NOT_TESTED`, `UNASSESSED`, `ERROR` with explanatory tooltips
- [ ] verified OEM/community/engineering source separation
- [ ] verified OEM URL storage
- [ ] Tesla explanation links where official sources are available
- [ ] troubleshooting and service references where legitimately public
- [ ] broken-link / `needs_review` state
- [ ] finding → explanation → source → related measurements navigation
- [ ] show source and verification level next to every repair explanation
- [ ] never invent a repair procedure when no verified source exists

## P1 — ICE / Hybrid diagnostics

- [ ] generic OBD-II DTC + freeze-frame
- [ ] I/M readiness
- [ ] odometer cross-check across available ECUs
- [ ] DPF/OPF soot/ash and differential pressure where exposed
- [ ] regeneration history where exposed
- [ ] SCR/AdBlue/NOx diagnostics where exposed
- [ ] per-cylinder misfire counters where exposed
- [ ] injector correction / smooth-running data where exposed
- [ ] requested vs actual boost pressure
- [ ] requested vs actual common-rail pressure
- [ ] thermal context and live-data replay

## P1 — Bus health / auto-electrician tools

- [ ] bus load
- [ ] frame rate
- [ ] error-frame observations
- [ ] bus-off / error-passive state when accessible
- [ ] dropped/overrun frame counters
- [ ] bitrate/configuration evidence
- [ ] raw CAN capture
- [ ] K-Line capture where supported
- [ ] separate physical termination/resistance test record

## P2 — Automation / AUTO TEST

- [ ] one-tap `AUTO TEST` / profile-driven “Sexy Button”
- [ ] one-tap `PRE-PURCHASE TEST` profile including forensic stages
- [ ] pre-test data-quality and safety gate
- [ ] automatic phase/session boundaries
- [ ] automatic analysis after each phase
- [ ] unsupported stages shown as `NOT_AVAILABLE`, not failed
- [ ] evidence coverage summary
- [ ] deterministic forensic finding IDs
- [ ] report with raw evidence references and SHA-256 evidence manifest
- [ ] rule engine with JSON/YAML representation
- [ ] replay dry-run for rules
- [ ] audit log for rule execution
- [ ] separate READ / ANALYZE / NOTIFY policy
- [ ] notification cooldown/rate limiting
- [ ] MQTT telemetry
- [ ] Home Assistant integration
- [ ] scheduled read-only remote telemetry
- [ ] stale-data detection

## P3 — experimental control

- [ ] separate WRITE/COMMAND subsystem
- [ ] J2534/PassThru adapter architecture
- [ ] simulator-only command tests
- [ ] explicit user confirmation
- [ ] separate safety review
- [ ] never enable automatically from an automation rule
- [ ] Security Gateway support only through verified/legal interfaces
- [ ] actuator tests / RoutineControl only after explicit safety review

## Quality gates

A task is not complete merely because code compiles. Vehicle-specific functionality needs:

1. unit test,
2. simulator/replay test,
3. source/provenance record,
4. verification state,
5. real-vehicle validation where practical,
6. documentation update.

For safety-relevant data, the source and scope must be visible in the UI. Unsupported data is never substituted with a guess.
