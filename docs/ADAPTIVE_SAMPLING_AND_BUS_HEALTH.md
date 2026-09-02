# ADAPTIVE_SAMPLING_AND_BUS_HEALTH.md — AutoDiag-WiCAN-Pro

Návrhový dokument pro dvě propojené oblasti:

- **(A)** adaptivní vzorkovací algoritmus pro AUTO TEST / PRE-PURCHASE
  scénáře, včetně zobrazení aktuální hodnoty samplingu uživateli,
  ručního override s bezpečnými mezemi a referenčních transportních limitů
- **(B)** zpracování CAN bus error frames a Bus Health panel

**Status:** design proposal (ne implementace).  
**Audit note:** Navazuje na principy z `AI_CONTEXT.md`, `AI_HANDOFF.md`,
`docs/AUTO_TEST_SPEC.md`, `docs/DIAGNOSTIC_BUS_HEALTH.md` a
`docs/CAPABILITY_DISCOVERY.md`. Před implementací ověř proti aktuálnímu
`main` (`Elm327Session`, `ConnectionViewModel`, `SimulatorWiCanTransport`).
PR #3 řeší capability discovery path, ne adaptive sampling ani TEC/REC.

---

## A. Adaptive Sampling

### A1. Princip

Vzorkovací frekvence **není pevná konstanta**. Odvozuje se ze:

1. stavu testu (REST / TRANSITIONING / LOAD / CHARGE / RECOVERY)
2. třídy sledované veličiny
3. reálné schopnosti transportu / ECU / WiCAN

Cíl: hustší log tam, kde se něco děje (přechody, rychlé změny proudu),
řidší v ustáleném stavu — **bez předstírání přesnosti, kterou systém nemá**.

Horní strop frekvence per signál se ověřuje při Capability Discovery.
Pokud transport/ECU nestíhá, `effectiveHz` klesá a UI to **nesmí skrývat**.

### A2. Sampling profily (třídy veličin)

| Třída  | Příklad veličin | Základní frekvence (cíl) | Poznámka |
|--------|-----------------|---------------------------|----------|
| SLOW   | teplota baterie, okolí, SOH (pokud je) | 0.1–0.5 Hz | pomalé změny |
| MEDIUM | SOC, pack napětí, teplota modulu | 1–2 Hz | výchozí battery telemetrie |
| FAST   | proud HV (pack), napětí článků při zátěži, **proud/výkon trakčního motoru (drive unit)** | 5–10 Hz | citlivé na dynamiku; drive unit jen pokud vozidlo/ECU údaj skutečně poskytne — jinak `UNAVAILABLE`, ne dopočítáno z pack current |
| BURST  | dočasně zvýšená frekvence při detekci přechodu | 2–5× základní třídy | časově omezené okno |

Konkrétní Hz jsou **cíle**, ne garance. Reálná hodnota je `effectiveHz`.

**Drive unit vs. pack current:**  
- Pack current = proud z/do HV baterie (celkový).  
- Drive unit / motor current & power = proud/výkon do pohonu.  
Rozdíl může zahrnovat DC-DC, topení, klimatizaci atd. Appka je **nesmí** zaměňovat ani odvozovat jedno z druhého bez explicitního, verified mapování.

### A3. Stavový model testu

```text
RESTING
  sampling: SLOW/MEDIUM podle třídy
  trigger → TRANSITIONING:
    detekovaná |ΔI/Δt| nebo |ΔV/Δt| > noise-floor threshold
    NEBO uživatelsky zahájený CHARGE/LOAD scénář

TRANSITIONING  (krátký, řádově sekundy)
  sampling: BURST na relevantních třídách
  účel: zachytit dynamiku přechodu (včetně drive unit, pokud AVAILABLE)

LOAD / CHARGE  (ustálený zátěžový/nabíjecí stav)
  sampling: FAST pro proud/napětí/drive unit, MEDIUM pro SOC/teplotu
  přechod zpět do TRANSITIONING při detekci ukončení zátěže/nabíjení

RECOVERY
  sampling: FAST na začátku (relaxace napětí),
            postupně snižovat k MEDIUM/SLOW jak se stabilizuje
  ukončení: hodnoty v pásmu stability po `recovery_stable_window`
            → návrat do RESTING
```

