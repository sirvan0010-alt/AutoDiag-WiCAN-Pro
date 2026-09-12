# AI Tesla Repository Extraction Plan

## Purpose

This is the persistent extraction plan for `AutoDiag-WiCAN-Pro` and `AutoDiag-WiCAN-Diagnostic-Data`.

The objective is **evidence-driven reuse across the whole AutoDiag platform**. AutoDiag is not limited to passive diagnostics. It is a universal vehicle interface platform combining diagnostics, CAN/OBD/ISO-TP/UDS, live vehicle data, user application/dashboard functions, Wi-Fi/BLE/USB connectivity, vehicle history/evidence and user-authorized vehicle control where technically and safely supported.

External repositories are sources of architecture, protocol knowledge, vehicle data, UI ideas, hardware/transport knowledge and control concepts. They are not runtime dependencies unless explicitly approved.

Core architecture:

`capture -> decode -> hunt -> correlate -> investigate`

Protocol path:

`transport -> CAN/SLCAN/OBD -> ISO-TP -> UDS/manufacturer protocol -> decoder -> observation/state`

Application consumers:

`observation/state -> diagnostics`

`observation/state -> live UI/dashboard`

`observation/state -> history/evidence`

`user intent -> capability/policy check -> vehicle command -> transport -> response/result -> audit/history`

The control path is deliberately separate from decoding so AutoDiag can support both read/observe and authorized control without turning a decoded signal into an unintended command.

---

# Classification model

Every useful external item is classified independently from repository priority:

- **KEEP** — directly reusable architecture/data for universal AutoDiag.
- **ADAPT** — useful implementation/concept that must be redesigned for Kotlin/Android and AutoDiag boundaries.
- **VEHICLE_SPECIFIC** — useful for a manufacturer/model/generation adapter or feature module.
- **CONTROL** — active vehicle command/control capability; potentially part of AutoDiag, but only through the dedicated command/policy/result path.
- **REFERENCE** — useful evidence or architectural inspiration, but not yet implementation-ready.
- **REJECTED** — not useful or not appropriate for the project.

`CONTROL` is **not** the same as `REJECTED`.

---

# Repository matrix — complete supplied Tesla set

## P0 — direct platform value; extract in parallel with application work

### 1. `outlandnish/tm3diag`
**Role:** CAN/UDS/diagnostic architecture.

**Extract:** CAN transport abstraction, frame capture/logging, ISO-TP/UDS request-response patterns, DID/routine abstractions, ECU discovery, negative responses, timeout/retry/session handling, CAN decoding, DBC generation, gateway logs, virtual CAN/bench tests and Model Y extraction paths.

**Classification:** KEEP / ADAPT / VEHICLE_SPECIFIC.

**Safety:** firmware flashing and security-access material is not blindly imported.

**Status:** COMPLETE-INITIAL. Durable artifact: `tesla/sources/tm3diag_architecture_extraction.md`.

---

### 2. `talas9/tesla_can_signals`
**Role:** Model Y signal-data source.

**Extract:** Model Y compact database, CAN IDs, bit layouts, scaling, units, enumerations, bus/ECU associations, self-test parser data and Model Y/Model 3 differences.

**Classification:** VEHICLE_SPECIFIC / REFERENCE.

**Evidence:** all static signals start as `EXTERNAL_REFERENCE`.

**Status:** PARTIAL/ONGOING. Priority candidates already extracted include BMS SOC, SOH, pack current, isolation and HV state candidates.

---

### 3. `OBDb/Tesla-Model-Y`
**Role:** Model Y generation/schema/validation source.

**Extract:** model-year/generation classification, signalset schema, naming, validation rules and machine-checkable data contracts.

**Classification:** KEEP / ADAPT / VEHICLE_SPECIFIC / REFERENCE.

**Important:** current `v3/default.json` has no populated command set and did not independently corroborate the current talas9 BMS signal candidates. Do not invent corroboration.

**Status:** COMPLETE-INITIAL. Durable artifact: `tesla/sources/OBDb_Tesla_Model_Y_extraction.md`.

---

### 4. `bassmaster187/TeslaLogger`
**Role:** telemetry, history, event and cloud/local data architecture.

