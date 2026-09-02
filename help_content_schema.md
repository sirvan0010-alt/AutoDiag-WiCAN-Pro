# AutoDiag-WiCAN-Pro — Help/Tooltip Content Schema

Centrální zdroj pravdy pro popisky a nápovědu. Žádné hardcoded stringy v UI.

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
      Meze až po Capability Discovery / Sampling Calibration Test (maxStableHz).
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
  short_tooltip: "Změří stabilní frekvenci čtení pro celý řetězec auto–WiCAN–síť."
  extended:
    description: >
      Nejvyšší frekvence, při které komunikace ještě zůstává spolehlivá
      (maximum stable Hz), ne maximum, kde ještě něco přišlo.
      Záleží na vozidle, ECU, protokolu, WiCAN, Wi-Fi a počtu současných
      signálů — ne na „rychlosti auta“.
    duration_estimate: "desítky sekund až pár minut"
    prerequisites:
      - "WiCAN připojený"
    what_it_does:
      - "Postupně zvyšuje frekvenci; sleduje timeout, latenci, jitter, dropped samples"
      - "Foreground service s notifikací"
      - "Stop při ERROR_PASSIVE / bus chybě"
      - "Uloží maxStableHz per vozidlo + WiCAN FW"
    safety_note: >
      Pouze čtení. Zátěž roste postupně, s hysterezí pod bodem selhání.
  verification: "verified"
  a11y_label: "Spustit kalibrační test maximální stabilní frekvence komunikace"
```

## Pravidla

1. Žádné vymyšlené prahy / CAN ID jako fakt.
2. HV / drive unit / Riso: jen co vozidlo poskytne.
3. Sampling: cílová vs. `effectiveHz`; kalibrace = max **stable** Hz.
4. Kalibrace: jen READ, foreground, stop při bus chybě.