Fáze se **přeskočí** jako `NOT_AVAILABLE`, pokud Capability Discovery
nebo aktuální spojení neumožní potřebná data. Přeskočená fáze ≠ FAIL.

### A4. Detekce přechodu (trigger pro BURST)

Přechod se detekuje z **rychlosti změny**, ne z pevného univerzálního
prahu v ampérech:

- Na začátku RESTING se kalibruje `noiseFloor` z prvních vzorků.
- Trigger: `|ΔI/Δt|` nebo `|ΔV/Δt|` překročí multiplikátor baseline šumu.
- Konzistentní s pravidlem AI_CONTEXT: žádné univerzální thresholdy
  vydávané za OEM limity.

### A5. Zobrazení aktuální hodnoty samplingu uživateli

Uživatel vidí, **jak často se právě loguje** — ne jen výsledná data.

```text
┌──────────────────────────────────────┐
│  Sampling: 8.2 Hz  ▲ BURST           │
│  pack I: 8 Hz | motor: 8 Hz | SOC: 1 │
└──────────────────────────────────────┘
```

Požadavky:

- Zobrazit **efektivní** frekvenci (reálně dosaženou z timestamp diff),
  ne pouze cílovou.
- Barevně/textově odlišit stav (RESTING = klidová, BURST = zvýrazněná).
- Volitelně rozpad podle třídy signálu, pokud běží současně různé Hz.
- V exportovaném logu/replay uložit skutečný interval u každého vzorku
  (timestamp diff), aby replay věrně ukázal hustotu.

Tooltip: `id: ui_sampling_rate_indicator` (viz `help_content_schema.md`).

### A6. Datový model (návrh)

```kotlin
enum class SamplingClass { SLOW, MEDIUM, FAST, BURST }

enum class TestState {
    RESTING, TRANSITIONING, LOAD, CHARGE, RECOVERY
}

enum class SamplingMode { AUTOMATIC, MANUAL_OVERRIDE }

data class SamplingState(
    val testState: TestState,
    val perSignalClass: Map<SignalId, SamplingClass>,
    val effectiveHz: Map<SignalId, Double>,  // reálně dosažená
    val burstUntil: Instant?,
    val noiseFloor: Map<SignalId, Double>,
    val mode: SamplingMode = SamplingMode.AUTOMATIC
)
```

`effectiveHz` = klouzavý průměr z intervalu posledních N timestampů
daného signálu. To je hodnota pro UI i pro log metadata.

### A7. Ruční override sampling rate (per test)

**Výchozí chování je vždy automatické** (A2–A4). Ruční override je
volitelná pokročilá možnost v nastavení **konkrétního** testu
(AUTO TEST / PRE-PURCHASE profil), defaultně sbalená — ne globální
přepínač.

**Zásadní pravidlo:** uživatel nesmí moct zadat frekvenci, kterou systém
prokazatelně neumí dodržet.

- U každého slideru/pole se zobrazuje **zašedlý (disabled) rozsah min–max**
  odvozený z ověřených limitů (Capability Discovery pro transport + ECU +
  signál), ne z odhadu.
- Hodnoty **mimo** ověřený rozsah jsou needitovatelné (disabled), ne jen
  „červeně po odeslání“ — k nesmyslu se uživatel vůbec nedostane.
- Pokud Discovery pro daný signál ještě neproběhla / je nekonzistentní:
  override je **needitovatelný** a zobrazí důvod (tooltip
  `sampling_override_disabled_reason`):
  *„Rozsah vzorkování zatím neznámý — proveď Capability Discovery
  nebo test se spuštěným vozidlem.“*