**Extract:** timestamped telemetry normalization, VIN-scoped ingestion, last-value/time-series state, invalid-value rejection, charging sessions, event/state history, ScanMyTesla integration, persistence, MQTT and API/data-model separation.

**Classification:** KEEP / ADAPT / VEHICLE_SPECIFIC / REFERENCE.

**Status:** COMPLETE-INITIAL. Durable artifact: `tesla/sources/TeslaLogger_extraction.md`.

---

### 5. `ekr/candash`
**Role:** Android live vehicle application/dashboard.

**Extract:** Android project structure, CAN-to-UI flow, live update loop, state model, rendering strategy, network/CANserver boundary, live gauges, sensor-derived UI features and lifecycle/threading patterns.

**Classification:** KEEP / ADAPT / VEHICLE_SPECIFIC.

**Status:** NEXT ACTIVE EXTRACTION.

---

### 6. `tomas7470/tesladash`
**Role:** SocketCAN/ELM327/DBC/live dashboard architecture.

**Extract:** ELM327-to-SocketCAN concepts, SocketCAN lifecycle, DBC loading, real-time UI updates, startup/service handling and hardware abstraction.

**Classification:** KEEP / ADAPT / VEHICLE_SPECIFIC.

**Status:** NEXT ACTIVE EXTRACTION.

---

### 7. `tfoldi/fleetwise-iot-tesla3`
**Role:** CAN telemetry, DBC, decoder manifest and cloud schema.

**Extract:** `model3can.dbc`, reduced DBC, decoder manifest, signal catalog, campaign configuration, signal-to-cloud mapping, timestamps and dashboard schema.

**Classification:** KEEP / ADAPT / VEHICLE_SPECIFIC / REFERENCE.

**Critical conflict already found:** CAN 306 pack-current encoding differs from the talas9 Model Y candidate. Preserve both; this is not corroboration.

**Status:** COMPLETE-INITIAL. Durable artifact: `tesla/sources/FleetWise_Tesla3_extraction.md`.

---

### 8. `clowrey/S3XY-BMS`
**Role:** BMS/CAN/isoSPI/serial/test architecture.

**Extract:** CAN message format, parameter API, serial command/response architecture, passive isoSPI snooping, cell telemetry model, test harness, session architecture and acquisition/decode/presentation/control separation.

**Classification:** KEEP / ADAPT / VEHICLE_SPECIFIC / CONTROL.

**Control restriction:** battery balancing/contactor/actuation code is not automatically imported. Read-only observation is the first AutoDiag target.

**Status:** NEXT ACTIVE EXTRACTION.

---

### 9. `evoffer/instrument-cluster-firmware`
**Role:** firmware/hardware mapping and CAN-capable aftermarket cluster evidence.

**Extract:** firmware package metadata, hardware/version mappings, readable configuration, CAN-related documentation and identifiers.

**Classification:** VEHICLE_SPECIFIC / REFERENCE / ADAPT.

**Binary policy:** catalog binaries first; reverse engineer only to answer a concrete AutoDiag question.

**Status:** P0/P1 extraction pending.

---

### 10. `timdorr/tesla-api`
**Role:** Tesla cloud/Owner API vocabulary and vehicle-state/command model.

**Extract:** endpoint catalog, state objects, command vocabulary, authentication/session architecture as historical context and cloud-vs-local distinctions.

**Classification:** KEEP / ADAPT / CONTROL / REFERENCE.

**Important:** cloud API data is a separate evidence domain from CAN/UDS and must not silently become CAN truth.

**Status:** P0/P1 extraction pending.

---

### 11. `barnybug/tesla-cli`
**Role:** practical Tesla cloud vehicle application/control model.

**Extract:** vehicle selection, command abstraction, state/charge vocabulary, sleep/wake semantics and cloud interaction patterns.

**Classification:** KEEP / ADAPT / CONTROL / REFERENCE.

**Status:** P1 but promoted in importance because AutoDiag includes user application/control.

---

### 12. `teslahunt/tesla-vin`
**Role:** vehicle identity and decoder/profile selection.

**Extract:** VIN parsing, model/year/body/motor/battery/plant classification and identity schema.

**Classification:** KEEP / ADAPT / VEHICLE_SPECIFIC.

