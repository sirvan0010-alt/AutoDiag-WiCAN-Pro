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

### A2. DTC — diagnostické chybové kódy
- Čtení Mode 03 (uložené kódy), Mode 07 (pending), Mode 0A (permanent).
- Mazání kódů (Mode 04) — s explicitním potvrzením a upozorněním, že to nemusí
  vyřešit příčinu.
- Napojení na Diagnostic Knowledge Base (Phase 6) — každý DTC ukazuje popis, možné
  příčiny, postup diagnostiky, se `source/verification` tagem podle existujícího modelu.
- Rozlišení generic (P0xxx) vs. manufacturer-specific (P1xxx) kódů.

### A3. Freeze Frame (zmrazená data)
- Mode 02 — snapshot hodnot PIDs v okamžiku vzniku DTC.
- Zobrazit vedle každého DTC jako kontext ("jaké byly podmínky, když chyba nastala").
- Historie freeze frame dat napříč více vznikem/mazáním kódů (pokud to ECU podporuje).

### A4. Readiness/Inspection Monitory (kontinuální i nekontinuální)
- Mode 01 PID 01 — stav MIL a počet aktivních kódů.
- Continuous monitors: misfire, fuel system, comprehensive components.
- Non-continuous monitors: catalyst, heated catalyst, EVAP system, O2 sensor,
  O2 heater, EGR/VVT, sekundární vzduch, klimatizace (podle standardu).
- Vizuální stav: Ready / Not Ready / Not Supported — potřebné pro emisní kontrolu (STK).

### A5. ECU identifikátory
- Mode 09: VIN, kalibrační ID (CALID), CVN (kalibrační verifikační číslo), ECU name.
- Výpis všech modulů odpovídajících na CAN (multi-ECU scan — motor, převodovka,
  ABS, airbag, BCM atd., pokud auto podporuje UDS/broadcast).
- Zobrazení podporovaných PID rozsahů.

### A6. Performance testy
- 0–100 km/h, 100–200, 1/4 mile — s GPS + OBD rychlostí.
- Výsledek orientační, s disclaimerem (povrch, sklon, stav vozidla).

### A7. Emisní / STK připravenost
- Souhrn MIL + readiness + počet cyklů od clear DTC.

---

## B. Rozšiřující funkce (nad základní OBD)

### B1. Detekce verze firmwaru WiCAN
- ATI / firmwarové stringy → aktivace/deaktivace funkcí v UI.

### B2. MQTT / Home Assistant / webhook
- Vzdálená telemetrie (WiCAN už má základ).

### B3. Multi-vehicle sessions
- Profil vozidla + historie měření.

### B4. Stavové notifikace
- DTC, napětí 12V, SOC, bus health — lokální i remote.

### B5–B7. Community signal import, vehicle profile DB
- Import z wican-fw supported vehicles / komunitních map.

### B8. Drag&drop dashboard
- Uživatelské uspořádání widgetů.

### B9. Tooltipy všude
- short_tooltip + extended + verification + kb_link + a11y (viz help_content_schema.md).

### B10. Charge cost tracking
- Náklady na nabíjení (kWh × tarif) — inspirace TeslaMate.

### B11. Vampire drain 12V + HV
- Sledování klidového odběru.

### B12. Geo-fencing / trip context
- Volitelné GPS kontext pro logy.

---

## C. Reference

Viz `REFERENCES.md`.

---

## D. Pro-tier / pokročilé READ (a oddělený WRITE)

### D1–D5
- Multi-ECU/OEM DTC, custom PID editor, trip computer, autotest orchestrace,
  hluboká BMS (cell deviation, pack current, thermal, isolation) — **jen když
  vozidlo data skutečně poskytne**.

### D6. Car Coding (WRITE)
- **Oddělená high-risk kategorie.** Není součástí standardního plánu.
  Vyžaduje samostatné rozhodnutí, Expert mode, opt-in, log každého zápisu.
  Implementovat až po pevném READ základu a explicitní bezpečnostní politice.

---

## Priorita pro Android MVP (návrh pořadí implementace)

Stav k auditu HEAD ~94f1409 / 98c7b5d: transportní vrstva, mDNS, ELM buffer,
CapabilityDiscovery (presence), SimulatorWiCanTransport a 3 tlačítka v UI jsou
už v `main`. SAFETY.md zatím chybí jako samostatný soubor (pravidla jsou v
AI_CONTEXT.md).

1. ~~Phase 0 skeleton + Phase 1 transport~~ — hotovo v main (ověřit assembleDebug + simulátor)
2. Phase 3 + A1–A5: Live data value parser, DTC, Freeze Frame, Readiness, ECU ID
   — toto je funkční MVP srovnatelné s běžnými OBD appkami (presence už umíme)
3. A6–A7: performance a emisní testy — snadné doplnění nad již fungující live data
4. B9: tooltipy/nápověda — zavádět průběžně s každou novou obrazovkou, ne až na konci
   (schema: help_content_schema.md)
5. Phase 4–6: Tesla/EV specifika, Knowledge Base — pouze ověřená data
6. HV charge/load/recovery adaptive sampling + PRE-PURCHASE orchestration
   (viz AI_HANDOFF.md a docs/PRE_PURCHASE_*.md) — klíčová automatizace
7. B10–B11 (charge cost tracking, vampire drain 12V/HV): brzy po EV data path
8. D1–D5: pokročilé READ funkce (multi-ECU, custom PID editor, trip computer,
   hluboká BMS diagnostika) — po zvládnutí základního MVP a EV specifik
9. B1–B8, B12: rozšiřující vrstva dle kapacity
10. D6 (Car Coding): pouze explicitní rozhodnutí, Expert mode, nejpozdější fáze

**Architektonický princip (neměnit):** stavíme rozáhlý diagnostický systém
v pořadí hardware → transport → evidence → diagnostika → automatizace → analýza → UI.
Neoptimalizujeme na „co nejjednodušší implementaci“. AUTO TEST / PRE-PURCHASE
včetně HV REST→CHARGE→LOAD→RECOVERY s adaptive sampling zůstává plnohodnotným
cílem, ne „jen graf baterie“.

---

*Poznámka: veškeré nové funkce dodržují existující bezpečnostní model projektu —
žádná WRITE/control funkcionalita mimo Phase 8 / D6, žádné neověřené CAN signály
představované jako fakt. `NOT_AVAILABLE` je platný výsledek.*
