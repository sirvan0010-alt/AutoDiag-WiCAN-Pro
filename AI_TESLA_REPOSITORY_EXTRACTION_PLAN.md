# AI Tesla Repository Extraction Plan

## Purpose

This document is the persistent instruction set for AI agents working on `AutoDiag-WiCAN-Pro` and its diagnostic-data work. It records the external Tesla repositories that were reviewed, classifies their usefulness, and defines what should be extracted, what should only be referenced, and what should be ignored.

The goal is **evidence-driven reuse**, not blind copying. External projects are references and evidence sources. Do not turn an external repository into a runtime dependency unless explicitly approved and technically justified.

Primary project architecture to preserve:

`capture -> decode -> hunt -> correlate -> investigate`

with a strict separation between:

- raw CAN/SLCAN transport,
- ISO-TP transport/session handling,
- UDS request/response logic,
- signal decoding and evidence,
- DTC history and verification state,
- vehicle-specific diagnostic data.

---

## Priority model

### P0 — extract first / parallel with current APK and diagnostic work

These repositories contain direct CAN, UDS, signal, diagnostic, telemetry, or reusable data-model evidence.

1. `outlandnish/tm3diag`
2. `talas9/tesla_can_signals`
3. `OBDb/Tesla-Model-Y`
4. `bassmaster187/TeslaLogger`
5. `ekr/candash`
6. `tomas7470/tesladash`
7. `tfoldi/fleetwise-iot-tesla3`
8. `clowrey/S3XY-BMS`
9. `evoffer/instrument-cluster-firmware`
10. `timdorr/tesla-api`

### P1 — retain and inspect after P0

These contain useful protocol/API, vehicle identification, or security-research evidence, but are less directly aligned with the core CAN diagnostic pipeline.

11. `barnybug/tesla-cli`
12. `teslahunt/tesla-vin`
13. `cham/TeslaYay`
14. `AnalyticETH/tesla-security-research`
15. `evoffer/electric-liftgate-firmware`
16. `evoffer/auto-present-door-handles-firmware`
17. `pickeditmate/YardstickTeslaChargePortOpener`
18. `0xfokki/tesla-ym50k`
19. `0xfokki/tesla-yfjoy`

### P2 — archive as reference; extract only when a related feature is needed

20. `polymorphic/tesla-model-y-checklist`
21. `nelsonic/tesla-mobile-office`
22. `Corbin/Tesla-Theater-YT-BUG`
23. `BinaryVortex/Tesla-Model-Y-Mock-Page`
24. `midudev/landing-tesla`
25. `rocketseat-content/youtube-clone-tesla-homepage`
26. `dimitrypo/openpilot`

### Invalid / unresolved URLs supplied by user

27. `matthewhefferon/tesla-clone-y` — repository URL currently returns 404.
28. `AmirhosseinDotZip/tesla-clone-y` — the supplied URL was malformed as `tesla-clone-ythttps`; the exact intended repository could not be verified from that URL.

Do not invent replacement repositories for invalid URLs. Re-check later if the user supplies corrected links.

---

# Detailed repository decisions

## 1. outlandnish/tm3diag — P0 / highest priority

Repository: https://github.com/outlandnish/tm3diag

### Why it matters

This is the strongest direct diagnostic reference in the supplied set. Its README explicitly describes Tesla Model 3 CAN diagnostics and a general CAN/UDS interoperability framework. It contains an interactive diagnostic terminal, general UDS tooling, firmware-image parsing, CAN decoding, CAN-data-to-DBC conversion, gateway log parsing, a live CAN viewer, and bench emulators. The project explicitly supports both real SocketCAN hardware and `vcan` offline testing. fileciteturn42file0L2-L6

### Extract first

- CAN interface abstraction and message handling.
- CAN frame capture/logging model.
- UDS session/request/response patterns.
- DID read/write abstractions.
- routine-control abstractions.
- ECU identity discovery patterns.
- negative-response handling.
- timeout/retry/session handling.
- diagnostic terminal architecture.
- CAN decoder architecture.
- `candata_to_dbc.py` and `compact_to_dbc.py` concepts.
- `clog.py` gateway log parsing.
- `decode_bin.py`, firmware parsing, and firmware metadata extraction concepts.
- Model Y-related extraction paths; the repository explicitly handles `.modely` extraction roots.
- test strategy and virtual-CAN support.