- Horní limit override **nikdy** nepřesáhne strop z Capability Discovery.
- Dolní limit je UX volba (typicky 0.1 Hz), ne technické omezení transportu.
- Override se ukládá **per vehicle/test profil**, ne globálně.
- V logu/reportu vždy: `automatic` vs `manual_override`, požadované Hz
  i `effectiveHz` (reprodukovatelnost, zákaz předstírání přesnosti).

```kotlin
data class SamplingRateOverride(
    val signalId: SignalId,
    val requestedHz: Double,
    val allowedRange: ClosedFloatingPointRange<Double>, // z Capability Discovery
    val isEditable: Boolean,
    val reasonIfDisabled: String?
)

data class TestSamplingProfile(
    val mode: SamplingMode, // AUTOMATIC (default) | MANUAL_OVERRIDE
    val overrides: Map<SignalId, SamplingRateOverride> = emptyMap(),
    val vehicleProfileId: String
)
```

### A8. Reálné rychlosti komunikace (transportní limity)

**Referenční rámec, ne garance.** Skutečné dosažitelné frekvence se vždy
ověřují Capability Discovery pro danou kombinaci firmware / vozidlo / ECU.
Tabulka = orientační strop pro výchozí zašedlé meze (A7), dokud neproběhne
reálné měření. Po měření UI používá naměřené hodnoty, ne tuto tabulku.

| Transport / rozhraní | Teoretický strop | Praktické omezení |
|----------------------|------------------|-------------------|
| ELM327 (TCP :3333, AT) | jednotky req/s na jeden PID | request/response, sériové; víc PIDů → nižší Hz na signál; závisí na FW a ECU |
| SLCAN / raw CAN (TCP :23) | limitováno CAN bitrate (typ. 500 kbit/s HS-CAN) | vyšší propustnost než ELM; appka sama mapuje CAN ID — žádné „PID zdarma“ |
| Wi-Fi TCP (WiCAN ↔ telefon) | lokální síť, řádově ms latence | slabé Wi-Fi / zátěž sítě snižuje `effectiveHz` — musí se projevit v UI |
| ECU odezva (OEM) | často pomalejší než transport | interní rate-limit ECU je často skutečný bottleneck |

**Zdroj pravdy pro konkrétní čísla:** výsledek Capability Discovery dané
session + naměřené `effectiveHz`, ne tato tabulka.

---

## B. CAN Bus Error Frame Handling

### B1. Proč odděleně

CAN chyby jsou **chyby komunikace**, ne diagnostický nález o vozidle.
Pokud se smíchají s `UNAVAILABLE` vehicle daty, uživatel nepozná,
zda auto údaj neposkytlo, nebo selhalo spojení.

Existující krátký dokument: `docs/DIAGNOSTIC_BUS_HEALTH.md`.
Tento soubor ho rozšiřuje; nenahrazuje ho.

### B2. Typy CAN chyb

| Typ        | Popis                                      | Appka |
|------------|--------------------------------------------|-------|
| Bit Error  | odeslaný bit ≠ přečtený na bus             | počítat, trend, ne jako DTC |
| Stuff Error| porušení bit-stuffing                      | totéž |
| CRC Error  | checksum nesedí                            | totéž, vyšší váha |
| Form Error | neplatný formát pevných polí               | totéž |
| ACK Error  | žádný uzel nepotvrdil                     | možné spící ECU / prázdný bus |
| Bus-Off    | uzel odpojen po TEC/REC limitu             | kritický — explicitní UI + recovery |

### B3. Error Counter model (TEC / REC)

Pokud WiCAN firmware **skutečně** zpřístupní TEC/REC (ověřit proti
meatpiHQ/wican-fw — **nepředpokládat**):

- zobrazit v Bus Health panelu (ne mezi vehicle diagnostics)
- sledovat trend
- stavy: `ERROR_ACTIVE` → `ERROR_PASSIVE` → `BUS_OFF`

Pokud firmware čítače **nedává**:

- odvodit z frekvence timeoutů / chybných odpovědí
- označit `source: DERIVED` (ne `MEASURED`)

### B4. Bus-Off Recovery

