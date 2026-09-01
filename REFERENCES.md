# AutoDiag-WiCAN-Pro — Reference a inspirace

Přehled externích zdrojů prohlédnutých při návrhu funkcí, s konkrétním
poznatkem, co si z každého odnést. Doplněk k `FEATURE_PROPOSALS.md`.

---

## Firmware / hardware (WiCAN PRO)

### meatpiHQ/wican-fw (originální firmware)
- ESP32-C3, ELM327/SLCAN přes TCP, AutoPID, MQTT, webhook, VPN (WireGuard), BLE.
- **Poznatek:** appka by měla umět detekovat verzi firmwaru a podle ní
  aktivovat/deaktivovat funkce v UI.

### wican-fw — Supported Vehicles
- Živá, komunitně udržovaná databáze vehicle profilů (EV i spalovací),
  včetně praktických varování (polling ECU s vypnutým motorem může spustit alarm).
- **Poznatek:** importovat/propojit do Diagnostic Knowledge Base jako výchozí
  seznam profilů (community-verified signal import).

### wican-fw — Car Scanner integrace
- Firmware oficiálně podporuje Car Scanner jako klienta.
- **Poznatek:** referenční bod pro komunikační protokol appka ↔ WiCAN.

### meatpiHQ/programming_examples (CAN)
- Ukázkové kódy pro CAN od výrobce.
- **Poznatek:** low-level CAN frame handling specifický pro WiCAN hardware.

---

## Referenční aplikace

### AndrOBD (fr3ts0n/AndrOBD)
- Open-source OBD Android appka: DTC, live data, freeze frame, gauge, logging.
- **Poznatek:** rozsah „standardních“ funkcí (sekce A) a UX vzorce — ne kopírovat kód.

### TeslaMate (teslamate-org/teslamate)
- Self-hosted logger (Elixir + Postgres + Grafana + MQTT).
- **Poznatky zapracované do FEATURE_PROPOSALS:**
  - Charge cost tracking (B10)
  - Vampire drain tracking (B11)
  - Trip / geo kontext

### Car Scanner (RaceLogic / related Pro features)
- I/M Readiness, Mode 06 detail, multi-ECU, coding (WRITE).
- **Poznatek:** UX pro readiness + Mode 06; coding jen jako oddělený Expert mode (D6).

---

## Dokumentace a standardy

### SAE J1979 / OBD-II
- Mode 01–0A, PID definice, readiness monitory.
- **Poznatek:** základ pro presence probes a value parsers; nikdy nevymýšlet mapování.

### SparkFun / podobné OBD tutoriály
- Vysvětlení nekontinuálních monitorů a drive cycle.
- **Poznatek:** podklad pro help_content_schema (Mode 06, readiness tooltips).

### Projektové docs (vlastní)
- `docs/CAPABILITY_DISCOVERY.md`, `AUTOMATION_ENGINE.md`, `PRE_PURCHASE_*.md`,
  `EV_ACCELERATION_BATTERY_ANALYSIS.md`, `AI_CONTEXT.md`, `AI_HANDOFF.md`.
- **Poznatek:** autoritativní pro verification model a AUTO TEST architekturu.

---

## Co si NEvzít

- Neověřené Tesla CAN ID z fór / reverse-engineering bez `verification: verified`.
- Univerzální SOH / Riso prahy vydávané za OEM fakt.
- WRITE/coding jako default součást diagnostiky.

*Aktualizuj při přidání nového externího zdroje.*
