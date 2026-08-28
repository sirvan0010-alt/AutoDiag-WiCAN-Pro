# PRE-PURCHASE TEST — UI SPECIFICATION

## Purpose

`PRE-PURCHASE TEST` is a read-only diagnostic workflow for used EV inspection. The UI must distinguish measured/reported evidence from interpretation and must never convert missing data into a positive or negative diagnosis.

## Main result screen

The first result screen should be readable in seconds:

```text
PRE-PURCHASE TEST

Identita vozidla             ● OK        ?
Kilometry                    ● SOULAD    ?
Airbag / crash evidence      ! NÁLEZ     ?
HV pyropojistka / PBDU       ● OK        ?
HV izolace (Riso)             ● OK        ?
Baterie / SOH                ● DOBRÁ     ?
Vyvážení článků              ● OK        ?
Tepelný systém               ● OK        ?
Pohon                        ● OK        ?

Pokrytí diagnostiky          87 %        ?

CELKOVÉ HODNOCENÍ             KONTROLA   ?
```

The exact rows depend on vehicle capabilities. Unsupported evidence is still represented, but the user-facing text is Czech.

## User-facing state vocabulary

Internal enum names are allowed in code and logs. They must not be shown directly as the primary UI label.

| Internal state | Primary Czech UI label | Meaning |
|---|---|---|
| `REPORTED_OK` | `OK` | ECU or validated measurement reports a normal state. |
| `REPORTED_FAULT` | `ZÁVADA` | Vehicle reports a relevant fault. |
| `CRASH_RELATED_EVIDENCE_FOUND` | `NÁLEZ` | Relevant crash/safety evidence was found. |
| `TRIPPED_REPORTED` | `AKTIVOVÁNO` | Supported ECU explicitly reports activation of the relevant HV pyrotechnic disconnect. |
| `FAULT_REPORTED` | `ZÁVADA` | Supported ECU reports a related fault. |
| `NOT_AVAILABLE` | `Vozidlo údaj neposkytlo` | The vehicle/interface did not provide the required signal or value. This is neither PASS nor FAIL. |
| `NOT_TESTED` | `Test neproveden` | The stage was intentionally not executed. |
| `UNASSESSED` | `Nelze vyhodnotit` | Available data are insufficient for a defensible interpretation. |
| `UNVERIFIED` | `Neověřeno` | A candidate signal or interpretation exists, but its meaning/scope is not sufficiently verified. |
| `INSUFFICIENT_COVERAGE` | `Nedostatečné pokrytí` | Important evidence sources were unavailable, so the result cannot be treated as complete. |
| `ERROR` | `Chyba komunikace` | The diagnostic transaction failed; this does not imply an ECU or vehicle fault. |
| `SUSPICIOUS_CONFIGURATION` | `Podezřelá konfigurace` | An anomaly requires further inspection; it is not proof of fraud or repair. |

## Mandatory `?` tooltip contract

Every row, metric, status, threshold and non-obvious result has an `?` tooltip.

Each tooltip contains, where applicable:

1. **Co se měří** — plain Czech explanation.
2. **Proč je to důležité** — practical significance before purchase.
3. **Zdroj** — ECU, CAN, UDS, physical measurement or calculated value.
4. **Výsledek** — what the current result says.
5. **Omezení** — what the result cannot prove.
6. **Ověření** — `OVĚŘENO`, `ČÁSTEČNĚ OVĚŘENO` or `NEOVĚŘENO`.

Tooltip content must come from the central knowledge base rather than being duplicated in screens.

## Airbag / crash evidence

The pre-purchase workflow must explicitly show an `Airbag / crash evidence` row.

Possible evidence includes, only where exposed and verified for the vehicle profile:

- airbag/RCM DTCs;
- deployment status;
- pretensioner deployment evidence;
- crash-event records where documented by the ECU;
- ECU identity/VIN mismatch;
- relevant Gateway history;
- BMS/HV evidence associated with a crash or pyrotechnic disconnect.

A clean current airbag-controller result must never be rendered as `NO ACCIDENTS`. The tooltip must explain that absence of available crash evidence is not proof that no accident occurred.

### Tooltip — Airbag / crash evidence

