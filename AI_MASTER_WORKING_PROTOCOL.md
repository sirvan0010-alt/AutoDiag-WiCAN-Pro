# AI Master Working Protocol

## 1. Source of truth

The GitHub repositories are the durable source of truth. ChatGPT conversations are working sessions, not the canonical project state.

Primary repository:
- `sirvan0010-alt/AutoDiag-WiCAN-Pro`

Diagnostic-data repository:
- `sirvan0010-alt/AutoDiag-WiCAN-Diagnostic-Data`

When the user says `@GitHub pokračuj`, continue from the current GitHub state, committed plans/manifests and latest accepted architecture. Do not restart the project or ask the user to restate the plan unless a genuinely blocking decision is missing.

## 2. What AutoDiag actually is

**AutoDiag is a universal vehicle interface platform, not a read-only diagnostic viewer.**

The platform combines:

- vehicle diagnostics;
- CAN/OBD/ISO-TP/UDS and other vehicle protocols;
- CAN capture, decoding, signal hunting and correlation;
- live vehicle data and dashboards;
- user-facing Android application functionality;
- communication with vehicle interfaces over Wi-Fi, Bluetooth, USB and other supported transports;
- vehicle-state/history/event storage;
- user-authorized vehicle commands and control functions where the vehicle, protocol and safety model permit them;
- vehicle-specific adapters/profiles on top of a universal core;
- future extensibility for additional transports, protocols, manufacturers and features.

Do **not** reduce AutoDiag to passive CAN sniffing or read-only diagnostics.

## 3. Universal architecture

Core data/diagnostic pipeline:

`capture -> decode -> hunt -> correlate -> investigate`

Protocol and transport layers:

`transport -> CAN/SLCAN/OBD -> ISO-TP -> UDS/manufacturer protocol -> decoder -> observation/state`

The resulting vehicle observations can feed several consumers:

`observation/state -> diagnostics`

`observation/state -> user application/dashboard`

`observation/state -> history/evidence`

`observation/state -> authorized control decisions`

Control must be a separate command path rather than an accidental side effect of decoding:

`user intent -> capability/policy check -> vehicle command -> transport -> response/result -> audit/history`

This separation allows AutoDiag to support both observation and control without mixing a decoded signal with an executable command.

## 4. External repository rule

External projects are **reference architecture, evidence and implementation sources**, not automatic runtime dependencies.

A useful feature is not rejected merely because it is active control, cloud communication, BLE, Wi-Fi, UI, firmware or user functionality. Instead classify it as:

- `KEEP` — directly useful to the universal AutoDiag platform;
- `ADAPT` — useful concept/code that must be redesigned for the universal architecture;
- `VEHICLE_SPECIFIC` — useful for a manufacturer/model/profile module;
- `CONTROL` — active vehicle command/control capability that belongs in the command layer and requires capability/safety gates;
- `REFERENCE` — useful evidence or architectural inspiration but not yet suitable for implementation;
- `REJECTED` — not appropriate for AutoDiag or outside the current project scope.

Never confuse `CONTROL` with `REJECTED`.

## 5. Safety and control policy

AutoDiag may contain vehicle-control functionality, but control is not enabled merely because an external repository demonstrates it.

Control implementations require:

- explicit vehicle/protocol capability identification;
- command/result separation;
- validation of target vehicle and ECU/function;
- clear user intent;
- safe failure and timeout handling;
- audit/history of issued commands and results;
- evidence/provenance for vehicle-specific behavior;
- additional safeguards for safety-critical functions.

Do not blindly import:
- autonomous-driving control logic;
- unverified safety-critical actuator control;
- ECU flashing/programming;
- seed/key or immobilizer bypass;
- exploit chains, persistence or authentication bypass;
- battery contactor/balancing actuation without an explicit, separately designed control/safety layer;
- payment handling or automated purchasing;
- bot-detection bypass.

These are restrictions on implementation, not a statement that AutoDiag can never have control features.

## 6. Evidence and verification

Verification states:

- `EXTERNAL_REFERENCE` — found in an external repository; not independently verified.
- `CROSS_CORRELATED` — supported by an independent second source or compatible capture.
- `VEHICLE_VERIFIED` — confirmed using actual vehicle evidence/reproducible vehicle test.

Never promote a static GitHub signal directly to `VEHICLE_VERIFIED`.

Preserve conflicting definitions side-by-side until resolved. Model Y data must not automatically be treated as Model 3 data and vice versa.

Required provenance:
- source repository;
- source path;
- source revision/commit SHA;
- extraction timestamp;
- artifact type;
- vehicle scope;
- confidence;
- verification state;
- target AutoDiag module;
- conflicts;
- notes.

## 7. Android/Kotlin rule

Android/Kotlin is the implementation target for the application.

Python/C/C++/Go/Qt and similar external implementations should be translated conceptually into Kotlin/Android-native architecture where appropriate. Do not mechanically port platform-specific assumptions.

The Android application may contain:
- diagnostics;
- live data;
- dashboard/UI;
- transport configuration;
- vehicle profiles;
- history/evidence;
- user-authorized control.

## 8. Work loop

For each source:

1. Inspect current repository state and revision.
2. Inspect concrete source/data/configuration files, not only README claims.
3. Identify useful architecture, data, protocol, UI, transport or control concepts.
4. Classify each useful item (`KEEP`, `ADAPT`, `VEHICLE_SPECIFIC`, `CONTROL`, `REFERENCE`, `REJECTED`).
5. Preserve provenance.
6. Cross-correlate independent sources.
7. Assign verification state.
8. Add tests where behavior is implemented.
9. Commit durable artifacts/code/manifests.
10. Continue to the next track without losing the accumulated state.

## 9. Durable memory rule

Every meaningful discovery ends in one of:

1. source/data manifest;
2. evidence record;
3. implementation commit;
4. explicit reference/rejected decision.

GitHub is the durable memory of the project.

## 10. Current extraction strategy

Tesla repositories are evaluated for the **whole AutoDiag platform**, not only for CAN decoding. Therefore inspect for:

- CAN/OBD/UDS/ISO-TP;
- Wi-Fi/BLE/USB transport;
- Android/mobile UI;
- vehicle-state models;
- command/control abstractions;
- cloud/local API separation;
- DBC/signal catalogs;
- telemetry/history;
- vehicle identity and model-year selection;
- firmware/hardware identification;
- feature-specific vehicle functions;
- evidence/provenance;
- testing and virtual-vehicle/bench infrastructure;
- security boundaries and defensive validation.

See `AI_TESLA_REPOSITORY_EXTRACTION_PLAN.md` for the maintained repository matrix and extraction order.
