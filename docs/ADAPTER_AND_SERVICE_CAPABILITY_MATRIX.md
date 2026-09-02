# WiCAN PRO Adapter & Diagnostic Service Capability Matrix

**Date:** 2026-09-02  
**Purpose:** Record which functions from the proposed professional-diagnostic feature list are technically possible with WiCAN PRO, which require vehicle/OEM support, and which require additional hardware/software that WiCAN PRO alone does not provide.

> This document is a capability/architecture specification, not a claim that every function works on every vehicle. A function is exposed only after the vehicle, ECU, protocol, firmware, security state and required data path are verified.

## 1. Executive conclusion

WiCAN PRO is a capable automotive interface, but it is **not by itself a complete clone of a professional OEM diagnostic tester**.

The official/public WiCAN PRO material confirms broad support for classic automotive interfaces including ISO 15765-4 CAN, SAE J1939, ISO 11898 raw CAN, Medium-Speed CAN, GM high-speed CAN, Single Wire CAN/GMLAN, SAE J1850 PWM/VPW, ISO 9141-2 and ISO 14230-4. It also exposes ELM327/ELM329/STN/VT-style command interfaces, Wi-Fi/BLE, SLCAN/SocketCAN and raw CAN capabilities. citeturn0search11

Therefore the adapter is a strong foundation for:

- CAN bus observation and capture;
- generic OBD-II communication;
- DTC reading where the vehicle exposes the service;
- live data where the ECU exposes the PIDs/signals;
- VIN and identification data where exposed;
- freeze-frame data where exposed;
- manufacturer-specific diagnostics through profiles;
- ISO-TP/UDS read-only work implemented in AutoDiag;
- many service functions **in principle**, when the vehicle ECU, protocol, security and required routine support them.

However, some items in the supplied professional-tool list are **not properties of the adapter itself**. Coding, adaptations, actuator tests, DPF regeneration, EGR learning, immobilizer work, programming, odometer writing, ADAS calibration and similar functions are vehicle-specific diagnostic operations. They require the correct ECU procedure, addressing, session, security access, routine/data identifiers, preconditions and often OEM/licensed data.

The architecture must therefore model capabilities as:

```text
Adapter capability
        +
Transport/protocol capability
        +
Vehicle/ECU capability
        +
Security/session capability
        +
Verified procedure/data
        +
Required hardware/licence
        ↓
Actual available diagnostic function
```

## 2. What is confirmed about WiCAN PRO

Current public WiCAN PRO information confirms the following interface families:

| Capability | WiCAN PRO status | AutoDiag interpretation |
|---|---|---|
| ISO 15765-4 CAN | **Supported** | Core CAN/OBD transport |
| SAE J1939 CAN | **Supported** | Commercial/heavy-vehicle path where applicable |
| ISO 11898 raw CAN | **Supported** | Raw CAN/sniff/capture foundation |
| Medium-Speed CAN | **Supported** | Additional vehicle bus access |
| GM High-Speed CAN | **Supported** | Vehicle-specific network access |
| Single Wire CAN / GMLAN | **Supported** | Vehicle-specific network access |
| SAE J1850 PWM | **Supported** | Legacy vehicle support |
| SAE J1850 VPW | **Supported** | Legacy vehicle support |
| ISO 9141-2 | **Supported** | K-Line family foundation |
| ISO 14230-4 slow/fast | **Supported** | KWP2000 foundation |
| ELM327 v2.3 instruction set | **Supported** | Generic diagnostic-app compatibility |
| ELM329 v2.2 | **Supported** | Extended ELM interface |
| STN instruction set | **Supported** | Alternative command interface |
| VT instruction set | **Supported** | Alternative command interface |
| Wi-Fi | **Supported** | Android transport |
| BLE | **Supported** | Alternative adapter transport |
| SLCAN / SocketCAN | **Supported** | Raw CAN tooling |
| Realdash CAN protocol | **Supported** | Telemetry/dashboard integration |
| microSD logging | **Supported** | Local capture/logging foundation |

These capabilities are documented for WiCAN PRO by the manufacturer/hardware documentation. citeturn0search11

## 3. CAN 2.0 vs CAN FD — important correction

