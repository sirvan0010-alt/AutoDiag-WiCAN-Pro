# AI_HANDOFF.md — AutoDiag-WiCAN-Pro

Živý handoff pro AI a vývojáře. Není roadmapa. Před prací vždy znovu ověř aktuální HEAD `main`.

**Auditovaný code baseline `main`:** `85db5617360ee0a08bf4cb042760435211b86c3d`
(*Add vehicle signal promotion evidence gate*, 2026-09-05).

Po tomto code baseline byly provedeny pouze dokumentační/evidence-synchronizační změny. **Nezaměňovat tento baseline za aktuální Git HEAD**; aktuální HEAD vždy ověřit přímo v GitHubu.

---

## PROJECT

AutoDiag-WiCAN-Pro — open, modulární Android automotive diagnostická a automatizační platforma nad **WiCAN PRO** (ESP32, meatpiHQ/wican-fw).

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

AI **nesmí smazat** plánovanou funkci jen proto, že ji teď nelze implementovat. Použít `BLOCKED: <důvod>`, ne „tato funkce nebude“.

**READ FIRST.** Žádné vymyšlené CAN ID, PID, Tesla signály, Riso MΩ, SOH.
`NOT_AVAILABLE` / `UNAVAILABLE` ≠ `ERROR` ≠ `FAIL`.

---

## 🟢 HOTOVO / AKTUÁLNĚ V `main`

### Dokumentace / pravidla

- `README.md`, `AI_CONTEXT.md`, `ROADMAP.md`
- `docs/` včetně `CAPABILITY_DISCOVERY.md`, `AUTOMATION_ENGINE.md`, `PRE_PURCHASE_TEST.md`, `PRE_PURCHASE_EV_TEST.md`, `EV_ACCELERATION_BATTERY_ANALYSIS.md`, `IMPLEMENTATION_TASKS.md`, `DIAGNOSTIC_KNOWLEDGE_BASE.md` a dalších
- `AI_CONTEXT.md` nyní obsahuje explicitní repository/evidence synchronization rules
- tento `AI_HANDOFF.md` popisuje code baseline a odděluje jej od následných dokumentačních commitů

### Android stack

- Multi-module: `android/app`, `android/core`, `android/simulator`
- `WiCanMdnsDiscovery` — mDNS/NSD
- `TcpWiCanTransport` — TCP + reconnect
- `SimulatorWiCanTransport` — in-process, bez sítě
- `Elm327Session` — AT init, bufferovaný reader do `>`, serializace příkazů
- `CapabilityDiscovery` — ATI, ATDP, 0902, 03, 010C
- `ConnectionViewModel` — CONNECTING → INITIALIZING_ELM → DISCOVERING_CAPABILITIES → READY / ERROR
- UI: discovery, ruční IP, **ELM327 :3333**, **SLCAN :23 link-only**, **Simulátor**, tooltipy, transport state

### Capability presence (ne dekódování hodnot)

| Probe | Účel |
|-------|------|
| ATI | komunikace s adaptérem |
| ATDP | OBD protokol |
| 0902 | VIN (pokud vozidlo poskytne) |
| 03 | Mode 03 presence |
| 010C | Mode 01 presence |

SLCAN **neprohlašuje** OBD AVAILABLE jen proto, že TCP funguje.

### Evidence / research

- SEOBD/S3XY evidence a promotion gates jsou research/evidence, **ne automaticky vehicle-verified Tesla runtime diagnostika**.
- Car Scanner 2.1.50 evidence je candidate/reconstruction evidence, nikoli automaticky produkční PID engine.
- Vehicle-specific mapování se nesmí povýšit bez explicitní provenance a verification scope.

---

## 🟡 ROZPRACOVÁNO / ČÁSTEČNĚ

- Capability **IDs** pro battery/HV/DTC alerts existují v modelech, ale nejsou probené — nesmí se UI ukazovat jako AVAILABLE.
- InfoTooltip komponenta existuje; centrální help content (`help_content_schema`) se teprve zavádí.
- CI/status checks na recent commits mohou být neověřené — lokální `assembleDebug` + test na zařízení je stále nutný.
- Outlander PHEV větev/PR obsahuje více candidate dekodérů, ale žádný z nich není tímto handoffem prohlášen za vehicle-verified.

---

## 🔴 CHYBÍ / NENÍ VEHICLE-VERIFIED

- Plný Mode 01 **parser hodnot** pro produkční použití s verified OBD mapováním.
- DTC **dekodér** (P0xxx význam, multi-frame).
- Freeze frame, readiness, Mode 06.
- EV/HV: SOC, cell voltages, isolation numeric, pack current — pouze pokud vozidlo data poskytne a mapping je verified.
- OEM/Tesla specific decoders jako vehicle-verified runtime funkce.
- SLCAN CAN monitor / sniffer / frame UI.
- AUTO TEST / PRE-PURCHASE orchestrace v kódu.
- Adaptive sampling engine + time-series store.
- MQTT / Home Assistant.
- WRITE_COMMAND subsystem (izolovaný, default off).

