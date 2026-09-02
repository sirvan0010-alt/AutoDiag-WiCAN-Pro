# AutoDiag-WiCAN-Pro — Help/Tooltip Content Schema

Centrální zdroj pravdy pro veškeré popisky, tooltipy a nápovědu v aplikaci.
Odděleno od kódu (žádné hardcoded stringy) kvůli lokalizaci a propojení
s Diagnostic Knowledge Base (Phase 6).

## Umístění (návrh)

```text
docs/help_content/
  schema.json
  pids/
  actions/
  dtc/
  kb/
```

## Princip

| Pole | Účel |
|------|------|
| `id` | Stabilní identifikátor (UI / capability) |
| `short_tooltip` | 1–2 věty |
| `extended` | Delší popis, předpoklady, safety |
| `verification` | `verified` / `partially_verified` / `unverified` |
| `kb_link` | ID článku v Knowledge Base |
| `a11y_label` | Accessibility |

Help je součást architektury, ne kosmetika na konci.

---

## 1. Schema pro PID

```yaml
- id: "pid_010C"
  mode: "01"
  pid: "0C"
  name: "Otáčky motoru"
  unit: "RPM"
  short_tooltip: "Aktuální otáčky motoru za minutu."
  extended:
    description: >
      Otáčky klikového hřídele. Klíčové pro volnoběh, misfire a performance testy.
    normal_range:
      idle: "600–1000 RPM (dle vozidla)"
    notes: "0 při běžícím motoru = chyba senzoru nebo komunikace, ne nutně zastavení."
  kb_link: null
  verification: "verified"
  a11y_label: "Otáčky motoru v otáčkách za minutu"
  polling:
    default_hz: 5
    min_interval_ms: 100
```

## 2. Schema pro akce / testy

```yaml
- id: "test_accel_0_100"
  category: "performance_test"
  name: "Test zrychlení 0–100 km/h"
  short_tooltip: "Změří čas zrychlení z 0 na 100 km/h."
  extended:
    description: >
      GPS + OBD rychlost. Výsledek orientační (povrch, sklon, stav vozidla).
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

## 4. Readiness a Mode 06

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
      I/M Readiness = Complete/Not Complete. Mode 06 = surová data vs. min/max limit.
  verification: "verified"
```

## 5. Knowledge Base — Drive Cycle

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
verification: "partially_verified"
source: "generic_obd2_guidance"
```

## 6. Sampling UI (navazuje na ADAPTIVE_SAMPLING_AND_BUS_HEALTH.md A5, A7)

```yaml
- id: "ui_sampling_rate_indicator"
  category: "diagnostic_ui"
  name: "Aktuální frekvence vzorkování"
  short_tooltip: "Jak často se právě reálně čtou hodnoty ze sběrnice."
  extended:
    description: >
      Zobrazuje efektivní (reálně dosaženou) frekvenci, ne cílovou.
      Pokud transport nebo ECU nestíhá, effectiveHz je nižší — appka to
      nezaokrouhluje nahoru ani neschovává.
    notes: >
      BURST = dočasně vyšší frekvence při detekovaném přechodu
      (akcelerace, start nabíjení).
  verification: "verified"
  a11y_label: "Aktuální efektivní frekvence vzorkování dat"

- id: "sampling_override_disabled_reason"
  category: "diagnostic_ui"
  name: "Ruční nastavení frekvence — nedostupné"
  short_tooltip: "Tento rozsah zatím appka nezná, proto ho nelze upravit."
  extended:
    description: >
      Appka nikdy nenabídne rozsah frekvence, který by si musela domýšlet.
      Editovatelné meze až po Capability Discovery pro daný signál/vozidlo.
    safety_note: >
      Horní hranice nikdy nepřekročí ověřený limit — ani v expertním režimu.
  verification: "verified"
  a11y_label: "Ruční nastavení frekvence vzorkování je momentálně nedostupné"

- id: "sampling_mode_manual_override"
  category: "diagnostic_action"
  name: "Ruční přepsání frekvence vzorkování"
  short_tooltip: "Umožní nastavit vlastní frekvenci místo automatické."
  extended:
    description: >
      Výchozí režim je vždy automatický (adaptivní podle stavu testu).
      Ruční override je per test a vozidlo, ne globálně.
    prerequisites:
      - "Dokončená Capability Discovery pro daný signál/vozidlo"
    notes: >
      Režim (automatic/manual) i effectiveHz se ukládají do reportu.
  verification: "verified"
  a11y_label: "Přepnout na ruční nastavení frekvence vzorkování"

- id: "sampling_mode_toggle"
  category: "diagnostic_ui"
  name: "Režim vzorkování automatický / ruční"
  short_tooltip: "Přepínač mezi adaptivním a ručním vzorkováním pro tento test."
  verification: "verified"
  a11y_label: "Režim vzorkování"
```

## 7. JSON Schema (CI validace)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "HelpContentEntry",
  "type": "object",
  "required": ["id", "name", "short_tooltip", "verification"],
  "properties": {
    "id": { "type": "string", "pattern": "^[a-z0-9_]+$" },
    "name": { "type": "string", "maxLength": 60 },
    "unit": { "type": ["string", "null"] },
    "short_tooltip": { "type": "string", "maxLength": 120 },
    "extended": { "type": "object" },
    "kb_link": { "type": ["string", "null"] },
    "verification": {
      "type": "string",
      "enum": ["unverified", "partially_verified", "verified"]
    },
    "a11y_label": { "type": "string" }
  }
}
```

CI: (1) schema validita, (2) každé `id` z UI má záznam ve všech jazycích,
(3) limit délky `short_tooltip`.

## 8. Kotlin rozhraní (návrh)

```kotlin
data class HelpEntry(
    val id: String,
    val name: String,
    val unit: String? = null,
    val shortTooltip: String,
    val extended: ExtendedHelp? = null,
    val kbLink: String? = null,
    val verification: VerificationLevel,
    val a11yLabel: String
)

enum class VerificationLevel { UNVERIFIED, PARTIALLY_VERIFIED, VERIFIED }

interface HelpContentRepository {
    fun getEntry(id: String, locale: Locale): HelpEntry?
    fun getAllForCategory(category: String, locale: Locale): List<HelpEntry>
}
```

Stejné `id` propojuje PID/test/DTC napříč UI, help systémem a Knowledge Base.

## Pravidla pro obsah

1. Žádné vymyšlené prahy / CAN ID jako fakt.
2. `verification` odpovídá důkazům.
3. HV / cell / Riso / drive unit: explicitně závisí na tom, co vozidlo poskytne.
4. Sampling: vždy rozlišovat cílovou vs. `effectiveHz`.
5. CS primárně; schema připravené na další jazyky.