The supplied feature text describes CAN 2.0 as a major WiCAN PRO advantage. We will retain CAN 2.0 support, but the project must **not claim CAN FD support from the ESP32-S3 controller itself**.

Espressif documents that ESP32-S3 contains a TWAI controller compatible with ISO 11898-1 / classic CAN 2.0, supports 11-bit and 29-bit identifiers, and does **not** natively support CAN-FD frames. CAN-FD therefore requires additional CAN-FD-capable hardware/controller and an appropriate firmware/data path. citeturn0search29turn0search7

Project rule:

- `CAN_2_0_CLASSIC`: supported foundation;
- `CAN_FD`: **not claimed for the standard ESP32-S3 TWAI path**;
- CAN-FD support may be added later only if the actual WiCAN hardware revision/firmware exposes a verified CAN-FD controller/path.

## 4. Requested diagnostic functions

### 4.1 Read-only functions

These are directly compatible with the project's READ-first architecture and are realistic targets:

- **CAN bus error/health observation** — supported where the interface/driver exposes the relevant counters/state; raw CAN traffic can be captured and analyzed.
- **DTC reading** — YES, where ECU/protocol exposes DTC services.
- **Live data** — YES, where standard or vehicle-specific PIDs/signals are available.
- **Graphical live data** — YES; rendering is an Android application function, not an adapter limitation.
- **VIN reading** — YES, where the vehicle exposes VIN through standardized or OEM service.
- **ECU identification / software / hardware version reading** — YES, where the ECU exposes identification data.
- **Freeze-frame** — YES, where standardized Mode 02 or an OEM-specific equivalent is exposed.
- **Readiness/monitor data** — YES where OBD/ECU exposes it.
- **DPF/SCR/AdBlue/NOx diagnostic values** — YES where the ECU exposes the relevant PIDs/DIDs/diagnostic services.
- **EGR operating/diagnostic data** — YES where exposed; Mode 06 and/or OEM data can be used depending on ECU.
- **Battery/HV diagnostics** — YES where the vehicle makes the required data accessible and the interface reaches the relevant bus/ECU.

The existing WiCAN documentation also confirms automatic protocol detection for WiCAN PRO in common OBD-II use, while standard WiCAN requires manual protocol selection. citeturn0search1turn0search2

### 4.2 Service functions — possible, but NOT adapter-only

The following are added to the product target because the user requested them:

- component adaptation;
- ECU adaptation;
- component learning / relearning;
- forced component activation;
- EGR self-learning;
- DPF forced regeneration;
- DPF adaptation/reset;
- GPF regeneration;
- SCR/AdBlue reset/adaptation;
- NOx sensor reset/adaptation;
- A/F adaptation;
- BMS adaptation after battery replacement;
- brake-pad service reset;
- ABS bleeding routines;
- clutch adaptation;
- gearbox/gear adaptation;
- coolant bleeding routines;
- throttle/ETS adaptation;
- SAS calibration;
- suspension calibration;
- headlamp/AFS calibration;
- rain/light sensor adaptation;
- turbocharging calibration;
- TPMS programming/reset;
- injector coding;
- gateway calibration;
- transport mode;
- oil/service reset;
- start/stop settings where the ECU legitimately provides the function;
- seat/window/sunroof initialization where supported;
- climate/AC calibration where supported;
- engine power-balance tests where supported.

**Implementation rule:** each of these is a `VehicleServiceFunction`, not a generic adapter command. The function becomes available only after capability discovery and a verified vehicle-specific procedure are matched.

## 5. High-risk service functions

The following require a separate safety/security layer and must not be enabled by the normal READ profile:

- ECU coding;
- programming/parameterization;
- immobilizer functions;
- advanced immobilizer programming;
- module replacement/configuration;
- actuator/forced-operation routines with safety consequences;
- airbag crash-data/reset operations;
- odometer write/copy/alteration;
- firmware flashing;
- security-access operations;
- routines affecting HV systems, brakes, steering or other safety-critical systems.

These require, depending on vehicle/OEM:

- diagnostic session control;
- SecurityAccess or equivalent authorization;
- vehicle-specific keys/tokens/certificates;
- OEM programming data;
- stable power supply;
- exact preconditions;
- additional interface/hardware;
- legally/contractually permitted software/data.

The app must show the required prerequisites instead of pretending that WiCAN PRO alone makes the operation possible.

## 6. Maintenance Function / "41 quick service functions"

The supplied list of 41 Maintenance Functions is incorporated as a **product target list**, not as a claim that WiCAN PRO universally implements the same menu.

Target functions:

1. AC CALIBRATION
2. ADBLUE RESET
3. AFS CALIBRATION
4. AIRBAG RESET
5. A/F ADAPTATION
6. BMS ADAPTATION
7. BRAKE PADS INDICATOR
8. ABS BLEEDING
9. CLUTCH ADAPTATION
10. COOLANT BLEEDING
11. ECU CODING
12. DPF REGENERATION
13. EGR SELF-LEARNING
14. ENGINE POWER BALANCE
15. ETS ADAPTATION
16. FRM RESET (BMW/MINI)
17. GATEWAY CALIBRATION
18. GPF REGENERATION
19. GEAR ADAPTATION
20. GEARBOX ADAPTATION
21. HIGH VOLTAGE BATTERY TEST
22. ICCS CALIBRATION
23. IMMO
24. IMMO PROG
25. INJECTOR CODE
26. LANGUAGE
27. MOTOR ANGLE CALIBRATION
28. NOx SENSOR RESET
29. ODOMETER
30. OIL RESET
31. RAIN/LIGHT SENSOR ADAPTATION
32. START&STOP SETTINGS
33. SUSPENSION CALIBRATION
34. SEATS CALIBRATION
35. SUNROOF INITIALIZATION
36. SAS CALIBRATION
37. TRANSPORT MODE
38. TURBOCHARGING CALIBRATION
39. TIRE MODIFICATION
40. TPMS RESET
41. WINDOWS CALIBRATION

### Required UI behavior

The application must not show all 41 functions as universally executable.

Instead:

```text
Vehicle detected
      ↓
ECU scan
      ↓
Capability discovery
      ↓
Vehicle + ECU + service match
      ↓
Maintenance Functions
      ├─ AVAILABLE
      ├─ AVAILABLE WITH PREREQUISITES
      ├─ REQUIRES OEM/SECURITY ACCESS
      ├─ REQUIRES ADDITIONAL HARDWARE
      ├─ NOT SUPPORTED BY THIS ECU
      └─ UNKNOWN / NEEDS VERIFICATION
```

## 7. DoIP

**DoIP is not the same thing as CAN.** It is an IP/Ethernet diagnostic transport.

The supplied text correctly identifies DoIP as an IP-based automotive diagnostic technology, but the project must not claim that an ordinary WiCAN PRO CAN connection automatically provides DoIP.

For AutoDiag:

- DoIP is a future transport target;
- actual DoIP requires a verified Ethernet/DoIP-capable hardware and firmware path;
- Wi-Fi from Android to WiCAN is only the adapter-side network link and does not itself turn CAN into DoIP;
- DoIP programming/diagnostics require the vehicle's Ethernet gateway and diagnostic behavior.

## 8. J2534

**J2534 is not a vehicle bus protocol.** It is a Pass-Thru interface/API standard used by diagnostic/programming software to communicate through an interface.

Therefore:

- WiCAN PRO protocol support does not automatically equal J2534 support;
- a J2534-compatible driver/API implementation would be a separate integration;
- OEM software compatibility would still depend on the OEM software, vehicle, interface capabilities and licensing;
- AutoDiag must not claim "dealer-equivalent programming" merely because J2534 appears in a specification list.

A future `PassThruAdapter` abstraction may be added if the hardware/firmware and host platform provide a verified implementation.

## 9. ISO / SAE protocol list from the supplied material

The supplied list is retained as a compatibility target, but each item must be verified separately against the actual WiCAN PRO hardware/firmware revision:

