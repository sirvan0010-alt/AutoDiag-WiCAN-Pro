# AutoDiag-WiCAN-Pro — Master Plan & Decisions

**Date:** 2026-09-02  
**Status:** Master specification / implementation backlog  
**Purpose:** Preserve the complete set of requirements and architectural decisions agreed during the 2026-09-02 design session before implementation continues.

> This document is the source of truth for the next implementation stages. It deliberately separates **protocol facts**, **vehicle-specific data**, **diagnostic interpretation**, **repair knowledge**, **estimates**, and **UI**. No signal, CAN ID, threshold, part number, repair step, or protocol behavior is to be invented.

---

## 1. Product goal

Build an open, modular Android automotive diagnostics and automation platform around **WiCAN PRO**, usable across ICE, hybrid, PHEV and EV vehicles.

The application must be **READ-first** and evidence-driven:

- connect to WiCAN PRO reliably;
- automatically detect and expose available communication methods;
- discover the vehicle and its ECUs/modules;
- read generic OBD-II data where supported;
- read manufacturer-specific data through verified vehicle profiles;
- provide live data, graphs, dashboards and HUD;
- run diagnostic tests and monitor results, including non-continuous monitors / Mode 06 where available;
- collect DTCs from all reachable ECUs;
- explain DTCs with evidence and vehicle context;
- when reliable repair data exists, show parts, part numbers, labor time, price ranges and repair procedures for the exact vehicle scope;
- produce pre-purchase / automated diagnostic reports;
- support EV battery and high-voltage diagnostics with explicit safety and confidence handling;
- support telemetry, MQTT/Home Assistant and automation;
- keep any future WRITE/coding/service functions isolated, disabled by default and explicitly verified.

The architecture must work for **all supported cars**, not just Tesla. Tesla is the reference implementation because its public service documentation provides a useful model for linking diagnostics to service procedures.

---

# 2. Core architecture

The common vehicle model is:

```text
VehicleProfile
 ├─ vehicle
 ├─ ECU[]
 │   ├─ identification
 │   ├─ protocol
 │   ├─ addressing
 │   ├─ capabilities
 │   ├─ sensors[]
 │   ├─ monitors[]
 │   └─ dtcs[]
 ├─ liveData[]
 ├─ monitors[]
 ├─ dashboard[]
 ├─ communication[]
 └─ verification[]
```

Common data flow:

```text
UI
 ↓
Use cases / session orchestration
 ↓
Diagnostic analysis
 ↓
Vehicle profile decoders
 ↓
UDS / ISO-TP / OBD protocol layers
 ↓
CAN / ELM / SLCAN framing
 ↓
WiCAN transport
```

### Architectural rule

Transport code must not contain Tesla, Škoda, VW, Hyundai, Kia, BMW, etc. business meaning.

Protocol layers must not contain UI behavior.

Vehicle profiles must not implement TCP framing.

Repair knowledge must not be hard-coded into a DTC parser.

---

# 3. Communication and WiCAN PRO

## 3.1 Automatic protocol detection

On connection the application should attempt verified detection of available methods and select the best supported method automatically.

Target protocol families:

- ISO 15765-4 CAN, 11-bit, 500 kbit/s;
- ISO 15765-4 CAN, 29-bit, 500 kbit/s;
- CAN 11-bit, 250 kbit/s;
- CAN 29-bit, 250 kbit/s;
- KWP2000 / ISO 9141 K-Line where hardware and vehicle support it;
- raw CAN / SLCAN where appropriate.

AUTO mode must never claim a protocol as supported merely because a probe timed out or because a profile says it should exist. Detection must produce evidence.

## 3.2 Manual selection

The user must be able to override AUTO and select a protocol manually.

The UI should show:

- selected protocol;
- detected protocols;
- unavailable/failed protocols;
- bitrate;
- CAN identifier mode;
- connection state;
- latency;
- effective sampling rate;
- frames/s where meaningful;
- RX/TX counts;
- errors/timeouts/dropped frames where measurable.

## 3.3 WiCAN transport

Support the transport capabilities relevant to WiCAN PRO, including as applicable:

- Wi-Fi TCP;
- WebSocket endpoints where verified by firmware/API;
- UDP broadcast/unicast for live-data paths where verified;
- BLE where supported by the hardware/firmware;
- ELM327 TCP endpoint (commonly port 3333);
- SLCAN/raw CAN paths (commonly port 23 in existing WiCAN usage).

Firmware/API endpoints discussed and requiring verification against the actual firmware before being treated as stable:

- `/ws_client.js`
- `/autopid_data`
- `/littlefs/auto_pid.json`
- `car_data.json`
- `/store_config`
- `/store_auto_data`
- `/check_status`

The documented firmware reference discussed was **v4.51p_beta-01**. Firmware/API compatibility must be versioned rather than assumed.

## 3.4 Session lifecycle

Implement explicit events/states such as:

- `SESSION_STARTED`
- `TRANSPORT_CONNECTED`
- `ELM_INITIALIZED`
- `CAPABILITY_DISCOVERED`
- `VEHICLE_IDENTIFIED`
- `ECU_DISCOVERED`
- `MEASUREMENT_RECEIVED`
- `DTC_RECEIVED`
- `TEST_PHASE_STARTED`
- `TEST_PHASE_COMPLETED`
- `LOAD_STARTED`
- `LOAD_STOPPED`
- `RECOVERY_STARTED`
- `BUS_HEALTH_CHANGED`
- `CAPTURE_STARTED`
- `CAPTURE_STOPPED`
- `ANALYSIS_COMPLETED`
- `SESSION_ENDED`

This Diagnostic Event Stream becomes the common backbone for UI, logging, replay, reports and automation.

---

# 4. Communication performance and adaptive sampling

The application must measure communication instead of pretending that the requested sampling rate is always achieved.

Track where possible:

- command latency;
- response latency;
- round-trip time;
- actual samples/s;
- frames/s;
- RX/TX throughput;
- timeout rate;
- dropped/invalid frame count;
- queue/backpressure;
- bus load when raw CAN statistics permit it.

## Sampling modes

User-selectable target rates:

- 10 Hz;
- 20 Hz;
- 50 Hz;
- MAX / hardware-limited;
- AUTO/adaptive.

The Live Data Engine should adapt based on:

- number of selected PIDs/signals;
- protocol round-trip time;
- ECU response rate;
- transport latency;
- bus load;
- dropped frames/timeouts;
- WiCAN hardware limitations.

Where possible, group compatible PID requests instead of issuing unnecessary sequential commands.

Hardware CAN filtering should be used when supported, including ESP32-S3/TWAI filtering where applicable.

---

# 5. ELM327 / OBD-II protocol foundation

Implement typed protocol responses rather than passing raw strings directly to UI.

Required foundations:

- command/response normalization;
- prompt (`>`) framing;
- buffering of fragmented TCP chunks;
- timeout/error classification;
- adapter capability discovery;
- protocol selection;
- multi-ECU addressing;
- ISO-TP segmentation/reassembly;
- negative response handling;
- traceable raw response storage.

Existing Mode 01 support must be evolved into a registry-driven decoder system rather than a small hard-coded PID list.

## Generic OBD-II target coverage

### Mode 01 — current data

Build a PID registry supporting, as evidence permits:

- engine RPM;
- vehicle speed;
- coolant temperature;
- engine load;
- intake air temperature;
- MAP;
- MAF;
- throttle/pedal position;
- ignition timing;
- control module voltage where available;
- fuel-related values;
- emissions-related values;
- supported-PID bitmaps;
- vehicle-specific extensions.

Existing implementation currently includes a small set such as RPM, speed, coolant temperature and MAP; do not treat this as complete.

### Mode 02 — freeze frame

Parse and associate freeze-frame PIDs with the DTC/event that caused the frame where available.

### Mode 03 — stored DTCs

Implement protocol-level parsing first, then pass normalized DTC objects to the knowledge layer.

### Mode 04 — clear DTCs

Keep clearing explicitly separated from read-only diagnostics. It must require deliberate user action and clear safety/confirmation semantics.

### Mode 05 — oxygen sensor monitoring

Support where the vehicle/ECU exposes standard Mode 05 data.

### Mode 06 — on-board monitoring

This is critical for non-continuous monitors such as the previously discussed EGR example.

Store at least:

- OBDMID;
- TID;
- CID;
- current/test value;
- minimum;
- maximum;
- scaling;
- unit;
- pass/fail/within-limit state;
- raw response;
- ECU/source;
- verification/provenance.

Example discussed: Škoda Fabia EGR monitor with current approximately 2.222%, minimum -35%, maximum 11.899%, reported OK. This is an example of the data shape, not a universal EGR limit.

**Important:** Mode 06 limits are monitor/ECU/vehicle specific. Never generalize one vehicle's limits to all vehicles.

### Mode 07 / Mode 0A

Support pending and permanent DTCs where exposed.

