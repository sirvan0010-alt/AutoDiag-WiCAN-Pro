# Reference APK feature inventory

The supplied Android APKs are used as behavioral and data-model references. We do not copy their UI design or proprietary implementation.

## Torque reference

Observed data/profile concepts:

- Long name + short display name
- Mode/PID or ECU-specific request identifier
- ECU/CAN header / target address
- Formula/equation for byte decoding
- Minimum and maximum display/engineering range
- Units
- Boolean/bit-field decoding
- Derived values referencing other values
- Vehicle-specific profiles matched by ECU/software/workshop identifiers
- Standard OBD-II equivalent PID linking
- Custom PIDs
- Logging and sampling-rate concepts
- Adapter read-speed information
- Multiple ECU families and non-OBD diagnostic requests

The important architectural lesson is that acquisition address, byte extraction, scaling, engineering range, identity and derived calculations should be separate metadata, not embedded in the UI.

## PHEV Watchdog reference

Observed EV/PHEV telemetry concepts:

- SOC / SOH
- pack voltage/current/power
- cell minimum/maximum voltage and cell identifiers
- per-cell voltage history
- module temperature minimum/maximum and identifiers
- average/target cell voltage
- charge/discharge power limits
- battery cooling/fan state
- resistance and cell-difference metrics
- sampling and historical logging
- DTC history/database concepts
- multiple chart modes

These are candidates for our EV telemetry model, history store and graph engine. Vehicle-specific meanings must still carry provenance and verification scope.

## Car Scanner reference

Useful product-scope references include broad vehicle profiles, ECU diagnostics, live data, custom/extended data and logging. The project should reproduce capabilities where protocol evidence permits, but use its own modern UI and evidence model.

## Tessie 16.0.29 reference

Static extraction of the supplied Tessie XAPK established a distinct Tesla Fleet/Direct Telemetry data path rather than a conventional CAN/OBD PID decoder:

- `streaming.tessie.com/{VIN}` WebSocket telemetry endpoint observed
- Fleet telemetry configuration/status/cache endpoints observed
- `vehicle_state.*`, `charge_state.*`, `drive_state.*`, `climate_state.*`, `gui_settings.*` and `vehicle_config.*` field families observed
- Explicit energy/powertrain identifiers observed for pack voltage/current, module temperatures, lifetime energy, AC/DC charging energy, motor voltage/current/torque, inverter state/temperature and isolation resistance
- Battery-health identifiers include `batteryHealth`, `health_percent`, `original_capacity` and `new_battery_capacity_`; this is app-derived candidate evidence, not vehicle-verified SOH
- Raw vehicle-data export as zipped CSV is exposed by the app
- A broad command surface is present, including charging/climate/locking and a `command/flash` identifier; command existence is recorded only and no write/flash mechanism is promoted
- An OBD profiler feature exists in the UI/API surface, but no OBD byte decoder contract was established by this static extraction

### Tessie automation evidence

The supplied Tessie 16.0.29 static extraction contains a first-class automation model and screen (`package:tessie/models/automation.dart`, `package:tessie/screens/home/automation_screen.dart`, `/automation`) with an explicit If-This/Then-That vocabulary. The extraction also exposes `AutomationType`, `AutomationAction`, `createAutomation`, `addOrUpdateAutomation`, `removeAutomation`, `automations`, `editAutomation` and `automation_timezone`.

Observed support/capability gates include `supportsAutomation`, `_doesVehicleSupportAutomation`, `_isAutomationActionSupported` and `canAutomationUseOnDeparture`. These identifiers are strong evidence that automation capability and action support are evaluated separately; exact boolean logic is not reconstructed from static strings alone.

Observed trigger identifiers are broader than the initial pass:

- battery: `belowBatterylevelthreshold`, `whenAtOrBelowTheSetLevel`
- driving: `whenDrivingBegins`, `whenDrivingEnds`
- charging: `whenChargingBegins`, `whenChargingEnds`, `whenCharging`, `whenPluggedIn`, `whenChargersArentProvidingPower`
- location: `whenEnteringALocation`, `whenLeavingALocation`, `whenUnpluggedAtALocation`
- speed/distance: `whenTheSetSpeedIsExceeded`, `whenTheSetOdometerIsExceeded`
- tires/security: `whenAnyTireIsBelowTheSetPressure`, `whenDoorsAreLeftUnlocked`
- Sentry: `whenMovementIsDetectedBySentry`, `whenAnAlarmIsTriggeredBySentry`
- weather/body: precipitation while a window/door/trunk is open
- climate/Dog Mode: Dog Mode or Climate Keep related trigger text

Observed action vocabulary includes climate start/stop, Sentry enable/disable, lock/unlock, charging start/stop, charge-limit and charging-amp changes, scheduled charging, seat heat/cool, steering-wheel heat, climate-keeper mode, charge-port operations, rear-trunk operations, window vent/close, lights flash, HomeLink and software-update schedule/cancel identifiers. Presence in the binary proves vocabulary/integration, not that every action is valid for every trigger, vehicle or subscription tier.

The extraction contains `/activity/commands?source=automation`. This is strong evidence that automation-originated command execution is represented in command activity with an automation source marker. It does not establish the exact create/update/delete backend JSON schema, trigger polling/evaluation interval, retry policy or action ordering semantics.

Timezone is a first-class automation concern: `automation_timezone` plus strings describing configuration in the current timezone and behavior when the timezone changes. AutoDiag should therefore persist timezone semantics with the automation rather than assuming device-local time at execution.

The extraction also contains sensitive-action confirmation language and transport text indicating actions may be sent over Bluetooth, Wi-Fi and cellular. This is reference evidence only; it does not authorize AutoDiag to reproduce proprietary transport behavior.

Reference architecture for AutoDiag automation is therefore:

`Trigger -> Conditions -> Vehicle capability gate -> Authorization gate -> Action -> Execution audit`

A concrete candidate remains:

`battery below threshold -> parked + Sentry enabled + not charging -> disable Sentry`

The exact battery threshold comparison/storage semantics and runtime wake/retry behavior remain unverified and must not be hard-coded from static strings.

For Tesla Fleet API control, use an authorized Vehicle Command path with the required virtual-key/signing boundary. Tesla documents `signed_command` as the generic command endpoint and states that unsigned commands are rejected; direct legacy `/command` use is deprecated for vehicles requiring the Tesla Vehicle Command Protocol. Undocumented raw CAN/UDS writes or authentication bypass are not implied by the Tessie extraction.

Tessie evidence is therefore useful for the Tesla telemetry/canonical-data model, capability discovery and automation architecture, but it must not be converted into CAN IDs, ECU bindings, PID byte offsets, scaling or vehicle verification without independent evidence.

Provenance: `AutoDiag-WiCAN-Diagnostic-Data/provenance/apk-extraction/tessie-16.0.29/analysis.json`; automation provenance: `AutoDiag-WiCAN-Diagnostic-Data/provenance/apk-extraction/tessie-16.0.29/automation-trigger-action-static-analysis.json` and `automation-type-action-deep-analysis.json`; candidate: `data/candidates/tessie_16_0_29_fleet_telemetry.json`.

## Remote-control reference

The supplied remote-control application is relevant to the future control/service architecture only. It must not cause the read-only diagnostic transport to become write-capable implicitly.

## Implementation policy

1. Reuse protocol facts and data-model ideas where they are standard or independently verifiable.
2. Do not copy proprietary source code or application UI.
3. Do not blindly ship third-party APK asset databases into the public repository.
4. Prefer an importer/schema so compatible profile data can be loaded as user/project data.
5. Every vendor-specific signal must carry vehicle/ECU scope, raw representation and verification status.
6. Unknown scaling stays raw/unknown instead of being guessed.
7. Read-only diagnostics remains the priority path.
8. Experimental control features live under `docs/experimental/` and isolated control APIs.
9. Automation must remain a separate trigger/condition/action layer; a reference APK's automation vocabulary does not itself authorize vehicle writes.
10. Automation capability, authorization and execution must be independently gated and auditable.