- ISO 15765 / CAN — **supported foundation**
- ISO 11898 classic CAN — **supported raw CAN foundation**
- SAE J1939 — **supported**
- SAE J2411 Single Wire CAN/GMLAN — **supported**
- SAE J1850 PWM — **supported**
- SAE J1850 VPW — **supported**
- ISO 9141 — **supported**
- ISO 14230/KWP — **supported**
- CAN MS-CAN — **supported**
- GM high-speed CAN — **supported**
- J1708 — **not yet verified for WiCAN PRO; do not claim**
- Chrysler SCI — **not yet verified; do not claim**
- Honda Diag-H — **not yet verified; do not claim**
- GM ALDL — **not yet verified; do not claim**
- Ford UBP — **not yet verified; do not claim**
- Nissan DDL UART — **not yet verified; do not claim**
- BMW DS1 / DS2 — **not yet verified; do not claim**
- KWP1281 — **not yet verified as a WiCAN PRO hardware capability; investigate separately**
- SAE J2819 TP2.0 — **not yet verified; do not claim**
- UART Echo Byte — **not yet verified; do not claim**

The distinction is deliberate: the product list supplied by the user is broader than the currently verified WiCAN PRO evidence. We keep the requested items in the backlog without turning them into false compatibility claims.

## 10. OEM diagnostic software names

The supplied list includes:

- ODIS (VW Group)
- ISTA (BMW)
- XENTRY (Mercedes-Benz)
- DiagBox (PSA/Stellantis)
- wiTECH (FCA/Stellantis)
- IDS / FJDS / FDRS (Ford)

These are **OEM/professional software ecosystems**, not protocols supported by the adapter.

AutoDiag may use the same diagnostic concepts and may eventually integrate legitimate interfaces/data sources, but it must not claim to be ODIS/ISTA/XENTRY/etc. or to reproduce their proprietary functionality without the necessary licensed interfaces, software, security and data.

## 11. Intelligent VIN and topology map

These requested functions are strong fits for AutoDiag:

### Intelligent VIN

- read VIN using the best available standardized/OEM method;
- cross-check VIN from multiple reachable ECUs;
- flag discrepancies;
- infer vehicle identity only from evidence;
- bind capabilities to VIN + vehicle scope + ECU/software where possible.

### ECU topology

Build a topology tree from discovered evidence:

```text
Vehicle
├── Gateway
├── Powertrain
│   ├── Engine / Inverter
│   └── Transmission / Drive Unit
├── ABS/ESC
├── SRS
├── BCM/Body
├── HVAC
├── BMS
├── Charging
├── Steering
└── ADAS
```

Each node has a state:

- `NOT_TESTED`
- `DISCOVERED`
- `RESPONDING`
- `NO_RESPONSE`
- `DTC_PRESENT`
- `COMMUNICATION_ERROR`
- `UNSUPPORTED`
- `UNKNOWN`

The topology is evidence-based. It must not invent an ECU merely because a vehicle normally has one.

### Prioritization

The UI may calculate a diagnostic priority based on:

1. active/safety-critical DTCs;
2. communication failures;
3. gateway/network faults;
4. power/ground-related observations;
5. multiple dependent ECU failures;
6. confirmed component faults;
7. lower-priority informational issues.

This is analysis, not a claim that the ECU with the most DTCs is necessarily the root cause.

## 12. Remote telemetry / cloud logging

The supplied FCD CLOUD concept is added to the product backlog as a **generic remote telemetry architecture**, not as a claim that WiCAN PRO already provides an integrated FCD CLOUD service.

Target architecture:

```text
Vehicle
  ↓
WiCAN PRO
  ↓
Wi-Fi / optional LTE-GPS peripheral / MQTT
  ↓
AutoDiag telemetry service
  ↓
Historical storage
  ↓
Web/mobile analysis
```

WiCAN PRO documentation indicates support for optional LTE/GPS peripherals and online telemetry use cases, while MQTT can transport CAN/OBD data. citeturn0search11turn0search9

The target is to support:

- automatic trip logging;
- approximately hundreds of live parameters where the vehicle/profile exposes them;
- configurable sampling rate;
- local buffering when offline;
- upload when connectivity returns;
- VIN/vehicle association;
- remote health dashboard;
- geolocation only with explicit user consent;
- privacy controls;
- rate limits and storage limits.