### Do NOT blindly port

Do not copy firmware flashing or security-access behavior into AutoDiag-WiCAN-Pro without a separate safety/evidence gate. The README explicitly warns that ECU flashing can render safety-critical systems unrecoverable and that the project deliberately ships no seed/key or immobilizer algorithms. fileciteturn42file0L2-L6

### AutoDiag integration target

Map concepts into:

`core/can -> core/isotp -> core/uds -> diagnostics -> diagnostic-data -> evidence`

The desired result is an Android/Kotlin implementation of the architecture, not a Python dependency.

---

## 2. talas9/tesla_can_signals — P0 / signal-data goldmine

Repository: https://github.com/talas9/tesla_can_signals

### Why it matters

The Model Y directory contains a `ModelY_ETH.compact.json` of approximately 518 KB and a `legacy_self_test_parser.json` of approximately 83 KB. The repository is explicitly organized by Model 3, Model S, Model X and Model Y. fileciteturn61file0L2-L10

### Extract first

- Model Y compact signal database.
- signal names.
- CAN IDs.
- bit positions and lengths.
- scaling and offsets.
- units.
- multiplexing/enumerations where present.
- ECU/bus associations.
- legacy self-test parser information.
- differences between Model Y and Model 3 signal definitions.

### Critical evidence rule

Treat every signal as **external reference evidence**, not as vehicle-verified truth. Promote a signal to the AutoDiag verified set only when it is independently correlated with captures, a second source, or an actual vehicle test.

### AutoDiag integration target

Convert the useful portions into the project's diagnostic-data schema and provenance model. Keep source repository, source file, source revision, extraction timestamp, confidence, and verification state with each imported signal.

---

## 3. OBDb/Tesla-Model-Y — P0 / structured Model Y signal catalog

Repository: https://github.com/OBDb/Tesla-Model-Y

### Why it matters

This repository is specifically dedicated to Tesla Model Y signal-set configurations organized by model year and version. Its structure contains `generations.yaml` and `signalsets`, and its CI validates signalsets against a schema. fileciteturn47file0L2-L6 fileciteturn39file0L2-L10

The current v3 directory contains `default.json`; the repository's structure and validation workflow are themselves valuable because they demonstrate a machine-checkable signal-data contract. fileciteturn46file0L2-L10

### Extract first

- generations/model-year classification.
- signalset schema.
- naming conventions.
- Model Y-specific signal organization.
- CI validation rules.
- provenance/validation ideas.

### AutoDiag integration target

Use as a reference for a normalized diagnostic-data manifest and automated schema validation. Cross-correlate signal names and definitions against `talas9/tesla_can_signals`, `TeslaLogger`, `tm3diag`, and captured data.

---

## 4. bassmaster187/TeslaLogger — P0 / telemetry and historical-data architecture

Repository: https://github.com/bassmaster187/TeslaLogger

### Why it matters

This is a very large, mature Tesla telemetry/logging project. The current repository is roughly 462 MB according to GitHub metadata. Its tree includes logging, MQTT, map generation, TeslaFi import, update infrastructure and a substantial TeslaLogger solution. fileciteturn31file0L2-L13

### Extract first

- telemetry data model.
- trip/session model.
- time-series storage concepts.
- CAN/vehicle-state correlation where present.
- charging-session history.
- drive statistics.
- alert/event handling.
- MQTT integration patterns.
- import/export structures.
- data normalization and persistence strategies.
- any Tesla vehicle API abstractions that complement CAN data.

### AutoDiag integration target

Use the data-model ideas for `DtcHistoryStore`, diagnostic sessions, event timelines, and evidence correlation. Do not import the whole application architecture.

---

## 5. ekr/candash — P0 / Android dashboard and live CAN UI

Repository: https://github.com/ekr/candash

### Why it matters

CANdash is explicitly an Android application for Tesla Model 3/Y that turns an Android device into an instrument cluster. The repository contains an `android` project and is designed around live CAN-derived vehicle information and blind-spot visualization. fileciteturn43file0L2-L6

The README describes live dashboard data, speed/power/battery displays, performance gauges, and blind-spot logic based on vehicle sensor information. It also documents the network relationship between Android and a CANserver. fileciteturn43file0L2-L6

