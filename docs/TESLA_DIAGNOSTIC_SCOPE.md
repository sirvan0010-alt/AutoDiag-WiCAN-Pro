# Tesla diagnostic scope

The deferred Tesla diagnostic area is now represented as an explicit capability catalog in `core/capability/TeslaDiagnosticCapabilities.kt`.

## Included domains

- powertrain / drive control
- brake electronics (ABS/ESP)
- body control (BCM)
- airbag / restraint controller
- electronic parking brake (EPB)
- instrument cluster
- parking assistance
- door electronics
- steering / electric power steering
- infotainment
- battery management system (BMS)

## Functional scope

The catalog covers the requested diagnostic functions at the capability level:

- DTC readout
- live/measured data
- actuator/output tests
- service procedures
- calibration/adaptation
- configuration where supported
- battery cell, temperature, SOC/SOH and power/charging diagnostics

The catalog intentionally does **not** contain guessed Tesla CAN IDs, undocumented payloads, security access sequences, or write commands. Those must be supplied by a vehicle-specific definition and verified against the discovered ECU/protocol before execution.

Safety-sensitive operations remain explicitly classified as service/configuration/control operations. Presence in the catalog is therefore not permission to execute them.

## CAN signal extraction status

The current Tesla Model 3/Y CAN evidence is maintained in the private diagnostic-data repository, not in the runtime implementation:

`provenance/apk-extraction/tesla/tesla-model3y-can-signal-source-matrix-2026-09-06.json`

Status: **`CANDIDATE_ONLY` / `NO_PROMOTION`**. The matrix explicitly separates three transport branches:

1. **Third-party CAN on SAE J1962** — first read-only feasibility path for applicable Model 3/Y configurations.
2. **DoIP/Ethernet** — separate diagnostic branch; applicability and WiCAN PRO support must be established independently.
3. **Tesla BLE vehicle-command** — fallback branch; it is not assumed necessary for Model 3/Y read-only CAN data.

Tesla's R5 service document confirms a third-party CAN interface on the OBD-II connector for specified Model 3/Y production ranges and regions. Older configurations can also have a separate DoIP diagnostic port, so physical port and vehicle configuration are part of the applicability gate.

The current candidate signal queue is:

| Signal/domain | Current status | Rule |
|---|---|---|
| SOC | `CANDIDATE / UNVERIFIED` | CAN message/field mapping requires vehicle capture |
| HV_V | `CANDIDATE / UNVERIFIED` | no production decoder without byte/scale evidence |
| HV_A | `CANDIDATE / UNVERIFIED` | no production decoder without byte/scale evidence |
| BATT_TEMP | `CANDIDATE / UNVERIFIED` | thermal field and scaling require capture |
| charging | `CANDIDATE / UNVERIFIED` | voltage/current/power/status fields require capture |
| drive | `CANDIDATE / UNVERIFIED` | drive/status/speed mapping requires capture |
| SOH | `UNKNOWN / NO_CAN_MAPPING_ESTABLISHED` | do not derive SOH from other battery values |

The matrix requires model, production date, region, diagnostic-port configuration, raw capture, message ID, bit/byte layout, scale/offset/unit and repeatability before promotion. Its safety scope is read-only evidence/feasibility; writes, security access, key/immobilizer operations and coding/flashing remain unimplemented.

WiCAN PRO's documented CAN/OBD capabilities make the J1962 CAN branch technically plausible, but that hardware capability does not by itself verify any Tesla signal mapping.

## Data storage rule

The canonical project-wide storage rules are in `docs/DATA_STORAGE_MAP.md`. Evidence from APK extraction belongs in the private provenance layer; candidate data stays candidate until the promotion gate passes; only promoted data may become runtime input.

## Integration path

```text
Tesla vehicle discovery
        -> ECU identity/topology
        -> protocol + vehicle-specific definition
        -> capability discovery
        -> DiagnosticEvidence
        -> live data / DTC / service operation
        -> capture + replay + verification
```

This keeps the transport layer independent of Tesla business meaning and allows the same diagnostic engine to serve ICE, hybrid and EV vehicles.