The phrase "up to ~190 live parameters" must remain a **target/example from the supplied concept**, not a universal WiCAN PRO guarantee.

## 13. CAN bus reading and "CAN errors"

The app should distinguish at least three different things:

1. **CAN traffic observed** — frames received by the interface.
2. **Interface/controller errors** — error counters, bus state, bus-off/error-passive where exposed.
3. **Vehicle diagnostic network errors** — DTCs reported by ECUs about communication with other modules.

These must not be merged into one generic "CAN error" number.

ESP32-S3 TWAI provides error counters, error-code capture and arbitration-lost capture at the controller level, making a useful bus-health layer possible on the classic CAN path. citeturn0search29

## 14. Implementation model for every service function

Every maintenance/service action must be represented approximately as:

```text
VehicleServiceFunction
  id
  title
  category
  vehicle_scope
  ecu_scope
  protocol
  addressing
  required_session
  security_requirement
  prerequisites[]
  required_tools[]
  required_parts[]
  routine_or_data_identifier
  expected_response
  safety_level
  source
  verification
  execution_mode
```

Execution modes:

- `READ_ONLY`
- `SIMULATOR_ONLY`
- `USER_CONFIRMATION_REQUIRED`
- `QUALIFIED_SERVICE_REQUIRED`
- `OEM_AUTH_REQUIRED`
- `ADDITIONAL_HARDWARE_REQUIRED`
- `DISABLED`

This is the core mechanism that lets one Android app cover many manufacturers without pretending that every ECU behaves identically.

## 15. Final capability verdict

### With WiCAN PRO alone + AutoDiag software

**Strongly realistic:**

- CAN monitoring/capture;
- generic OBD-II;
- DTC reading;
- live data;
- VIN/identification where exposed;
- freeze frames where exposed;
- many manufacturer-specific read operations;
- ISO-TP;
- UDS read-only services;
- ECU discovery where addressing/topology can be discovered;
- EV/ICE telemetry where the vehicle exposes it;
- graphical recording/replay;
- bus-health analysis;
- remote telemetry through supported network paths.

**Realistic but vehicle/ECU/procedure dependent:**

- adaptations;
- relearns;
- actuator routines;
- DPF regeneration;
- EGR learning;
- SCR/AdBlue service;
- NOx reset;
- BMS adaptation;
- injector coding;
- ABS bleeding;
- clutch/gearbox adaptation;
- SAS/AFS/suspension calibration;
- gateway/module configuration;
- many Maintenance Functions.

**Not guaranteed by WiCAN PRO alone:**

- DoIP;
- CAN-FD;
- J2534 Pass-Thru;
- OEM dealer-software equivalence;
- OEM online parameterization;
- OEM security credentials;
- immobilizer programming;
- firmware flashing;
- odometer writing.

These remain separate integration/verification projects.

## 16. Safety and legal boundary

The project can implement powerful diagnostics, but high-risk functions must be isolated. In particular:

- no arbitrary CAN write commands;
- no automatic actuator activation on an unknown vehicle;
- no immobilizer programming in the normal diagnostic profile;
- no odometer alteration functionality in the normal product scope;
- no unsafe HV procedure without prerequisites;
- no airbag/crash-data manipulation without explicit verified service workflow;
- no flashing without a verified OEM/programming path and power-state checks;
- no claim of dealer equivalence without evidence.

The application should always tell the user whether a function is **readable**, **supported**, **verified**, **available with prerequisites**, **requires external service/data**, or **not supported**.

## 17. Evidence used for this matrix

- WiCAN PRO manufacturer/hardware capability information: public Crowd Supply description and specifications. citeturn0search11
- WiCAN official documentation for Car Scanner and automatic protocol detection. citeturn0search1
- WiCAN official automation documentation for standard OBD-II PIDs and protocol selection. citeturn0search2
- Espressif ESP32-S3 technical documentation for classic CAN/TWAI and CAN-FD limitation. citeturn0search29turn0search7
- WiCAN MQTT documentation for CAN RX/TX and OBD-II over MQTT. citeturn0search9

The project must periodically re-check these claims against the actual WiCAN PRO hardware revision and installed firmware before marking a capability `VERIFIED`.