### Extract first

- Android project structure.
- CAN-to-UI data flow.
- live-update loop.
- vehicle-state model.
- dashboard rendering strategy.
- network/CANserver abstraction.
- Tesla Model Y/3 UI assumptions.
- sensor-derived feature logic.

### AutoDiag integration target

Use the Android-side architecture as a UI reference for a future live signal inspector and dashboard. Do not copy its network assumptions into the WiCAN transport layer.

---

## 6. tomas7470/tesladash — P0 / Raspberry Pi + SocketCAN dashboard

Repository: https://github.com/tomas7470/tesladash

### Why it matters

This project is a DIY Model Y/Model 3 dashboard using Raspberry Pi 4, a 7-inch display, OBDLink MX+, Qt/PyQt, SocketCAN, and an ELM327-to-SocketCAN driver. It explicitly integrates a Model 3 DBC. fileciteturn44file0L2-L6

The tree also contains `elmcan`, `setup_can0.sh`, a Tesla screen GUI, 3D models and STLs. fileciteturn35file0L2-L10

### Extract first

- SocketCAN setup and lifecycle.
- ELM327-to-SocketCAN integration concepts.
- DBC loading/use.
- real-time UI update architecture.
- startup/service handling.
- hardware abstraction.
- physical mounting only if a future hardware UI is needed.

### AutoDiag integration target

Use it as a second independent reference for the transport-to-dashboard boundary and DBC-based decoding.

---

## 7. tfoldi/fleetwise-iot-tesla3 — P0 / cloud telemetry + DBC evidence

Repository: https://github.com/tfoldi/fleetwise-iot-tesla3

### Why it matters

This project collects Tesla CAN telemetry and deploys an AWS IoT FleetWise edge/cloud solution. Its tree includes a full `model3can.dbc` (~322 KB), a reduced DBC, decoder manifest, signal catalog, campaign configurations and Grafana dashboard data. fileciteturn40file0L2-L10

The README confirms the purpose as CAN-bus telemetry collection and edge/cloud deployment. fileciteturn55file0L2-L6

### Extract first

- `model3can.dbc`.
- `model3can-reduced.dbc`.
- `decoder_manifest.json`.
- `signal_catalog.json`.
- campaign configuration patterns.
- signal-to-cloud mapping.
- telemetry timestamping.
- dashboard schema.

### AutoDiag integration target

Use the DBC and catalog as independent cross-checks, not as automatically verified Model Y data. The architecture is especially useful for the project's `capture -> decode -> correlate` pipeline.

---

## 8. clowrey/S3XY-BMS — P0 / BMS, CAN, isoSPI and test architecture

Repository: https://github.com/clowrey/S3XY-BMS

### Why it matters

This repository is unusually rich in explicit engineering documentation. It contains a Tesla BMS interface port using RP2350A, CAN, isoSPI, serial APIs, cell monitoring, current sensing and an ESPHome touchscreen. The README documents a 108+ parameter API, dual serial interfaces, real-time cell monitoring, CAN broadcast, isoSPI master/snooper modes and a unified BMB test interface. fileciteturn54file0L1-L2

The repository tree also contains dedicated documents for CAN message format, CAN integration, parameter API, implementation plans, exact balancing, dual serial API and development/session architecture. fileciteturn38file0L1-L2

### Extract first

- CAN message definitions.
- parameter API schema.
- signal naming conventions.
- serial command/response design.
- diagnostic/test harness concepts.
- passive isoSPI snooping architecture.
- evidence/test logging patterns.
- cell-level telemetry model.
- separation between acquisition, decoding, presentation and control.

### Safety boundary

Do not import contactor control, balancing control, pack-voltage actuation or other battery-control operations into AutoDiag merely because they exist here. For AutoDiag, the primary value is **read-only observation, decoding, logging and evidence architecture**.

---

## 9. evoffer/instrument-cluster-firmware — P0/P1 / firmware artifacts and CAN-enabled aftermarket cluster

Repository: https://github.com/evoffer/instrument-cluster-firmware

### Why it matters

The repository is very large and contains many dated firmware packages, including files explicitly labelled `(CAN)`, multiple Model Y/3-compatible display variants, and update packages. fileciteturn37file0L2-L10

