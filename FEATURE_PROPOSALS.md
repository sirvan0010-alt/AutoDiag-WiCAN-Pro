# AutoDiag-WiCAN-Pro — Feature Proposals

Doplňkový dokument k `ROADMAP.md`. Obsahuje (A) standardní funkce, které by měla mít
každá seriózní OBD-II diagnostická aplikace, a (B) rozšiřující funkce navržené nad
rámec aktuálního roadmapu. Každá položka je zařazená do fáze, kam nejlépe zapadá,
a respektuje bezpečnostní model projektu (READ-first, verification levels).

---

## A. Standardní OBD-II funkce (core, Phase 3–4)

### A1. Live Data (živá data)
- Standardní PIDy (Mode 01) v reálném čase; dashboard; capture log; frekvence per-PID
  (viz `docs/ADAPTIVE_SAMPLING_AND_BUS_HEALTH.md`).

### A2. DTC
- Mode 03/07/0A; Mode 04 s potvrzením; Knowledge Base + verification tagy.

### A3. Freeze Frame
- Mode 02 kontext u DTC; historie pokud ECU podporuje.

### A4. Readiness / I/M
- MIL, continuous/non-continuous monitory; Ready / Not Ready / Not Supported.

### A5. ECU identifikátory
- Mode 09 VIN/CALID/CVN; multi-ECU scan; podporované PID rozsahy.

### A6. Performance testy
- 0–100, braking; GPS+OBD; odhady jen `unverified`.

### A7. Emisní / STK
- Souhrn připravenosti; Mode 05/06 kde dostupné; pre-STK report.

---

## B. Rozšiřující funkce

### B1. Firmware capability probe
WiCAN FW verze → zapnutí/vypnutí funkcí v UI.

### B2. Offline-first / last-known-state
Cache posledního stavu se timestampem.

### B3. Battery heatmapa
Teplota/napětí cell/module v čase.

### B4. Export MDF4/ASAM
Vedle CSV/JSON.

### B5. Multi-vehicle / multi-device sessions

### B6. Stavové notifikace (parked vs driving)

### B7. Community-verified signal import
Vždy `unverified` do ověření.

### B8. Drag & drop dashboard
Layout per vehicle profile.

### B9. Tooltipy
Centrální schema (`help_content_schema.md`).

### B10. Charge Cost Tracking

### B11. Vampire Drain 12V + HV

### B12. Geo-fencing

### B13. Sampling Calibration Test (design; kód po zeleném simulátoru + reálném WiCAN)
- Aktivní test: změří dosažitelnou frekvenci pro **vozidlo + WiCAN + síť**.
- Android **foreground service** s notifikací; uživatel může appku používat.
- Výsledek `CalibrationResult` plní `allowedRange` pro ruční override
  (`docs/ADAPTIVE_SAMPLING_AND_BUS_HEALTH.md` **A0**, **A7**, **A9**).
- Pouze READ; postupné navyšování zátěže; okamžitý stop při bus chybě.
- Částečný výsledek = platný (`PARTIAL` / `INTERRUPTED`).

---

## C. Reference

Viz `REFERENCES.md`.

---

## D. Pro-tier READ a oddělený WRITE

### D1–D4
Multi-ECU DTC, custom PID, trip computer, component monitor (READ-only).

### D5. Hluboká EV/PHEV BMS
- Cell Voltage Deviation; Total Pack Current;
- **Drive Unit / Motor Current & Power** (FAST třída; `UNAVAILABLE` pokud ECU nedá);
- Thermal matrices; Isolation Resistance (jen reportovaná hodnota).

### D6. Car Coding — HIGH-RISK WRITE
Jen po SAFETY.md, Expert mode, opt-in; mimo standardní roadmap.

---

## Priorita pro Android MVP (vůči `main`)

1. ~~Transport + simulátor v main~~ — **ověřit** `assembleDebug` + `docs/SIMULATOR_TEST_SCENARIOS.md`
   - B13 Calibration: **design hotový**; implementace až po simulátoru a reálném WiCAN
2. Mode 01 parser, DTC, Freeze Frame, Readiness, ECU ID
3. A6–A7 performance / emise
4. B9 tooltipy průběžně
5. EV data path (verified only) + Knowledge Base
6. Adaptive sampling + AUTO TEST; kalibrace (A9) napájí A7 meze
7. B10–B11 charge / vampire
8. D1–D5 (drive unit jen když AVAILABLE)
9. B1–B8, B12
10. D6 jen explicitní rozhodnutí

**Princip:** hardware → transport → evidence → diagnostika → automatizace → analýza → UI.
`NOT_AVAILABLE` je platný výsledek. Sampling default **automatic**.
