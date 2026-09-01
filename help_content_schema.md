# AutoDiag-WiCAN-Pro — Help/Tooltip Content Schema

Centrální zdroj pravdy pro veškeré popisky, tooltipy a nápovědu v aplikaci.
Odděleno od kódu (žádné hardcoded stringy) kvůli lokalizaci a propojení
s Knowledge Base.

## Princip

Každý diagnostický prvek má:

| Pole | Účel |
|------|-------|
| `id` | Stabilní identifikátor (napojení na capability / UI) |
| `short_tooltip` | 1–2 věty na hover / ℹ |
| `extended` | Rozšířený popis (co se počítá, z jakých vzorků, SOC/T, trend vs okamžitá) |
| `verification` | `verified` / `partially_verified` / `unverified` |
| `kb_link` | ID článku v Diagnostic Knowledge Base |
| `a11y_label` | Accessibility label |

Help systém je **součást architektury**, ne kosmetický doplněk na konci vývoje.

## 1. Základní schema (YAML)

```yaml
- id: "battery_cell_deviation"
  category: "battery"
  name: "Odchylka napětí článků"
  short_tooltip: "Rozdíl mezi nejvyšším a nejnižším dostupným napětím článku."
  extended:
    description: >
      Počítá se jen z článků, které vozidlo skutečně poskytlo.
      Okamžitá hodnota vs. trend během zátěže se rozlišují.
    computed_from: "min/max cell V z dostupných reportů"
    notes: >
      Rozdíl při zátěži může být normální. Jedna nízká cell voltage
      při akceleraci ≠ vadný článek.
    confidence: "závisí na počtu a kvalitě dostupných článků"
  verification: "unverified"   # dokud není ověřeno pro konkrétní VIN/firmware
  kb_link: null
  a11y_label: "Odchylka napětí bateriových článků"
```

Pokud data chybí: UI ukazuje `UNAVAILABLE` / „Vozidlo údaj neposkytlo“, ne ERROR.

## 2. Příklady diagnostických akcí

```yaml
- id: "test_accel_0_100"
  category: "performance_test"
  name: "Test zrychlení 0–100 km/h"
  short_tooltip: "Změří čas zrychlení z 0 na 100 km/h."
  extended:
    description: >
      Kombinuje GPS a OBD rychlost. Výsledek orientační.
    duration_estimate: "10–20 s"
    prerequisites:
      - "Vozidlo stojí, motor běží"
      - "Bezpečné a legální místo"
    safety_note: "Pouze na uzavřeném nebo k tomu určeném prostoru."
  verification: "unverified"
  a11y_label: "Spustit test zrychlení 0 až 100"

- id: "test_emissions_readiness"
  category: "emissions_test"
  name: "Kontrola připravenosti na emise / STK"
  short_tooltip: "Zkontroluje, zda je vozidlo připravené na emisní měření."
  extended:
    description: >
      MIL, readiness monitory, počet cyklů od clear DTC.
  verification: "verified"
  a11y_label: "Kontrola připravenosti na emisní kontrolu"
```

## 3. Schema pro DTC

```yaml
- code: "P0301"
  name: "Misfire zjištěn — válec 1"
  short_tooltip: "Zapalování/spalování ve válci 1 selhává."
  extended:
    affected_system: "Zapalování / palivový systém / mechanika motoru"
    severity: "medium"
    possible_causes:
      - "Vadná svíčka nebo cívka"
      - "Ucpaný vstřikovač"
      - "Nízká komprese"
    source: "generic_obd2_standard"
    verification: "verified"
  a11y_label: "Chybový kód P0301, misfire válce jedna"
```

## 4. Readiness monitory a Mode 06

```yaml
- id: "monitor_category_im_readiness"
  category: "readiness"
  name: "Připravenost k emisní kontrole (I/M Readiness)"
  short_tooltip: "Rychlý přehled, které systémy už ECU stihla otestovat."
  extended:
    description: >
      „Not Ready“ neznamená závadu — ECU ještě neměla podmínky test dokončit
      (typicky po clear DTC nebo odpojení baterie).
  kb_link: "kb_drive_cycle_guide"
  verification: "verified"

- id: "action_mode06_detail"
  category: "diagnostic_action"
  name: "Mode 06 — detailní hodnoty monitorů"
  short_tooltip: "Přesné naměřené hodnoty a limity za jednotlivými testy."
  extended:
    description: >
      I/M Readiness = Complete/Not Complete. Mode 06 = surová data
      (naměřená hodnota vs. min/max limit).
  verification: "verified"
```

## 5. Knowledge Base článek (Drive Cycle)

```yaml
id: "kb_drive_cycle_guide"
title: "Jak dokončit jízdní cyklus (Drive Cycle) pro emisní monitory"
summary: >
  Postup, jak přimět ECU dokončit nekontinuální monitory před STK.
body_sections:
  - heading: "Proč monitory ukazují Not Complete"
    text: >
      Po clear DTC / odpojení baterie ECU vynuluje nekontinuální monitory.
  - heading: "Obecný jízdní cyklus (orientační)"
    text: >
      1. Motor vychladnout. 2. Plynulá jízda ~15 min. 3. Úsek brzdění motorem.
    disclaimer: "Přesný postup se liší dle výrobce."
```

## Pravidla pro obsah

1. Žádné vymyšlené prahy / CAN ID jako fakt.
2. `verification` musí odpovídat skutečnému stavu důkazů.
3. Tooltip vysvětluje **co** a **odkud**, ne jen marketing.
4. Pro HV / cell / Riso: explicitně uvést, že závisí na tom, co vozidlo poskytne.
5. Lokalizace: CS primárně; schema připravené na další jazyky.

Napojení: stejné `id` jako capability / UI komponenta (`InfoTooltip`).