### Mode 09

Support identification data such as:

- VIN;
- calibration IDs;
- calibration verification numbers;
- other standardized identifiers as supported.

### Multi-ECU

The architecture must permit multiple ECUs to answer the same standardized request and must preserve ECU identity/source for every response.

---

# 6. ECU discovery / full vehicle scan

The application should be able to discover as many reachable modules as the vehicle/protocol permits, for example:

- engine / powertrain;
- transmission;
- ABS/ESC;
- SRS/airbag;
- 4WD/AWD;
- BCM/body;
- instrument cluster;
- climate/HVAC;
- BMS/HV battery;
- charging controller;
- gateway;
- steering;
- ADAS/camera/radar modules;
- other manufacturer-specific ECUs.

For each discovered ECU preserve:

- human name;
- ECU address / functional address where known;
- protocol;
- request/response addressing;
- VIN relation;
- software identification;
- hardware identification;
- calibration information;
- supported services;
- supported measurements;
- supported DTC services;
- diagnostic session capabilities;
- evidence of discovery;
- verification state.

Hard-coded address mappings may exist only as **illustrative/profile data** and must never be presented as universal.

---

# 7. Vehicle identity and capability model

Create a vehicle-scoped identity model containing, where known:

- VIN;
- manufacturer;
- model;
- generation;
- model year;
- production date/range;
- engine/motor;
- battery variant;
- transmission;
- drivetrain;
- region/market;
- ECU identities;
- ECU software/hardware versions;
- protocol configuration.

Capabilities must be cached by vehicle/ECU scope with provenance and revision information.

A signal should be considered exact only when the vehicle/ECU/profile scope supports it.

---

# 8. Live Data Engine

The user must be able to select **1–16 live values** simultaneously.

The UI must support:

- stacked time-series graphs;
- synchronized timestamps;
- rolling history buffer;
- pause/freeze view;
- min/max/current/statistics;
- landscape phone layout;
- readable units;
- quality/availability indicators;
- calculated-vs-measured distinction;
- raw-source inspection;
- adaptive sampling information.

HTML5 Canvas/WebGL was discussed as a possible rendering direction for high-frequency graphs, but implementation should follow the Android UI architecture already present in the repository.

---

# 9. Standard ICE live data

Where the vehicle supports the measurement, target:

- RPM;
- speed;
- engine load;
- coolant temperature;
- intake air temperature;
- throttle position;
- accelerator pedal position;
- MAP/boost;
- MAF;
- ignition timing;
- fuel pressure/rail pressure where available;
- lambda/O2 values where available;
- control module voltage;
- ambient/other temperature values;
- vehicle-specific sensors.

The app must distinguish:

- measured;
- calculated;
- inferred;
- unavailable;
- unknown;
- unverified.

---

# 10. EV / hybrid live data

Use the same architecture, but expose EV-specific measurements when supported:

- HV pack voltage;
- HV pack current;
- HV pack power;
- SoC;
- SoH;
- battery temperature min/max/average;
- cell voltage min/max;
- cell voltage delta;
- cell temperature delta;
- charge/discharge state;
- charging power/state;
- motor RPM;
- motor torque where actually available;
- inverter/motor temperatures;
- isolation resistance/status;
- 12 V battery voltage/status;
- thermal-management state;
- drive-unit data;
- vehicle-specific BMS values.

Never invent a Tesla signal/CAN ID or universal EV threshold.

---

# 11. EV battery health analysis

Battery health must be an orchestration layer over measured evidence, not a single guessed SoH number.

Target analysis phases:

- `STATIC`
- `LOAD`
- `RECOVERY`
- `TREND`
- `CONFIDENCE`

The analysis should include, where data exists:

- cell spread at rest;
- cell spread under load;
- recovery behavior;
- temperature influence;
- SoC influence;
- charging behavior;
- discharge behavior;
- pack voltage/current/power;
- capacity-related evidence;
- isolation status;
- drive-unit state;
- repeated fingerprints over time.

A low cell voltage under load must be interpreted in context; it is not automatically a failed cell/module.

Status-only isolation information must never be converted into a fabricated resistance value such as mΩ.

---

# 12. Dashboard

Create configurable dashboard widgets/gauges for values such as:

- RPM;
- speed;
- boost/MAP;
- power;
- torque;
- SoC;
- pack voltage/current/power;
- temperatures;
- battery delta;
- selected diagnostics.

Dashboard configuration must be profile/data driven rather than vehicle logic embedded in composables.

---

