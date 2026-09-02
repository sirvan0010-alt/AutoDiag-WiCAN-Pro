# AutoDiag-WiCAN-Pro — Feature Proposals

Doplňkový dokument k `ROADMAP.md`. Obsahuje (A) standardní funkce, které by měla mít
každá seriózní OBD-II diagnostická aplikace, a (B) rozšiřující funkce navržené nad
rámec aktuálního roadmapu. Každá položka je zařazená do fáze, kam nejlépe zapadá,
a respektuje bezpečnostní model projektu (READ-first, verification levels).

---

## A. Standardní OBD-II funkce (core, Phase 3–4)

Tyto funkce chybí explicitně vyjmenované v roadmapu a měly by být součástí MVP,
protože je očekává každý uživatel přicházející z aplikací typu Torque, Car Scanner
nebo OBD Fusion.

### A1. Live Data (živá data)
- Standardní PIDy (Mode 01) v reálném čase: RPM, rychlost, teplota chladicí kapaliny,
  MAP/MAF, lambda/O2 senzory, palivový tlak, timing advance, napětí baterie atd.
- Vlastní dashboard s widgety (gauge, graf, číselník) — uživatelsky skládatelný.
- Možnost nahrávat live data do capture logu (návaznost na Phase 2 simulator/replay).
- Konfigurovatelná frekvence dotazování per-PID (šetření CAN bandwidth i baterie vozidla).
  Viz také `docs/ADAPTIVE_SAMPLING_AND_BUS_HEALTH.md` (automatické vs. ruční meze).

### A2. DTC — diagnostické chybové kódy
- Čtení Mode 03 (uložené kódy), Mode 07 (pending), Mode 0A (permanent).
- Mazání kódů (Mode 04) — s explicitním potvrzením a upozorněním, že to nemusí
  vyřešit příčinu.
- Napojení na Diagnostic Knowledge Base (Phase 6) — každý DTC ukazuje popis, možné
  příčiny, postup diagnostiky, se `source/verification` tagem podle existujícího modelu.
- Rozlišení generic (P0xxx) vs. manufacturer-specific (P1xxx) kódů.

### A3. Freeze Frame (zmrazená data)
- Mode 02 — snapshot hodnot PIDů v okamžiku vzniku DTC.
- Zobrazit vedle každého DTC jako kontext („jaké byly podmínky, když chyba nastala“).
- Historie freeze frame dat napříč více vznikem/mazáním kódů (pokud to ECU podporuje).

### A4. Readiness/Inspection Monitory (kontinuální i nekontinuální)
- Mode 01 PID 01 — stav MIL a počet aktivních kódů.
- Continuous monitors: misfire, fuel system, comprehensive components.
- Non-continuous monitors: catalyst, heated catalyst, EVAP system, O2 sensor,
  O2 heater, EGR/VVT, sekundární vzduch, klimatizace (podle standardu).
- Vizuální stav: Ready / Not Ready / Not Supported — potřebné pro emisní kontrolu (STK).

### A5. ECU identifikátory
- Mode 09: VIN, kalibrační ID (CALID), CVN, ECU name.
- Výpis všech modulů odpovídajících na CAN (multi-ECU scan), pokud auto podporuje UDS/broadcast.
- Zobrazení podporovaných PID rozsahů per-ECU (Mode 01 PID 00/20/40/60…).

### A6. Testy zrychlení (performance)
- 0–100 km/h, 0–60 mph, čtvrt míle — GPS + OBD rychlost.
- Braking test (rychlost → 0).
- Log/replay s grafem; torque/power estimate jen jako `unverified` / orientační.

### A7. Emisní testy / I/M Readiness
- Souhrn „připravenost na STK“: MIL + readiness + drive cycles od clear DTC.
- Mode 05 / Mode 06 tam, kde ECU podporuje.
- Export pre-STK report (PDF/CSV).

---

## B. Rozšiřující funkce

### B1. Firmware capability probe (Phase 1)
Při připojení zjistit verzi WiCAN PRO firmwaru a aktivovat/deaktivovat funkce v UI
podle skutečné podpory (WebSocket, AutoPID, VPN, filtry) — žádná mrtvá tlačítka.

### B2. Offline-first / last-known-state (Phase 7)
Cache posledního známého stavu vozidla při ztrátě spojení; „last seen“ s timestampem.

### B3. Battery heatmapa (Phase 5)
Teplotní/napěťová heatmapa cell/module v čase — doplněk Pack → Module → Cell.

### B4. Export MDF4/ASAM (Phase 2 / 5)
Vedle CSV/JSON export kompatibilní s MDF4 (CANalyzer, asammdf, INCA).

### B5. Multi-vehicle / multi-device sessions (Phase 4/7)
Přepínání profilů mezi více auty / WiCAN jednotkami.

### B6. Stavově podmíněné notifikace (Phase 7)
Automation engine rozlišuje parked vs. driving — méně rušení za jízdy.