### Extract first

- README/documentation.
- firmware package metadata.
- any plaintext configuration files.
- CAN-related documentation.
- firmware version-to-hardware mappings.
- update package structure.
- identifiers, signal names or message definitions if present in readable files.

### Binary policy

Do not assume that a firmware ZIP is useful merely because it exists. Catalog it first: filename, version/date, hardware family, checksum/hash if available, file types and whether readable source/configuration is present. Only perform binary reverse engineering when it answers a specific AutoDiag question.

---

## 10. timdorr/tesla-api — P0/P1 / official-ish historical owner API reference

Repository: https://github.com/timdorr/tesla-api

### Why it matters

This is a substantial historical Tesla API documentation/code repository. The root contains `ownerapi_endpoints.json` (~73 KB), API documentation, a Ruby library structure, specs and an API description. fileciteturn30file0L2-L2

### Extract first

- owner API endpoint catalog.
- vehicle state/command vocabulary.
- API object schemas.
- endpoint naming and semantics.
- historical authentication/session architecture only as context.
- differences between cloud vehicle state and local CAN state.

### AutoDiag integration target

Use it to distinguish **cloud/Owner API evidence** from **local CAN/UDS evidence**. It should not replace local diagnostic transport.

---

## 11. barnybug/tesla-cli — P1 / practical Owner API CLI

Repository: https://github.com/barnybug/tesla-cli

### Why it matters

This is a small Go CLI for querying and controlling Tesla Model S/3/X/Y vehicles. It includes vehicle listing, vehicle state, charge state and an explicit power-saving mode that avoids waking sleeping vehicles. fileciteturn52file0L2-L6

### Extract

- command abstraction.
- sleep/wake semantics.
- vehicle selection.
- charge-state vocabulary.
- cloud-vs-local distinction.

Do not use it as a source of CAN IDs.

---

## 12. teslahunt/tesla-vin — P1 / vehicle identity enrichment

Repository: https://github.com/teslahunt/tesla-vin

### Why it matters

The package decodes Tesla VINs into model, year, body type, motor, battery type, manufacturing plant and other identity attributes. The README states that it follows Model S/3/X/Y service manuals. fileciteturn51file0L2-L6

### Extract

- VIN parsing rules.
- model/year/motor/battery classification.
- identity schema.
- mapping from VIN to vehicle-family selection.

### AutoDiag integration target

Use VIN-derived identity to select the correct diagnostic-data generation and decoder candidates before capture analysis. VIN inference must remain separate from vehicle-verified signal evidence.

---

## 13. cham/TeslaYay — P1 / historical application integration

Repository: https://github.com/cham/TeslaYay

### Why it matters

TeslaYay is an application built on top of a TeslaAPI service, with Redis and a web application. It is not a CAN diagnostic project. fileciteturn57file0L2-L6

### Extract

Only reusable service/application patterns and API object assumptions. The README also points to a separate `TeslaAPI` repository; if that repository becomes discoverable, treat it as a separate candidate rather than assuming it is the same as `timdorr/tesla-api`.

---

## 14. AnalyticETH/tesla-security-research — P1 / security architecture and evidence only

Repository: https://github.com/AnalyticETH/tesla-security-research

### Why it matters

This is documented Tesla Model 3/Y infotainment security research covering ODIN, hermes, data-value access, persistence vulnerabilities and telemetry architecture. The repository says the vulnerabilities were responsibly disclosed and assigned CVEs where applicable. fileciteturn56file0L2-L6

### Extract safely

- infotainment architecture.
- service/component names.
- trust-boundary concepts.
- telemetry provenance.
- historical vulnerability/fix timeline.
- defensive lessons for diagnostic tooling.
- evidence that certain data originates from specific vehicle computers.

### Do not operationalize

Do not reproduce exploit chains, persistence mechanisms, token replay, telemetry spoofing, authentication bypasses, or commands against live vehicles as part of AutoDiag. The value here is architecture, threat modeling, provenance and defensive validation.

---

## 15. evoffer/electric-liftgate-firmware — P1 / actuator feature-specific firmware reference

Repository: https://github.com/evoffer/electric-liftgate-firmware

### Why it matters