# 13. HUD / mirrored display

Provide a HUD mode intended for windshield reflection.

Requirements:

- horizontally mirrored output;
- large high-contrast values;
- minimal distractions;
- configurable widgets;
- live update;
- landscape-friendly layout;
- warning state that is understandable without reading small text.

The HUD uses the same Live Data / Dashboard data source as the normal UI.

---

# 14. Diagnostic Event Stream and evidence model

Every important diagnostic fact should be traceable to evidence.

Measurements should preserve at least:

- value;
- unit;
- timestamp;
- source;
- ECU;
- vehicle scope;
- raw representation;
- quality;
- availability;
- verification;
- measured/calculated/inferred flag.

Verification states:

- `UNKNOWN`
- `UNVERIFIED`
- `PARTIALLY_VERIFIED`
- `VERIFIED`

Availability states:

- `AVAILABLE`
- `PARTIAL`
- `UNAVAILABLE`
- `UNKNOWN`
- `ERROR`

This prevents an unsupported or missing feature from silently becoming PASS/FAIL.

---

# 15. Raw CAN tools

Implement a proper raw CAN layer and UI tools for:

- frame parsing;
- CAN 11-bit/29-bit handling;
- bitrate;
- filters;
- frame counters;
- RX/TX statistics;
- bus load where measurable;
- error frames/status where available;
- dropped-frame counters;
- capture;
- replay;
- indexed replay sessions.

**Replay safety:** replay must never implicitly transmit to a live vehicle. A replay session is analysis/simulation by default.

---

# 16. Capture / replay / simulator

All new decoders should be testable without a real vehicle.

Provide:

- deterministic simulator inputs;
- recorded CAN/ELM traces;
- replay indexes;
- parser tests;
- protocol tests;
- vehicle-profile tests;
- regression fixtures.

The simulator/replay path is the first validation layer before real-vehicle testing.

---

# 17. DTC architecture

The DTC pipeline must be:

```text
ECU / protocol response
 ↓
DTC parser
 ↓
Normalized DTC
 ↓
Vehicle/ECU context
 ↓
Knowledge lookup
 ↓
Diagnostic interpretation
 ↓
Repair intelligence
 ↓
Estimate / procedure / report
```

A DTC must **not** directly equal a failed component.

The app should show:

- code;
- ECU;
- protocol/source;
- textual meaning where verified;
- severity;
- symptoms;
- possible causes;
- diagnostic checks;
- evidence;
- related components;
- related tests;
- repair references;
- confidence/verification.

A component/part shortcut is allowed only when a source explicitly supports it or repeated validated evidence justifies a carefully qualified association.

---

# 18. Repair intelligence — DTC to mechanic-ready information

This is a major product requirement.

When a DTC is shown and reliable data exists, the app should immediately build a **Repair Intelligence Card** for the exact vehicle scope.

Target content:

1. DTC and ECU
2. What it means
3. Severity / drivability / safety relevance
4. Possible causes
5. Diagnostic checks
6. Required parts
7. OEM/manufacturer part numbers
8. Superseded/replacement part numbers
9. Quantity
10. New/reuse/discard requirements
11. Labor time
12. Labor cost
13. Parts cost
14. Total estimated range
15. Tools
16. Mechanical repair procedure
17. Electrical repair / wiring guidance
18. Calibration/programming requirements
19. Safety prerequisites
20. Post-repair checks
21. DTC clear / verification step where appropriate
22. Direct source links
23. Source provenance
24. Data age/revision
25. Confidence/verification status

The estimate must be a range when exact price or labor information is not available.

Never present an invented exact price, part number, labor time or procedure.

---

# 19. Exact vehicle matching for repair data

Repair knowledge must be matched against:

- VIN where possible;
- manufacturer;
- model;
- generation;
- model year;
- production date;
- engine/motor;
- battery variant;
- transmission;
- drivetrain;
- region;
- ECU;
- ECU software/hardware;
- protocol/diagnostic context.

If exact matching cannot be proven, the UI must say that the information is approximate or broader-scope rather than exact.

---

# 20. Repair source hierarchy

Preferred evidence order:

1. Official OEM service manual / technical documentation
2. Official OEM service portal
3. Licensed professional repair database
4. OEM TSB / recall / service campaign
5. Reputable professional repair database
6. Verified community data
7. General web/video/forum material

Source records must contain:

- provider;
- source type;
- vehicle scope;
- access level;
- URL/reference;
- revision/date where known;
- verification state;
- license/access notes.

