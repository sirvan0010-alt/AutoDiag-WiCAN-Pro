# AI_HANDOFF.md — AutoDiag-WiCAN-Pro

Živý handoff pro AI a vývojáře. Není roadmapa. Před prací vždy znovu ověř
aktuální HEAD `main` — tento soubor může být o commit pozadu.

**Audit HEAD při vzniku této verze handoffu:** `94f1409`
(`fix(obd): buffer ELM responses across TCP chunks and serialize commands`)

---

## PROJECT

AutoDiag-WiCAN-Pro — open, modulární Android automotive diagnostická a
automatizační platforma nad **WiCAN PRO** (ESP32, meatpiHQ/wican-fw).

- WiCAN PRO = hardware / interface / firmware
- AutoDiag = diagnostická a automatizační vrstva nad ním

## ARCHITECTURAL PRINCIPLE (zakotveno)

Nebudeme optimalizovat projekt na „co nejjednodušší implementaci“.

Stavíme **rozsáhlý diagnostický systém** v pořadí:

```text
hardware → transport → evidence → diagnostika → automatizace → analýza → UI
```

Vždy rozlišovat:

1. co umí **WiCAN PRO**
2. co skutečně poskytuje **konkrétní auto**
3. co umíme **bezpečně přečíst**
4. co umíme **odvodit** (s označením inference)
5. co je **experimentální / reverse-engineered**
6. co ještě **nemáme ověřené**

AI **nesmí smazat** plánovanou funkci jen proto, že ji teď nelze implementovat.
Použít `BLOCKED: <důvod>`, ne „tato funkce nebude“.

**READ FIRST.** Žádné vymyšlené CAN ID, PID, Tesla signály, Riso MΩ, SOH.
`NOT_AVAILABLE` / `UNAVAILABLE` ≠ `ERROR` ≠ `FAIL`.

---

## 🟢 HOTOVO (kód v `main`, ověřeno stromem repa)

### Dokumentace / pravidla

- `README.md`, `AI_CONTEXT.md` (24 pravidel), `ROADMAP.md`
- `docs/` včetně `CAPABILITY_DISCOVERY.md`, `AUTOMATION_ENGINE.md`,
  `PRE_PURCHASE_TEST.md`, `PRE_PURCHASE_EV_TEST.md`,
  `EV_ACCELERATION_BATTERY_ANALYSIS.md`, `IMPLEMENTATION_TASKS.md`,
  `DIAGNOSTIC_KNOWLEDGE_BASE.md`, a další

### Android stack

- Multi-module: `android/app`, `android/core`, `android/simulator`
- `WiCanMdnsDiscovery` — mDNS/NSD
- `TcpWiCanTransport` — TCP + reconnect
- `SimulatorWiCanTransport` — in-process, bez sítě
- `Elm327Session` — AT init, **bufferovaný** reader do `>`, serializace příkazů
- `CapabilityDiscovery` — ATI, ATDP, 0902, 03, 010C
- `ConnectionViewModel` — fáze CONNECTING → INITIALIZING_ELM →
  DISCOVERING_CAPABILITIES → READY / ERROR
- UI: discovery, ruční IP, **ELM327 :3333**, **SLCAN :23 link-only**,
  **Simulátor**, tooltipy, transport state (mode / state / host / port)

### Capability presence (ne dekódování hodnot)

| Probe | Účel |
|-------|------|
| ATI | komunikace s adaptérem |
| ATDP | OBD protokol |
| 0902 | VIN (pokud vozidlo poskytne) |
| 03 | Mode 03 presence |
| 010C | Mode 01 presence |

SLCAN **neprohlašuje** OBD AVAILABLE jen proto, že TCP funguje.

---

## 🟡 ROZPRACOVÁNO / částečně

- Capability **IDs** pro battery/HV/DTC alerts existují v modelech, ale
  **nejsou probené** — nesmí se UI ukazovat jako AVAILABLE
- InfoTooltip komponenta existuje; centrální help content (`help_content_schema`)
  se teprve zavádí
- CI/status checks na recent commits často **neověřené** — lokální
  `assembleDebug` + test na zařízení je stále nutný

---

## 🔴 CHYBÍ (kód / ověřená funkce)

- Plný Mode 01 **parser hodnot** (RPM, speed, coolant, …)
- DTC **dekódér** (P0xxx význam, multi-frame)
- Freeze frame, readiness, Mode 06
- EV/HV: SOC, cell voltages, isolation numeric, pack current — **jen když
  vozidlo data poskytne a mapping je verified**