**Důležité:** existence reverse-engineered evidence, candidate JSON nebo parseru sama o sobě neznamená `VERIFIED`.

---

## 🔵 PLÁNOVÁNO

Zdroj produktových návrhů: `FEATURE_PROPOSALS.md` (sekce A–D).

Klíčové cíle uživatele (neodstraňovat):

### PRE-PURCHASE TEST

One-tap workflow: identifikace → capability discovery → DTC/freeze/readiness → live data → 12V → EV/HV pokud dostupná → battery/cells pokud dostupná → teploty → charging → Riso pokud dostupná → bus health → load/recovery → analýza → report s confidence + provenance + seznam **co nešlo otestovat**.

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

**Adaptive sampling**:

- pomalé veličiny → nižší frekvence
- proud/napětí při dynamice → vyšší frekvence
- detekce přechodu stavu → dočasně hustší log
- ustálený stav → frekvenci snížit

Limity frekvence musí vycházet z WiCAN / ESP32 / ECU / TCP — nevymýšlet přesnost, kterou systém nemá.

Výstup: časové řady jen pro **skutečně získané** veličiny s doloženým původem. Chybí-li cell data → `UNAVAILABLE` („Vozidlo údaj neposkytlo“), ne `ERROR`.

---

## ⚠️ EVIDENCE / VERIFICATION GATE

Používej tuto hierarchii:

```text
RAW EXTRACTION
  ↓
STATIC EVIDENCE
  ↓
SCHEMA / FIELD CANDIDATE
  ↓
PROTOCOL / CAN / BLE MAPPING
  ↓
DECODER CANDIDATE
  ↓
TESTED (simulator/replay)
  ↓
VEHICLE VERIFIED
  ↓
PRODUCTION
```

Candidate evidence může obsahovat konkrétní decoder, scale nebo response indexy, pokud jsou přímo doložené zdrojem. To však **nepotvrzuje jejich fyzický význam, ECU/CAN binding ani vehicle applicability**.

### Outlander PHEV — 21 04

`21 04` je v Diagnostic-Data `main` veden jako **candidate / unverified / static_apk_extraction** s `promotionStatus: blocked: vehicle verification required`.

Aktuální evidence dovoluje tvrdit pouze:

- request: `21 04`
- 32 output positions, index `0..31`
- decoder: `unsigned_u8`
- scale: `0.02` (`/50.0`)
- unit: `V`
- zdroj: přímá DEX evidence z PHEV Watchdog APK

Nesmí se tvrdit bez další evidence:

- přesné fyzické přiřazení hodnot k článkům/modulům
- ECU adresa / CAN ID
- konkrétní CAN framing
- generace vozidla mimo doložený scope
- vehicle verification

`21 04` tedy není „false/unresolved“ jen proto, že není vehicle-verified. Správný stav je **EXTRACTED / CANDIDATE / UNVERIFIED** a promotion do production je blokována požadavkem na vehicle capture/verification.

---

## 🚧 EXTERNÍ DIAGNOSTIC-DATA REPO

Zdroj dat je oddělen od Android aplikace.

Aktuální manifest `AutoDiag-WiCAN-Diagnostic-Data` má canonical candidate set **10 souborů**. `records.candidates` je proto **10**; `records.vehicles`, `records.ecus` a `records.signals` jsou **0**, protože manifest má počítat pouze production records, nikoli candidate-internal ECU/signal counts. Manifest obsahuje explicitní `record_count_policy`.

S3XY/Tesla evidence zůstává schema/research evidence, pokud není v Diagnostic-Data explicitně zapsána s provenance a `unverified` stavem.

---

## SAFETY

- Žádné vymyšlené CAN ID / Tesla signály / Riso MΩ z OK/fault.
- Jedna nízká cell voltage při akceleraci ≠ vadný článek.
- WRITE izolované, default off.
- Simulator/replay před reálným vozidlem pro nové dekodéry.

## WORK STYLE

- Pracovat proti **aktuálnímu GitHubu**, ne starému ZIP.
- Malé commity, testy, nepushovat falešné AVAILABLE.
- Dokumentace = specifikace; kód = implementace; simulator = test; auto = validace.
- Po každém významném commitu ověřit, že `AI_CONTEXT.md`, `AI_HANDOFF.md`, evidence a manifest stále popisují stejný stav.

*Synchronizační revize: 2026-09-05. Před další prací vždy ověř skutečný Git HEAD.*