- Detekovat opakované/trvalé selhání (ne jeden timeout).
- **Nesmí** tiše zkoušet donekonečna bez informace uživateli.
- UI: např. „CAN sběrnice — ztráta komunikace, pokouším se o obnovení“.
- Recovery s backoff + limit pokusů → pak ruční zásah.
- Appka **nevydává** diagnostické/control příkazy pro „oprávu“ busu.

### B5. Odlišení od AUTO TEST výsledků

- Zobrazovat v samostatné **Bus Health** sekci.
- Bus-off během AUTO TEST / PRE-PURCHASE:
  - fáze = `NOT_AVAILABLE`
  - `reason: bus_error` (≠ `vehicle_did_not_provide`, `timeout`, …)
  - zbytek testu může pokračovat, pokud je bezpečné
  - report uvede, které části ovlivnila chyba sběrnice

### B6. Datový model (návrh)

```kotlin
enum class CanErrorType { BIT, STUFF, CRC, FORM, ACK, BUS_OFF }
enum class NodeErrorState { ERROR_ACTIVE, ERROR_PASSIVE, BUS_OFF }
enum class DataSource { MEASURED, DERIVED, OEM_REPORTED, INFERRED }

data class BusHealthSnapshot(
    val timestamp: Instant,
    val nodeState: NodeErrorState,
    val tec: Int?,
    val rec: Int?,
    val errorCounts: Map<CanErrorType, Int>,
    val source: DataSource
)

enum class NotAvailableReason {
    VEHICLE_DID_NOT_PROVIDE,
    BUS_ERROR,
    TIMEOUT,
    CAPABILITY_NOT_DISCOVERED,
    TRANSPORT_LIMIT
}
```

---

## Návaznost na existující dokumenty

| Dokument | Vztah |
|----------|--------|
| `docs/DIAGNOSTIC_BUS_HEALTH.md` | základní Bus Health view — tento doc ho rozšiřuje |
| `docs/AUTO_TEST_SPEC.md` | fáze REST/LOAD/RECOVERY/CHARGE |
| `docs/AUTO_TEST_RESULT_CONTRACT.md` | `NOT_AVAILABLE` + reason |
| `docs/CAPABILITY_DISCOVERY.md` | strop Hz a dostupné signály (včetně drive unit, pokud existuje) |
| `docs/AUTOMATION_ENGINE.md` | session phases, dry-run/replay |
| `AI_HANDOFF.md` | adaptive sampling / HV test planned |
| `FEATURE_PROPOSALS.md` | D5 — pack current + drive unit current/power |
| `help_content_schema.md` | `ui_sampling_rate_indicator`, `sampling_override_disabled_reason`, `sampling_mode_toggle` |
| `docs/SIMULATOR_TEST_SCENARIOS.md` | happy-path před jakoukoli sampling implementací |

---

## Implementační pořadí (návrh)

1. `assembleDebug` + simulátor end-to-end (`SIMULATOR_TEST_SCENARIOS.md`).
2. Mode 01 value parser + DTC presence (bez falešných hodnot).
3. Bus Health panel z toho, **co WiCAN skutečně reportuje**.
4. `SamplingState` + `effectiveHz` indikátor (i fixed poll nejdřív).
5. AUTO TEST orchestrace s `NOT_AVAILABLE` + `reason`.
6. Adaptive BURST / noise-floor; ruční override (A7) až po ověřených mezích z Discovery.
7. Drive unit current/power — **jen** po verified vehicle mapping; jinak `UNAVAILABLE`.

---

## Co tento dokument **není**

- Není ověřená implementace proti hardwaru.
- Není seznam Tesla CAN ID ani mV prahů.
- Nepředpokládá, že WiCAN firmware dává TEC/REC.
- Neobsahuje WRITE / bus-recovery příkazy do vozidla.
- Nenahrazuje Capability Discovery — stropy Hz bez Discovery se v UI **neodemykají**.

*Aktualizuj při změně stavu implementace nebo po ověření proti reálnému WiCAN PRO firmware.*
