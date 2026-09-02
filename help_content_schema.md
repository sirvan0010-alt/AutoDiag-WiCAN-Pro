# AutoDiag-WiCAN-Pro — Help/Tooltip Content Schema

C entrální zdroj pravdy pro popisky a nápovědu. Žádné hardcoded stringy v UI.

## Princip

| Pole | Účel |
|------|------|
| `id` | Stabilní ID |
| `short_tooltip` | 1–2 věty |
| `extended` | Popis, předpoklady, safety |
| `verification` | verified / partially_verified / unverified |
| `kb_link` | Knowledge Base |
| `a11y_label` | Accessibility |

---

## Sampling UI + kalibrace

```yaml
- id: "ui_sampling_rate_indicator"
  category: "diagnostic_ui"
  name: "Aktuální frekvence vzorkování"
  short_tooltip: "Jak často se právě reálně čtou hodnoty ze sběrnice."
  extended:
    description: >
      Efektivní (reálně dosažená) frekvence, ne cílová. Při limitu transportu/ECU
      effectiveHz klesá — appka to neschovává.
  verification: "verified"
  a11y_label: "Aktuální efektivní frekvence vzorkování dat"

- id: "sampling_override_disabled_reason"
  category: "diagnostic_ui"
  name: "Ruční nastavení frekvence — nedostupné"
  short_tooltip: "Rozsah zatím neznámý, proto ho nelze upravit."
  extended:
    description: >
      Meze až po Capability Discovery / Sampling Calibration Test.
      Horní hranice nikdy nepřekročí ověřený limit.
  verification: "verified"
  a11y_label: "Ruční nastavení frekvence je momentálně nedostupné"

- id: "sampling_mode_manual_override"
  category: "diagnostic_action"
  name: "Ruční přepsání frekvence vzorkování"
  short_tooltip: "Vlastní frekvence místo automatické (per test)."
  verification: "verified"
  a11y_label: "Přepnout na ruční nastavení frekvence"

- id: "sampling_mode_toggle"
  category: "diagnostic_ui"
  name: "Režim vzorkování automatický / ruční"
  short_tooltip: "Přepínač adaptivní vs. ruční pro tento test."
  verification: "verified"
  a11y_label: "Režim vzorkování"

- id: "action_sampling_calibration_test"
  category: "diagnostic_action"
  name: "Kalibrace rychlosti komunikace"
  short_tooltip: "Změří, jak rychle appka reálně umí číst data z tvého auta."
  extended:
    description: >
      Aktivní test dosažitelné frekvence pro vozidlo + WiCAN + síť.
      Výsledek určí meze ručního vzorkování (A7/A9).
    duration_estimate: "desítky sekund až pár minut"
    prerequisites:
      - "WiCAN připojený"
    what_it_does:
      - "Postupně zvyšuje frekvenci a sleduje spolehlivost"
      - "Foreground service s notifikací"
      - "Stop při chybě CAN sběrnice"
      - "Uloží výsledek per vozidlo + WiCAN FW"
    safety_note: "Pouze čtení. Zátěž roste postupně, ne skokem."
  verification: "verified"
  a11y_label: "Spustit kalibrační test rychlosti komunikace"
```

Další příklady (PID, DTC, readiness, KB) zůstávají v historii / lze doplnit
do `docs/help_content/` jako YAML soubory. CI validace proti JSON Schema
a `HelpContentRepository` — viz předchozí verze dokumentu.

## Pravidla

1. Žádné vymyšlené prahy / CAN ID jako fakt.
2. HV / drive unit / Riso: jen co vozidlo poskytne.
3. Sampling: cílová vs. `effectiveHz`.
4. Kalibrace: jen READ, foreground, stop při bus chybě.