The README documents multiple generations of aftermarket Tesla frunk/tailgate ECUs, hardware variants, BLE-connected versions and firmware/config update artifacts. It explicitly maps Model Y hardware/firmware variants. fileciteturn53file0L2-L6

### Extract

- hardware/firmware version mapping.
- Model Y actuator feature variants.
- BLE-capable ECU identification.
- configuration-vs-firmware separation.
- update artifact metadata.

Use only when implementing feature-specific identification or aftermarket-device detection.

---

## 16. evoffer/auto-present-door-handles-firmware — P1 / actuator feature reference

Repository: https://github.com/evoffer/auto-present-door-handles-firmware

### Decision

Retain for later extraction of hardware/firmware version mappings and possible CAN/BLE/control-state evidence. It is not a first-line diagnostic decoder source.

---

## 17. pickeditmate/YardstickTeslaChargePortOpener — P1/P2 / narrow hardware feature

Repository: https://github.com/pickeditmate/YardstickTeslaChargePortOpener

### Decision

The repository is extremely small. Keep the URL in the reference set because a dedicated charge-port opener can reveal useful feature-specific communication details, but do not spend core extraction time on it until charge-port control/identification becomes an explicit AutoDiag requirement.

---

## 18. 0xfokki/tesla-ym50k — P2 / too small for current extraction

Repository: https://github.com/0xfokki/tesla-ym50k

The current tree contains only a tiny JavaScript project with README, `index.js`, `package.json` and license. fileciteturn36file0L2-L10

### Decision

Archive/reference only. Re-check if the author later adds protocol, CAN, BLE or Tesla diagnostic code.

---

## 19. 0xfokki/tesla-yfjoy — P2 / currently empty

Repository: https://github.com/0xfokki/tesla-yfjoy

GitHub currently reports repository size 0 and no archived status. No useful source tree was identified.

### Decision

Watch only. No extraction now.

---

## 20. polymorphic/tesla-model-y-checklist — P2 / vehicle inspection knowledge, not code

Repository: https://github.com/polymorphic/tesla-model-y-checklist

### Why retain

The README is a detailed Model Y delivery/inspection checklist derived from owner reports, covering exterior, interior, charging, HVAC, cameras, blind spot, liftgate and other vehicle functions. fileciteturn50file0L2-L6

### AutoDiag use

Use as a **feature inventory and test-case source**, not as a software dependency. It can help turn physical vehicle functions into diagnostic verification scenarios.

---

## 21. nelsonic/tesla-mobile-office — P2 / usability only

Repository: https://github.com/nelsonic/tesla-mobile-office

### Decision

Retain for future Tesla usability/Android/mobile workflow ideas. Not a CAN/UDS extraction priority.

---

## 22. Corbin/Tesla-Theater-YT-BUG — P2 / historical UI bug evidence

Repository: https://github.com/Corbin/Tesla-Theater-YT-BUG

### Decision

Keep as historical Tesla web/UI behavior evidence. Do not prioritize for diagnostic decoding.

---

## 23. BinaryVortex/Tesla-Model-Y-Mock-Page — P2 / UI mock

Repository: https://github.com/BinaryVortex/Tesla-Model-Y-Mock-Page

### Decision

UI/reference only. Useful for interface ideas, not for CAN/UDS/signal extraction.

---

## 24. midudev/landing-tesla — P2 / frontend design only

Repository: https://github.com/midudev/landing-tesla

### Decision

Frontend/landing-page reference. No diagnostic extraction priority.

---

## 25. rocketseat-content/youtube-clone-tesla-homepage — P2 / frontend clone

Repository: https://github.com/rocketseat-content/youtube-clone-tesla-homepage

### Decision

Frontend training/example project. Do not extract for vehicle diagnostics.

---

## 26. dimitrypo/openpilot — P2 / large autonomy reference

Repository: https://github.com/dimitrypo/openpilot

### Why retain

The repository has a Tesla-specific default branch named `frogtesla`, indicating a Tesla-oriented fork/branch. GitHub reports a very large codebase. This makes it potentially valuable for vehicle interface, CAN, signal, safety and integration concepts.

### Extract later

- Tesla-specific vehicle interface code.
- CAN message handling.
- signal packing/unpacking.
- vehicle state architecture.
- safety boundary concepts.
- actuator/state abstractions.

### Safety boundary

