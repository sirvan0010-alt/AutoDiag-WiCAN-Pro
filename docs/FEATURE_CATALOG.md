# AutoDiag Feature Catalog

This document is the master functional scope. A feature is not considered implemented until code, tests, vehicle evidence and documentation exist where applicable.

## Connectivity

- WiCAN PRO mDNS discovery
- Manual IP connection
- TCP ELM327 :3333
- TCP SLCAN/raw CAN :23
- reconnect handling
- connection diagnostics
- raw TX/RX logging
- simulator/mock transport

## Generic diagnostics

- OBD-II PIDs
- DTC reading/clearing only where explicitly supported and safe
- live data
- freeze-frame where exposed
- UDS
- KWP2000
- K-Line transport where hardware permits
- CAN frame capture/filter/export/replay

## Vehicle identification

- make/model/year
- trim
- drive unit
- battery variant
- battery supplier
- chemistry
- pack topology
- BMS generation
- firmware/diagnostic generation where verifiable
- market/region
- confidence and provenance for every identification field
- prominent US-market Tesla warning when confirmed

## Tesla READ diagnostics

- battery telemetry
- pack voltage/current/power
- SOC
- temperatures
- per-module data
- per-cell/brick data when exposed
- min/max/spread
- load response
- recovery response
- thermal behavior
- charging telemetry
- AC charging
- DC fast charging
- charging curve
- HV isolation/Riso
- contactor/HV status where safely exposed
- drive-unit/inverter telemetry where verified
- diagnostic codes and alerts

## Automated EV Health Test

- Quick / Standard / Full modes
- pre-purchase test
- parked/idle baseline
- controlled load observation
- acceleration/load analysis
- recovery analysis
- AC charging analysis
- DC charging analysis
- cell/module tracking during charging
- thermal analysis
- Riso analysis
- DTC analysis
- data-quality checks
- evidence-backed thresholds
- STATIC / LOAD / RECOVERY / TREND / CONFIDENCE engine
- Battery Fingerprint
- vehicle self-history
- transparent confidence
- human-readable report
- expert numerical report
- replay
- export

## Battery visualization

Simple:
- health-oriented status cards
- short explanations
- contextual warnings
- `?` tooltips

Expert:
- pack overview
- module grid
- cell grid
- cell voltage map
- cell deviation map
- synchronized current/voltage/power graphs
- temperature map
- charge/load/recovery curves
- time scrubber and replay
- exact numerical values

The visualization must never color a value as good/bad when no evidence-backed classification exists.

## Diagnostic Knowledge Base

For each verified code/alert:
- meaning
- affected system
- observed conditions
- possible causes from sources
- recommended checks
- OEM explanation
- OEM troubleshooting
- OEM repair/service procedure
- source URL
- verification state
- vehicle scope
- last reviewed

OEM and community sources are always separated.

## Remote monitoring and automation

- Wi-Fi telemetry
- background monitoring
- historical storage
- MQTT
- Home Assistant
- notifications
- user rules
- dashboards/widgets
- automation triggers

The automation engine may act only on explicitly supported and documented read signals.

## Custom vehicle actions

Future, isolated from READ diagnostics:
- capability matrix
- explicit confirmation
- verified actions only
- experimental write sandbox
- audit log

No guessed CAN writes.

## Multi-brand expansion

Initial targets:
- Tesla
- VAG / VW / Audi / Škoda / SEAT
- Hyundai / Kia
- BMW
- Mercedes-Benz
- Renault / Dacia
- Nissan
- Mitsubishi

Architecture remains manufacturer/profile based so a missing decoder results in graceful degradation rather than fabricated data.

## Long-term Android compatibility

- modern Kotlin/Compose architecture
- current target SDK maintenance
- dependency updates
- automated build/test/lint
- compatibility testing
- no unnecessary platform-specific assumptions
- transport/core logic separated from UI