- OEM/Tesla specific decoders
- SLCAN CAN monitor / sniffer / frame UI
- AUTO TEST / PRE-PURCHASE orchestrace v kódu (spec v `docs/` už je)
- Adaptive sampling engine + time-series store
- MQTT / Home Assistant
- WRITE_COMMAND subsystem (izolovaný, default off)
- Licence v root (ověřit stav)
- `SAFETY.md` v root — README na něj odkazuje; pokud chybí, doplnit

---

## 🔵 PLÁNOVÁNO

Zdroj produktových návrhů: `FEATURE_PROPOSALS.md` (sekce A–D).

Klíčové cíle uživatele (neodstraňovat):

### PRE-PURCHASE TEST

One-tap workflow: identifikace → capability discovery → DTC/freeze/readiness →
live data → 12V → EV/HV pokud dostupná → battery/cells pokud dostupná →
teploty → charging → Riso pokud dostupná → bus health → load/recovery →
analýza → report s confidence + provenance + seznam **co nešlo otestovat**.

### AUTO TEST architektura

```text
identifikuj → zjisti dostupná data → nastav test podle schopností vozidla
→ měř → synchronně loguj → detekuj změny → analyzuj průběh
→ vyhodnoť kvalitu měření → uveď omezení → závěr s confidence/provenance
```

Ne: „PID → OK/FAIL podle univerzálního limitu“.

### HV baterie — automatizovaný test (Phase 5 / PRE-PURCHASE)

Scénáře podle možností vozidla:

1. **REST** — SOC, V, I, T, cells/modules pokud dostupná
2. **CHARGE** — průběhy při nabíjení
3. **LOAD** — topení / spotřebiče / akcelerace — živá data + log
4. **RECOVERY** — návrat po zátěži
5. Přechody REST→LOAD→RECOVERY, CHARGE→LOAD→RECOVERY→CHARGE

**Adaptive sampling** (ne pevné „1× za X s“):

- pomalé veličiny → nižší frekvence
- proud/napětí při dynamice → vyšší frekvence
- detekce přechodu stavu → dočasně hustší log
- ustálený stav → frekvenci snížit

Limity frekvence musí vycházet z WiCAN / ESP32 / ECU / TCP — nevymýšlet
přesnost, kterou systém nemá.

Výstup: časové řady jen pro **skutečně získané** veličiny s doloženým původem.
Chybí-li cell data → `UNAVAILABLE` („Vozidlo údaj neposkytlo“), ne `ERROR`.

---

## ⚠️ NEOVĚŘENO

Veškerá vehicle-specific mapování, Tesla CAN, battery mV prahy, Riso MΩ,
SOH výpočty bez OEM reportu — dokud nemají `verification: verified` + scope
(VIN/firmware/HW).

---

## 🚧 HARDWARE LIMIT

WiCAN/ESP32, CAN bitrate, TCP throughput, polling rate, buffer, počet
současných signálů, Android výkon při hustém logu během AUTO TEST.

Při limitu: `BLOCKED: hardware/transport limit`, funkce zůstává v plánu.

---

## CURRENT TASK (doporučené pořadí po tomto docs commitu)

1. Lokální `./gradlew :app:assembleDebug` + test **Simulátor** end-to-end
2. Test reálný WiCAN: ELM327 + SLCAN link-only
3. Mode 01 value parser pro PIDy s **verified** OBD mapováním
4. DTC parser z Mode 03 odpovědi (bez vymyšlených oprav)
5. Help content podle `help_content_schema.md` napojený na capability ID
6. Teprve pak orchestration PRE-PURCHASE / HV adaptive sampling engine

---

## HELP SYSTEM

Každý diagnostický prvek: `short_tooltip`, `extended`, `verification`,
`kb_link`, `a11y_label`. Schema: `help_content_schema.md`.

---

## SAFETY (shrnutí)

- Žádné vymyšlené CAN ID / Tesla signály / Riso MΩ z OK/fault
- Jedna nízká cell voltage při akceleraci ≠ vadný článek
- WRITE izolované, default off
- Simulator/replay před reálným vozidlem pro nové dekodéry

---

## WORK STYLE

- Pracovat proti **aktuálnímu GitHubu**, ne starému ZIP
- Malé commity, testy, nepushovat falešné AVAILABLE
- Dokumentace = specifikace; kód = implementace; simulator = test; auto = validace

*Aktualizuj tento soubor při každé významné změně stavu `main`.*