Do not import autonomous driving/control logic into AutoDiag. Extract only read-only protocol/data-model concepts relevant to diagnostics.

---

# Cross-repository extraction order

## Phase A — current APK/Android diagnostic foundations

Run in parallel with the current AutoDiag-WiCAN-Pro implementation:

1. `tm3diag`
2. `candash`
3. `tesladash`
4. `tesla_can_signals`
5. `OBDb/Tesla-Model-Y`

Primary questions:

- How is raw CAN represented?
- How are streams/fragments reconstructed?
- How are ISO-TP sessions represented?
- How are UDS requests and responses matched?
- How are signals mapped to frames?
- How are Model Y generations separated?
- How is live data delivered to an Android UI?

## Phase B — evidence/data expansion

6. `TeslaLogger`
7. `fleetwise-iot-tesla3`
8. `S3XY-BMS`
9. `instrument-cluster-firmware`
10. `tesla-api`

Primary questions:

- How do we persist time-series vehicle evidence?
- How do we correlate multiple sources?
- How do we normalize DBC/signal catalogs?
- How do we record source provenance?
- How do we distinguish cloud state from local bus state?
- How can BMS/ECU evidence be represented safely?

## Phase C — identity, feature and security context

11. `tesla-vin`
12. `tesla-cli`
13. `electric-liftgate-firmware`
14. `auto-present-door-handles-firmware`
15. `TeslaYay`
16. `tesla-security-research`
17. `openpilot` Tesla branch

## Phase D — only when required

UI clones, mock pages, mobile-office projects and narrow feature repositories.

---

# Evidence rules for AI

1. Never mark a Tesla signal as `vehicle_verified` merely because it exists in a public DBC, JSON database, Python decoder or GitHub repository.
2. Record every imported item with source repository, path, revision/commit, extraction date and original identifier.
3. Prefer independent agreement between at least two technically independent sources.
4. Prefer actual captured vehicle evidence over static documentation.
5. When sources disagree, preserve both candidates and mark the conflict; do not silently choose one.
6. Model Y data must not be assumed identical to Model 3 data.
7. VIN/model-year information should influence candidate selection but never prove a CAN signal by itself.
8. Firmware artifacts are evidence sources, not automatically executable inputs.
9. Security research is for architecture, provenance and defensive validation unless a separate, explicitly authorized research task exists.
10. Battery/BMS/contactor/control functionality is read-only by default in AutoDiag.
11. External code is reference architecture, not a dependency.
12. Any useful Python/C/C++/Go implementation must be translated into Kotlin/Android-native architecture where appropriate rather than mechanically ported.

---

# Required extraction record

For every repository actually mined, create or update a repository evidence record containing:

- repository URL
- owner/name
- default branch
- inspected commit SHA
- inspection date
- license
- repository role
- priority
- useful files/directories
- functions/data extracted
- target AutoDiag module
- confidence
- verification state
- known conflicts
- safety restrictions
- follow-up extraction tasks

Recommended target modules:

- `core/can`
- `core/slcan`
- `core/isotp`
- `core/uds`
- `diagnostics`
- `diagnostic-data`
- `evidence`
- `DtcHistoryStore`
- `VerificationState`
- live signal viewer/dashboard

---

# Immediate next action

Do not spend time extracting frontend clone repositories. Start a parallel technical extraction of:

**`tm3diag + tesla_can_signals + OBDb/Tesla-Model-Y + TeslaLogger + CANdash + tesladash + fleetwise-iot-tesla3 + S3XY-BMS`**

The highest-value immediate targets are:

1. `tm3diag` — CAN/UDS/diagnostic implementation patterns.
2. `tesla_can_signals` — large Model Y signal database.
3. `OBDb/Tesla-Model-Y` — structured Model Y signal-set schema and validation.
4. `fleetwise-iot-tesla3` — large DBC + decoder manifest + signal catalog.
5. `TeslaLogger` — mature telemetry/history architecture.
6. `CANdash` and `tesladash` — live dashboard and Android/SocketCAN integration.
7. `S3XY-BMS` — BMS/CAN/isoSPI/test architecture and detailed engineering documentation.

These should be cross-correlated before promoting any new signal or diagnostic capability into the main AutoDiag-WiCAN-Pro evidence set.