Copyright rule: do not copy large proprietary/OEM manuals into the repository. Store metadata, references, permitted summaries and direct links; integrate licensed data where legally available.

---

# 21. Tesla reference source

The Tesla public Model Y DIY documentation supplied during the session is an important public reference:

`https://service.tesla.com/docs/Public/diy/modely/cs_cz/`

It explicitly warns that procedures/parts may vary by configuration/region and that the exact vehicle and applicable procedure must be verified. It also contains DIY areas covering general procedures, power/restart, wheels/tires/brakes, wipers, doors/windows, filters, cameras/windows, frunk/trunk, accessories, towing, low-voltage battery and charging.

Use it as a **public source link and source metadata**, not as a reason to copy the manual contents wholesale.

Other Tesla service references discussed:

- Tesla Service Manual
- Tesla Service Mode / Service Mode Plus
- Tesla public DIY documentation
- Tesla service procedure correction codes / labor information where publicly exposed

For high-voltage procedures, the application must preserve the source's qualification/safety requirements and must not turn dangerous HV work into casual home instructions.

---

# 22. Labor and price estimation

The application needs a transparent estimate engine.

## Labor classes

Support:

- OEM flat-rate / warranty time;
- published professional standard time;
- independent estimate;
- user-configured estimate.

User settings should include:

- currency;
- country/region;
- workshop hourly rate;
- dealer vs independent shop;
- VAT/tax treatment;
- markup;
- preferred parts source.

## Parts data

Each part record should preserve:

- component;
- OEM/manufacturer number;
- supersession;
- quantity;
- mandatory/optional;
- new/reuse/discard;
- source;
- region/currency;
- date/currency of price;
- price type;
- availability/link;
- confidence.

## Total estimate

```text
parts subtotal
+ labor time × selected labor rate
+ optional shop charges
+ applicable taxes/markup
= transparent total estimate
```

If only a range is available, show a range and explain its origin.

---

# 23. Professional repair data integrations

Professional data providers were discussed as possible integration targets, not as data that may simply be copied into the open-source repository.

Examples:

- ALLDATA Repair / ALLDATA Labour Times;
- Mitchell 1 ProDemand / Estimate Guide;
- other licensed OEM/professional databases.

The architecture should support a `RepairSourceResolver` so providers can be added without changing DTC parsers or UI.

Provider access must be explicit: public, licensed, authenticated, unavailable, or link-out-only.

---

# 24. Repair procedure model

Create a structured `RepairProcedure` model containing:

- vehicle scope;
- DTC/ECU/component scope;
- procedure type;
- source;
- access;
- revision/date;
- labor time;
- correction code where applicable;
- tools;
- parts;
- safety warnings;
- prerequisites;
- steps/summary;
- torque information where legally/source-permitted;
- wiring information where legally/source-permitted;
- calibration/programming;
- post-repair checks;
- verification state.

Procedure types should distinguish at least:

- inspection;
- mechanical repair;
- electrical diagnosis;
- wiring repair;
- replacement;
- calibration;
- programming;
- adaptation;
- HV/EV procedure;
- post-repair verification.

---

# 25. Two repair UI modes

Discussed product concept:

### "Jsem doma"

For a skilled DIY user:

- required tools;
- required parts;
- part numbers;
- mechanical/electrical procedure summary;
- safety prerequisites;
- difficulty;
- estimated time;
- estimated parts/labor cost;
- post-repair checks;
- direct source link.

### "Jsem v servisu"

For professional use:

- full ECU/DTC context;
- diagnostic evidence;
- repair procedure reference;
- labor operation/correction code;
- parts list;
- wiring/diagnostic references;
- calibration/programming requirements;
- report/export.

The app must not lower safety requirements in DIY mode.

---

# 26. Pre-purchase / automatic diagnostic workflow

Implement a state machine for automated testing.

Suggested phases:

```text
CONNECT
 ↓
IDENTIFY VEHICLE
 ↓
DISCOVER ECUs
 ↓
READ DTCs
 ↓
READ FREEZE FRAME
 ↓
READ READINESS
 ↓
READ LIVE DATA
 ↓
RUN AVAILABLE MONITORS
 ↓
EV/BATTERY TESTS IF SUPPORTED
 ↓
ANALYZE
 ↓
BUILD REPAIR ESTIMATES
 ↓
GENERATE REPORT
```

Unsupported steps must remain `UNAVAILABLE`/`UNKNOWN`; they must not become PASS.

---

# 27. Report model

Reports should preserve:

- vehicle identity;
- session timestamp;
- WiCAN/adapter identity;
- communication metrics;
- discovered ECUs;
- DTCs;
- freeze-frame data;
- readiness;
- Mode 06 results;
- live-data snapshots;
- EV battery health analysis;
- evidence/provenance;
- repair intelligence;
- parts;
- labor;
- cost estimates;
- confidence/verification;
- warnings;
- unsupported areas.

Reports should be useful for a private owner, buyer, technician and service discussion without pretending to be an OEM certificate.

---

# 28. Knowledge Base runtime

The current `DtcKnowledge` concept should evolve into a general knowledge runtime containing:

- DTC definitions;
- symptoms;
- possible causes;
- diagnostic tests;
- repair references;
- vehicle scope;
- ECU scope;
- sources;
- verification;
- revision/date;
- provider/license metadata.

Add a source resolver and evidence ranking system.

Knowledge lookup must be deterministic and testable.

---

# 29. Vehicle profiles

Vehicle profiles are the scalable mechanism for manufacturer-specific information.

A profile may contain:

- vehicle matching criteria;
- ECU list;
- request/response IDs;
- diagnostic protocol;
- PID/signal definitions;
- units/scaling;
- cell/temperature/HV definitions;
- capabilities;
- source/provenance;
- verification state;
- revision.

External WiCAN profile/reverse-engineering projects discussed during the session may be used as research inputs, but their values must be independently verified before being promoted to `VERIFIED`.

---

# 30. Manufacturer expansion

After generic OBD + Tesla/EV foundations, expand through profiles/protocol layers to:

- VAG: VW / Audi / Škoda / SEAT / CUPRA;
- Hyundai / Kia;
- BMW;
- Mercedes-Benz;
- Renault / Dacia;
- Nissan;
- Mitsubishi;
- Toyota;
- Ford;
- GM;
- Stellantis brands;
- Volvo / Polestar;
- additional manufacturers as evidence becomes available.

Protocol foundations should include, as required by the vehicle:

- UDS;
- ISO-TP;
- KWP2000;
- K-Line;
- manufacturer-specific diagnostic services.

---

# 31. UDS / service framework

Future read-oriented UDS foundations should support, where verified:

- Diagnostic Session Control `0x10`;
- Security Access `0x27` only in a controlled framework;
- Read Data By Identifier `0x22`;
- Write Data By Identifier `0x2E` only in the future isolated WRITE layer;
- Routine Control `0x31` only when explicitly supported and safely isolated.

All WRITE/coding/adaptation/service-actuation functionality is:

- separate from read-only code;
- disabled by default;
- explicitly gated;
- vehicle/profile scoped;
- evidence verified;
- confirmation required;
- never triggered automatically by a DTC.

---

# 32. Automation / MQTT / Home Assistant

Extend the same diagnostic event and measurement model to:

- MQTT;
- Home Assistant;
- remote telemetry;
- parked/driving notifications;
- threshold alerts;
- charge-cost calculations;
- vampire-drain monitoring;
- geofencing;
- sampling calibration;
- automated reports.

Automation must operate on typed events/measurements, not UI strings.

---

# 33. UI information quality

Every important value should expose a compact information/tooltip mechanism.

Tooltip metadata should be schema-driven and may include:

- definition;
- unit;
- source;
- calculation;
- normal context;
- verification;
- limitations;
- vehicle scope.

The UI must make it visually obvious when a value is:

- measured;
- calculated;
- inferred;
- unavailable;
- unverified;
- verified.

---

# 34. Safety rules — non-negotiable

1. Read-only is the default.
2. Never invent CAN IDs, signals, PID scaling, thresholds or repair procedures.
3. Never convert unknown status into a numerical measurement.
4. Never claim an unsupported test passed.
5. Never present a DTC as proof that one exact component failed unless evidence supports it.
6. Never infer universal battery-health limits from one vehicle.
7. Never infer resistance from a status-only isolation result.
8. Never automatically transmit replay traffic to a live vehicle.
9. Never automatically clear DTCs.
10. Never automatically perform actuator tests/coding/programming.
11. Keep HV procedures visibly safety-gated.
12. Preserve source/provenance for diagnostic and repair claims.
13. Preserve exact vehicle/ECU scope.
14. Preserve uncertainty instead of hiding it.
15. Licensed/proprietary repair information must not be copied into the open repository without permission.

---

# 35. Implementation sequence

This is the agreed progression. Work incrementally; each stage should compile and have tests before the next major layer is added.

## P0 — Architecture and evidence foundation