**Use:** VIN identity may select candidate vehicle profiles, but never proves a CAN signal.

**Status:** P1, should be moved earlier because universal vehicle identification is foundational.

---

## P1 — strong value for user application, control, feature modules, security and vehicle integration

### 13. `cham/TeslaYay`
**Role:** TeslaAPI-backed user application/service integration.

**Extract:** service boundaries, API object handling, state presentation and application integration patterns.

**Classification:** ADAPT / CONTROL / REFERENCE.

---

### 14. `AnalyticETH/tesla-security-research`
**Role:** infotainment architecture, trust boundaries, telemetry provenance and defensive security.

**Extract:** component architecture, service boundaries, data provenance and defensive validation lessons.

**Classification:** KEEP / ADAPT / REFERENCE.

**Do not operationalize:** exploit chains, persistence, token replay or authentication bypasses.

---

### 15. `evoffer/electric-liftgate-firmware`
**Role:** feature-specific actuator hardware/firmware/BLE reference.

**Extract:** hardware generations, firmware mapping, BLE-capable variants, configuration/firmware separation and feature identification.

**Classification:** VEHICLE_SPECIFIC / CONTROL / REFERENCE.

---

### 16. `evoffer/auto-present-door-handles-firmware`
**Role:** feature-specific actuator, CAN/BLE and firmware reference.

**Extract:** hardware/firmware variants, communication architecture and feature-state identification.

**Classification:** VEHICLE_SPECIFIC / CONTROL / REFERENCE.

---

### 17. `pickeditmate/YardstickTeslaChargePortOpener`
**Role:** narrow charge-port hardware/control reference.

**Extract:** only communication, device-identification and command architecture relevant to a future charge-port feature.

**Classification:** VEHICLE_SPECIFIC / CONTROL / REFERENCE.

---

### 18. `dimitrypo/openpilot`
**Role:** large vehicle-interface/safety/CAN architecture reference with Tesla-specific work.

**Extract:** Tesla vehicle interface, CAN handling, state models, signal packing/unpacking, safety boundaries and integration architecture.

**Classification:** KEEP / ADAPT / VEHICLE_SPECIFIC / REFERENCE.

**Do not import autonomous driving behavior or safety-critical actuation blindly.**

---

### 19. `nelsonic/tesla-mobile-office`
**Role:** mobile/Tesla user workflow.

**Extract:** only if AutoDiag develops vehicle/mobile workspace or Android workflow features.

**Classification:** ADAPT / REFERENCE.

---

### 20. `polymorphic/tesla-model-y-checklist`
**Role:** vehicle feature inventory and real-world test-case source.

**Extract:** vehicle functions that can become AutoDiag inspection/verification scenarios: charging, HVAC, cameras, blind spot, liftgate, etc.

**Classification:** KEEP / VEHICLE_SPECIFIC / REFERENCE.

This is more useful than previously classified because AutoDiag needs user-facing feature tests, not just CAN decoding.

---

## P2 — retain/watch; extract when a concrete feature needs it

### 21. `0xfokki/tesla-ym50k`
Tiny JS project. Watch for future protocol/CAN/BLE additions.

**Classification:** REFERENCE.

### 22. `0xfokki/tesla-yfjoy`
Currently no useful source tree identified.

**Classification:** WATCH.

### 23. `Corbin/Tesla-Theater-YT-BUG`
Historical web/UI behavior.

**Classification:** REFERENCE.

### 24. `BinaryVortex/Tesla-Model-Y-Mock-Page`
UI mock/reference.

**Classification:** REFERENCE.

### 25. `midudev/landing-tesla`
Frontend/visual design reference only.

**Classification:** REFERENCE.

### 26. `rocketseat-content/youtube-clone-tesla-homepage`
Frontend training/clone project.

**Classification:** REFERENCE.

### 27. `gucluceyhan/tesla-sr-bot`
Historical Model Y inventory/order bot.

**Classification:** REFERENCE only for inventory filtering/UI/logging if such a feature is ever requested.

**Do not import:** payment-card handling, bot-detection bypass or automated purchasing.

---

## Invalid/unresolved supplied URLs

### 28. `matthewhefferon/tesla-clone-y`
Supplied repository URL returned 404. Do not invent a replacement.