### B7. Community-verified signal import (Phase 9)
Import CAN definic od komunity vždy jako `unverified`, dokud neprojdou verification modelem.

### B8. Přizpůsobitelný dashboard — drag & drop (Phase 3/7)
- Přeskládání widgetů per vehicle profile; grid snapping; velikosti dlaždic.
- Reset na výchozí layout.
- Persist (DataStore/Room) per profil.

### B9. Tooltipy / kontextová nápověda (napříč appkou)
- short_tooltip + extended + verification + kb_link + a11y.
- Centrální YAML/JSON (viz `help_content_schema.md`), ne hardcoded stringy.
- Onboarding guided tour volitelně.

### B10. Charge Cost Tracking (Phase 5 — EV)
Session kWh, tarify, měsíční/roční přehled, export; relevantní i pro PHEV.

### B11. Vampire Drain — 12V i HV (Phase 5)
Klidový odběr 12V a HV SoC/den; trend; korelace s teplotou; notifikace jen když auto stojí.

### B12. Geo-fencing (Phase 7)
Vlastní lokace pro charge cost a segmentaci vampire drain.

---

## C. Reference

Viz `REFERENCES.md`.

---

## D. Pro-tier READ a oddělený WRITE

### D1. Multi-ECU a OEM-specifické DTC (Phase 4/6)
ABS/ESP, SRS, BCM, HVAC; manufacturer DTC formáty; Flash Counter READ-only.

### D2. Live data bez umělého limitu + custom PID editor (Phase 3)
Custom hex request + konverze jako `unverified` do ověření.

### D3. Trip computer a datalogger (Phase 2/7)
Agregace jízd; CSV/MDF4 export.

### D4. Autotest / Component Monitor (Phase 4)
READ-only přehled integrity signálů, které ECU už vyhodnocuje — appka neaktivuje aktuátory.

### D5. Hluboká EV/PHEV BMS diagnostika (Phase 5)
- **Cell Voltage Deviation** — Delta V max–min; Pack → Module → Cell; heatmapa (B3).
- **Total Pack Current** — proud přes stykač při zátěži i regeneraci.
- **Drive Unit / Motor Current & Power** — proud a výkon do trakčního motoru, **pokud ECU poskytuje**.
  Doplněk k pack current („kolik z baterie“ vs. „kolik do pohonu“). Vzorkování ve třídě
  **FAST** (`docs/ADAPTIVE_SAMPLING_AND_BUS_HEALTH.md` A2). Pokud vozidlo neposkytuje →
  `UNAVAILABLE`, **nikdy** dopočítaný odhad jako změřená hodnota.
- **Thermal matrices** — teploty modulů / hotspoty.
- **Isolation Resistance (MΩ)** — READ-only, jen pokud vozidlo reportuje; žádný převod
  surových bytů na MΩ bez verified mapování.

Vše READ-only. Žádné falešné AVAILABLE.

### D6. ⚠️ Car Coding / Adaptation — HIGH-RISK (mimo standardní roadmap)
WRITE do ECU. Jen po `SAFETY.md`, Expert mode, opt-in, log zápisů, ověřené procedury.
Nejdřív READ; případně vůbec, pokud cíl je diagnostika.

---

## Priorita pro Android MVP (aktualizováno vůči `main`)

Stav: transport, mDNS, ELM buffer, CapabilityDiscovery (presence), SimulatorWiCanTransport
a 3 režimy připojení jsou v `main`. `SAFETY.md` jako samostatný soubor chybí
(pravidla v `AI_CONTEXT.md`).

1. ~~Phase 0 skeleton + Phase 1 transport~~ — v main; **ověřit** `assembleDebug` + simulátor
   (`docs/SIMULATOR_TEST_SCENARIOS.md`)
2. Phase 3 + A1–A5: value parser Mode 01, DTC, Freeze Frame, Readiness, ECU ID
3. A6–A7: performance a emisní testy
4. B9: tooltipy průběžně (`help_content_schema.md`)
5. Phase 4–6: EV/Tesla jen s verified daty; Knowledge Base
6. HV REST→CHARGE→LOAD→RECOVERY + adaptive sampling
   (`docs/ADAPTIVE_SAMPLING_AND_BUS_HEALTH.md`, `docs/PRE_PURCHASE_*.md`)
7. B10–B11 charge cost / vampire drain
8. D1–D5 včetně drive unit current (jen když AVAILABLE)
9. B1–B8, B12 dle kapacity
10. D6 coding — jen explicitní rozhodnutí

**Princip:** hardware → transport → evidence → diagnostika → automatizace → analýza → UI.
`NOT_AVAILABLE` je platný výsledek. Adaptive sampling default **automatic**; ruční
override jen v ověřeném rozsahu (A7).

---

*Žádná WRITE mimo Phase 8 / D6. Žádné neověřené CAN signály jako fakt.*
