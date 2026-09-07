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

Doplněk k sekci "Integration path" výše. Sleduje se tady stav statické
extrakce CAN signálů pro Model 3/Y — ne jako duplicitní zdroj pravdy, ale
jako čitelný souhrn toho, co je jinak roztroušené v `provenance/apk-extraction/`
v `AutoDiag-WiCAN-Diagnostic-Data`.

**Canonical source:** `AutoDiag-WiCAN-Diagnostic-Data`,
`provenance/apk-extraction/tesla/tesla-model3y-can-signal-source-matrix-2026-09-06.json`.
The matrix is maintained in the private diagnostic-data repository; this public
section is a readable summary and is not a second source of truth.

### Transportní větve

Tesla nabízí tři různé diagnostické cesty, nikdy nemíchané do jedné vrstvy:

| Transport | Stav | Poznámka |
|---|---|---|
| CAN přes J1962 | první testovaná větev | oficiální Tesla dokumentace potvrzuje 3rd-party CAN na J1962 pro konkrétní Model 3/Y konfigurace a výrobní rozsahy |
| DoIP/Ethernet (UDS) | zatím ne | Tesla dokumentuje přechod na DoIP u novějších konfigurací; vlastní applicability gate, až přijde na řadu |
| BLE (Tesla RoutableMessage) | fallback | vlastní session/auth vrstva, vyšší implementační náklad než CAN |

Tesla R5 service document confirms the third-party CAN interface for specified
Model 3/Y production ranges and regions. It also warns that older configurations
can have a separate DoIP diagnostic port, so model, production date, region and
physical port configuration remain part of the applicability gate.

### Candidate signály (Model 3/Y, CAN větev)

Žádná z položek níž není `VERIFIED`. Všechny čekají na vehicle capture/replay.

| Veličina | Zdroj CAN ID (candidate) | Stav |
|---|---|---|
| SOC | `0x292` | candidate |
| HV pack voltage/current | `0x132` | candidate |
| BMS status | `0x212` | candidate |
| BMS thermal status | `0x312` | candidate |
| Charging/UI | `0x333` | candidate |
| Charge-line V/A/W | `0x264` | candidate |
| SOH | — | **UNKNOWN, žádná CAN cesta zatím nenalezena** |

SOH se řeší jako samostatný problém — až po ověření prvních šesti položek se
zkoumá, jestli je dostupné přes diagnostickou UDS větev nebo jinou servisní
cestu, ne přes stejný CAN mechanismus jako živá data.

### Co z tohohle plyne pro implementaci

- Candidate CAN ID výše se **nesmí** zapojit do žádného runtime kódu
  (`ObdLiveDataEngine` ani jiného), dokud neprojdou stejným schvalovacím
  řetězcem jako Mitsubishi Outlander signály (candidate → vehicle capture → replay → verified).
- Veřejně publikovaná Tesla dokumentace s konkrétním ID/bity/délkou/endianitou/
  faktorem/offsetem/jednotkou je silnější evidence než komunitní DBC — pokud
  jsou k dispozici obě, upřednostni oficiální zdroj a zaznamenej to do `source`
  pole candidate záznamu.
- Neplatí předpoklad, že každý veřejně publikovaný Tesla DBC signál je
  dostupný na každé konfiguraci Modelu 3/Y na stejném portu — to se ověřuje
  per generace/výrobní rozsah, ne paušálně.

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