### 29. `AmirhosseinDotZip/tesla-clone-y`
Supplied URL was malformed (`tesla-clone-ythttps`). Exact repository could not be verified. Re-check only if a corrected URL is supplied.

---

# Re-prioritized extraction order

## Track A — universal transport/diagnostics
1. `tm3diag`
2. `tesla-vin`
3. `tesla_can_signals`
4. `OBDb/Tesla-Model-Y`
5. `fleetwise-iot-tesla3`

## Track B — Android/user application/live data
6. `candash`
7. `tesladash`
8. `TeslaLogger`
9. `Tesla Model Y checklist`

## Track C — user control/cloud/feature integration
10. `tesla-api`
11. `tesla-cli`
12. `TeslaYay`
13. `electric-liftgate-firmware`
14. `auto-present-door-handles-firmware`
15. `YardstickTeslaChargePortOpener`

## Track D — BMS/ECU/firmware evidence
16. `S3XY-BMS`
17. `instrument-cluster-firmware`
18. `openpilot`

## Track E — security/provenance/defensive architecture
19. `tesla-security-research`

Everything else remains available as reference and is extracted when a concrete AutoDiag feature benefits from it.

---

# Evidence rules

1. A public GitHub signal is never automatically `VEHICLE_VERIFIED`.
2. Model 3 data is not automatically Model Y data.
3. VIN/model-year identity selects candidates but does not prove signal correctness.
4. Prefer actual vehicle captures over static documentation.
5. Prefer technically independent second-source agreement for `CROSS_CORRELATED`.
6. Preserve conflicting signal definitions side-by-side.
7. Cloud/API state, CAN state and diagnostic/UDS state are separate evidence domains until correlated.
8. Firmware artifacts are evidence sources, not automatically executable inputs.
9. Control features are allowed as AutoDiag capabilities, but require explicit command/policy/result architecture and stronger safeguards for safety-critical functions.
10. Autonomous control, exploit chains, credential bypass, payment automation and other unrelated/high-risk behavior are not copied merely because an external project contains them.
11. External code remains reference architecture unless deliberately adapted into AutoDiag.
12. Every extracted item records provenance, confidence and verification state.

---

# Required extraction record

For every mined repository record:

- repository URL;
- owner/name;
- default branch;
- inspected commit SHA;
- inspection date;
- license/status where available;
- repository role;
- priority;
- useful files/directories;
- architecture/data/protocol/UI/control concepts extracted;
- classification (`KEEP`, `ADAPT`, `VEHICLE_SPECIFIC`, `CONTROL`, `REFERENCE`, `REJECTED`);
- target AutoDiag module;
- confidence;
- verification state;
- known conflicts;
- safety restrictions;
- follow-up tasks.

Target modules include:

- `core/can`
- `core/slcan`
- `core/isotp`
- `core/uds`
- `transport/wifi`
- `transport/bluetooth`
- `transport/usb`
- `vehicle-profiles`
- `diagnostics`
- `decoder`
- `evidence`
- `DtcHistoryStore`
- `VerificationState`
- `live-dashboard`
- `vehicle-control`
- `command-audit`

---

# Current durable extraction state

Completed initial durable artifacts:

- `tm3diag_architecture_extraction.md`
- `OBDb_Tesla_Model_Y_extraction.md`
- `FleetWise_Tesla3_extraction.md`
- `TeslaLogger_extraction.md`
- Model Y candidate signal files from `talas9/tesla_can_signals`

Known unresolved issue:

- Tesla Model Y CAN 306 pack-current candidate and FleetWise Model 3 CAN 306 pack-current definition conflict in bit layout/scaling. Keep both with vehicle scope; do not merge them.

Next immediate work:

1. Inspect concrete source in `ekr/candash`.
2. Inspect concrete source in `tomas7470/tesladash`.
3. Create durable Android/live-dashboard extraction artifact.
4. Update this manifest with current source revisions and results.
5. Continue to `S3XY-BMS`.
6. In parallel, begin the cloud/control and vehicle-identity tracks without allowing them to contaminate local CAN evidence.

The goal is not to collect repositories for their own sake. The goal is to extract everything useful for building a **universal AutoDiag vehicle platform** while preserving provenance, vehicle scope, evidence quality and safe control boundaries.