- [ ] Vehicle identity/scope model
- [ ] ECU identity/capability model
- [ ] typed measurement model
- [ ] verification/availability states
- [ ] Diagnostic Event Stream
- [ ] source/provenance model
- [ ] repository architecture cleanup
- [ ] update stale AI handoff/context docs

## P1 — WiCAN communication foundation

- [ ] transport abstraction
- [ ] TCP/ELM framing robustness
- [ ] raw CAN/SLCAN transport
- [ ] reconnect lifecycle
- [ ] protocol detection
- [ ] communication metrics
- [ ] trace/logging
- [ ] capability discovery
- [ ] simulator/replay fixtures

## P2 — Protocol stack

- [ ] typed ELM responses
- [ ] OBD-II normalizer
- [ ] ISO-TP
- [ ] negative responses
- [ ] multi-ECU support
- [ ] CAN frame parser/filter/stats

## P3 — Generic OBD

- [ ] registry-driven Mode 01
- [ ] supported-PID discovery
- [ ] live-data scheduler
- [ ] Mode 02 freeze frame
- [ ] Mode 03 DTC
- [ ] Mode 04 clear, gated
- [ ] Mode 05
- [ ] Mode 06 monitors
- [ ] Mode 07 pending DTC
- [ ] Mode 09 VIN/CALID/CVN
- [ ] Mode 0A permanent DTC
- [ ] readiness

## P4 — ECU discovery

- [ ] functional scan
- [ ] physical ECU discovery
- [ ] ECU identification
- [ ] software/hardware IDs
- [ ] capability persistence
- [ ] multi-ECU DTC aggregation

## P5 — Live Data + UI

- [ ] 1–16 selectable values
- [ ] rolling graphs
- [ ] landscape layout
- [ ] dashboard gauges
- [ ] mirrored HUD
- [ ] sampling controls
- [ ] communication quality display

## P6 — Tesla READ profile

- [ ] exact vehicle/profile matching
- [ ] verified signal catalog
- [ ] Tesla diagnostic decoding
- [ ] EV measurements
- [ ] service/DIY source metadata

## P7 — EV health

- [ ] STATIC/LOAD/RECOVERY/TREND/CONFIDENCE
- [ ] cell spread context
- [ ] battery fingerprint
- [ ] charging analysis
- [ ] thermal analysis
- [ ] isolation-status analysis
- [ ] drive-unit analysis
- [ ] EV report

## P8 — Repair intelligence

- [ ] RepairSource
- [ ] RepairProcedure
- [ ] RepairPart
- [ ] LaborEstimate
- [ ] PriceEstimate
- [ ] RepairEstimateEngine
- [ ] DTC → repair references
- [ ] exact vehicle matching
- [ ] source resolver
- [ ] DIY/service UI modes
- [ ] transparent estimates
- [ ] source links/provenance

## P9 — Pre-purchase automation/reporting

- [ ] test state machine
- [ ] evidence aggregation
- [ ] report model
- [ ] PASS/FAIL only where supported
- [ ] repair-cost summary

## P10 — Knowledge/provider layer

- [ ] OEM source registry
- [ ] licensed provider adapters
- [ ] ALLDATA/Mitchell-style integration boundary
- [ ] offline/cache behavior
- [ ] revision/update handling

## P11 — Remote/automation

- [ ] MQTT
- [ ] Home Assistant
- [ ] telemetry
- [ ] alerts
- [ ] geofence
- [ ] charge-cost / vampire drain

## P12 — Additional vehicle families

- [ ] VAG
- [ ] Hyundai/Kia
- [ ] BMW
- [ ] Mercedes
- [ ] Renault/Dacia
- [ ] Nissan/Mitsubishi
- [ ] Toyota/Ford/GM/Stellantis/Volvo/Polestar

## P13 — Isolated WRITE framework

- [ ] UDS service framework
- [ ] explicit capability gates
- [ ] safety review
- [ ] confirmation UX
- [ ] dry-run/simulator first
- [ ] no arbitrary write implementation

---

# 36. Concrete code components to implement

The next code pass should introduce or evolve components with responsibilities similar to:

```text
core/
 ├─ vehicle/
 │   ├─ VehicleIdentity
 │   ├─ VehicleScope
 │   ├─ EcuIdentity
 │   └─ CapabilitySnapshot
 │
 ├─ diagnostics/
 │   ├─ DiagnosticEvent
 │   ├─ Measurement
 │   ├─ Dtc
 │   ├─ DtcKnowledge
 │   ├─ DiagnosticEvidence
 │   ├─ RepairIntelligence
 │   ├─ RepairProcedure
 │   ├─ RepairPart
 │   ├─ LaborEstimate
 │   ├─ PriceEstimate
 │   ├─ RepairEstimate
 │   ├─ RepairSource
 │   ├─ RepairSourceResolver
 │   └─ RepairEstimateEngine
 │
 ├─ obd/
 │   ├─ ObdPidDefinition
 │   ├─ ObdPidRegistry
 │   ├─ Mode01Decoder
 │   ├─ Mode02Decoder
 │   ├─ Mode03Decoder
 │   ├─ Mode05Decoder
 │   ├─ Mode06Decoder
 │   ├─ Mode07Decoder
 │   ├─ Mode09Decoder
 │   └─ Mode0ADecoder
 │
 ├─ protocol/
 │   ├─ IsoTp
 │   ├─ Uds
 │   ├─ Kwp
 │   └─ NegativeResponse
 │
 ├─ transport/
 │   ├─ WiCanTransport
 │   ├─ Elm327Transport
 │   ├─ SlcanTransport
 │   └─ CommunicationMetrics
 │
 ├─ live/
 │   ├─ LiveDataEngine
 │   ├─ SamplingController
 │   └─ RollingSeriesBuffer
 │
 ├─ capture/
 │   ├─ CanCapture
 │   └─ ReplayEngine
 │
 └─ profiles/
     ├─ VehicleProfile
     ├─ ProfileResolver
     └─ Tesla / VAG / HyundaiKia / ...
```

Exact package paths may differ from this sketch; responsibilities are the important part.

---

# 37. Required tests

At minimum, create automated tests for:

### Transport

- fragmented TCP response;
- prompt handling;
- timeout;
- reconnect;
- malformed response;
- metrics.

### OBD

- Mode 01 known PIDs;
- supported-PID bitmap;
- Mode 03 DTC;
- Mode 06 scaling/min/max/current;
- Mode 09 VIN;
- multi-ECU response handling.

### ISO-TP/UDS

- single-frame;
- first/consecutive frames;
- flow control;
- multi-frame reassembly;
- negative response parsing.

### Vehicle profile

- exact match;
- partial match;
- no match;
- wrong production range;
- wrong ECU software;
- verification state propagation.

### Repair intelligence

- DTC without repair mapping;
- DTC with possible causes but no confirmed part;
- exact part match;
- superseded part;
- labor + parts range;
- source provenance;
- stale/unknown price;
- unsupported procedure;
- exact vs approximate vehicle scope.

### Safety

- unsupported step never becomes PASS;
- replay never transmits by default;
- write operation cannot execute from a read-only diagnostic event;
- HV procedure retains safety gating.

---

# 38. Definition of done for each feature

A feature is not considered complete merely because a decoder exists.

It is complete when:

1. data model exists;
2. protocol layer exists;
3. source/provenance exists;
4. simulator/replay fixture exists;
5. automated tests exist;
6. UI state handles unavailable/error/unknown;
7. verification level is explicit;
8. real-vehicle validation status is documented;
9. documentation/roadmap is updated;
10. safety constraints are enforced.

---

# 39. Existing repository work to preserve

The repository already contains the following important foundations that should be evolved rather than discarded:

- WiCAN/ELM connectivity work;
- `CapabilitySnapshot`;
- `ReplayIndex`;
- `DtcKnowledge`;
- `PrePurchaseReport`;
- `DiagnosticTooltips`;
- `Mode01Decoder`;
- simulator/replay concepts;
- existing ROADMAP / FEATURE_PROPOSALS / REFERENCES documentation;
- existing repair-knowledge architecture document.

Any stale document that claims Mode 01 is completely absent must be corrected to reflect the actual implementation.

---

# 40. Final product principle

The application should behave like a diagnostic platform, not a collection of hard-coded screens:

```text
WiCAN
  ↓
Transport
  ↓
Protocol
  ↓
ECU discovery
  ↓
Vehicle profile
  ↓
Typed measurements / DTCs / tests
  ↓
Evidence + verification
  ↓
Knowledge
  ↓
Repair intelligence
  ↓
Cost + parts + labor
  ↓
Procedure / report / automation
```

The same pipeline must support ICE, hybrid, PHEV and EV vehicles.

**Most important rule:** when the app does not know something, it must say that it does not know it. Verified data should be clearly stronger than inferred data, and exact vehicle scope should always beat generic assumptions.