> Tato kontrola hledá dostupné záznamy a stavy související s havárií, aktivací airbagů, předpínačů a dalších bezpečnostních systémů. Pokud vozidlo nebo řídicí jednotka historii neposkytuje, výsledek neznamená, že vozidlo nikdy nebylo havarované.
>
> **Zdroj:** diagnostická data dostupná z podporovaných řídicích jednotek.
> **Omezení:** rozsah historie závisí na konkrétním vozidle a jednotce.
> **Ověření:** podle konkrétního profilu vozidla.

## HV pyrotechnic disconnect / pyrofuse

The pre-purchase workflow must explicitly show an `HV pyropojistka / PBDU` row.

The application may report a state only when a supported vehicle exposes a documented or independently verified signal.

Allowed evidence can include:

- BMS/HV controller status or DTC indicating pyrotechnic disconnect;
- related crash/safety evidence;
- HV contactor/interlock evidence where relevant;
- ECU identity and configuration evidence;
- documented event history where the vehicle exposes it.

An HV battery pyrofuse/PBDU is not an airbag squib. Generic airbag squib resistance must never be displayed as HV pyrofuse resistance.

Equal resistance readings between airbag circuits are not proof of an emulator. At most, a supported and statistically justified anomaly can be reported as `Podezřelá konfigurace` and referred for physical/service-document inspection.

### Tooltip — HV pyropojistka / PBDU

> Pyropojistka je bezpečnostní prvek vysokonapěťové baterie, který může při závažné události elektricky odpojit HV obvod. AutoDiag zde zobrazuje pouze stav, který skutečně poskytuje podporovaná řídicí jednotka.
>
> **Důležité:** HV pyropojistka/PBDU není totéž co rozbuška airbagu. Hodnota odporu airbagového okruhu se nesmí vydávat za odpor HV pyropojistky.
>
> **Omezení:** pokud vozidlo tento údaj neposkytuje, AutoDiag nemůže stav pyropojistky potvrdit ani vyloučit.
> **Ověření:** podle konkrétní platformy, ECU a ověřeného diagnostického signálu.

## HV isolation / Riso

The UI must distinguish vehicle-reported isolation from a physical insulation-resistance test.

The displayed result should include the raw value and unit when available, plus voltage context and threshold provenance when a threshold is applied.

A universal hard-coded `200 kΩ` ISO minimum must not be used. Interpretation must follow the verified vehicle architecture and applicable protection criteria.

### Tooltip — HV izolace (Riso)

> Riso je izolační odpor mezi vysokonapěťovým systémem a karoserií. Nízká hodnota může souviset například s poškozenou izolací, vlhkostí nebo závadou některého HV komponentu.
>
> **Zdroj:** hodnota hlášená vozidlem nebo samostatné fyzické měření; tyto dva typy důkazu AutoDiag nerozmíchává.
> **Omezení:** použitý limit závisí na elektrické architektuře a ověřeném zdroji.
> **Ověření:** zobrazit společně s rozsahem platnosti použitého limitu.

## Missing data

When a signal is absent, the UI must say exactly:

**`Vozidlo údaj neposkytlo`**

The tooltip must explain:

> Vozidlo nebo připojené diagnostické rozhraní neposkytuje potřebný údaj. Tento stav neznamená, že je součástka v pořádku, ani že je vadná. Pouze nebyl získán dostatečný diagnostický důkaz.

The UI must never display raw `NOT_AVAILABLE` as the main user-facing label.

## Communication errors

Technical error identifiers remain in logs. User-facing errors are Czech.

Example:

```text
Chyba komunikace
?

Nepodařilo se přečíst diagnostický údaj.
Řídicí jednotka v nastaveném časovém limitu neodpověděla.
To samo o sobě neznamená, že je řídicí jednotka vadná.

Kód: UDS_TIMEOUT
Jednotka: BMS
```

## Overall verdict

The overall result must not be a simple `PASS` when important forensic evidence is unavailable.

Recommended states:

- `BEZ ZJIŠTĚNÝCH PROBLÉMŮ` — relevant tested evidence is normal and coverage is sufficient.
- `VYŽADUJE KONTROLU` — one or more findings require inspection.
- `OMEZENÉ HODNOCENÍ` — important evidence was not available.
- `CHYBA TESTU` — the workflow could not complete reliably.

A clean result means **no relevant problem was found in the available verified evidence**, not that the vehicle is guaranteed accident-free or defect-free.

## Safety

`PRE-PURCHASE TEST` is read-only. It must not probe, bridge, bypass, energize, reset or command pyrotechnic circuits. Vehicle-specific CAN IDs, UDS DIDs and thresholds remain unverified until supported by appropriate evidence.
