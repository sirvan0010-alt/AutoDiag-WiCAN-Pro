# ADAPTIVE_SAMPLING_AND_BUS_HEALTH.md — AutoDiag-WiCAN-Pro

Návrhový dokument pro dvě propojené oblasti:

- **(A)** adaptivní vzorkovací algoritmus pro AUTO TEST / PRE-PURCHASE
  scénáře, včetně zobrazení aktuální hodnoty samplingu uživateli
- **(B)** zpracování CAN bus error frames a Bus Health panel

**Status:** design proposal (ne implementace).  
**Audit note:** Dokument navazuje na principy z `AI_CONTEXT.md`,
`AI_HANDOFF.md`, `docs/AUTO_TEST_SPEC.md`, `docs/DIAGNOSTIC_BUS_HEALTH.md`
a `docs/CAPABILITY_DISCOVERY.md`. Před implementací ověř proti aktuálnímu
`main` (Elm327Session, ConnectionViewModel, SimulatorWiCanTransport) a
otevřenému PR #3 — ten řeší capability discovery path, ne adaptive sampling
ani TEC/REC model.

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

| Třída  | Příklad veličin                          | Základní frekvence (cíl) | Poznámka |
|--------|------------------------------------------|---------------------------|----------|
| SLOW   | teplota baterie, okolí, SOH (pokud je)   | 0.1–0.5 Hz                | pomalé změny |
| MEDIUM | SOC, pack napětí, teplota modulu         | 1–2 Hz                    | výchozí battery telemetrie |
| FAST   | proud HV, cell napětí při zátěži         | 5–10 Hz                   | citlivé na dynamiku |
| BURST  | dočasně zvýšená frekvence při přechodu   | 2–5× základní třídy       | časově omezené okno |

Konkrétní Hz jsou **cíle**, ne garance. Reálná hodnota je `effectiveHz`.

### A3. Stavový model testu

```text
RESTING
  sampling: SLOW/MEDIUM podle třídy
  trigger → TRANSITIONING:
    detekovaná |ΔI/Δt| nebo |ΔV/Δt| > noise-floor threshold
    NEBO uživatelsky zahájený CHARGE/LOAD scénář

TRANSITIONING  (krátký, řádově sekundy)
  sampling: BURST na relevantních třídách
  účel: zachytit dynamiku přechodu

LOAD / CHARGE  (ustálený zátěžový/nabíjecí stav)
  sampling: FAST pro proud/napětí, MEDIUM pro SOC/teplotu
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

UI indikátor (např. malý prvek v rohu live-data / test obrazovky):

```text
┌──────────────────────────────┐
│  Sampling: 8.2 Hz  ▲ BURST   │
│  proud HV: 8 Hz | SOC: 1 Hz  │
└──────────────────────────────┘
```

Požadavky:

- Zobrazit **efektivní** frekvenci (reálně dosaženou z timestamp diff),
  ne pouze cílovou.
- Barevně/textově odlišit stav (RESTING = klidová, BURST = zvýrazněná).
- Volitelně rozpad podle třídy signálu, pokud běží současně různé Hz.
- V exportovaném logu/replay uložit skutečný interval u každého vzorku
  (timestamp diff), aby replay věrně ukázal hustotu.

Tooltip pro indikátor: `id: ui_sampling_rate_indicator`
(doplnit do `help_content_schema.md`).

### A6. Datový model (návrh)

```kotlin
enum class SamplingClass { SLOW, MEDIUM, FAST, BURST }

enum class TestState {
    RESTING, TRANSITIONING, LOAD, CHARGE, RECOVERY
}

data class SamplingState(
    val testState: TestState,
    val perSignalClass: Map<SignalId, SamplingClass>,
    val effectiveHz: Map<SignalId, Double>,  // reálně dosažená
    val burstUntil: Instant?,
    val noiseFloor: Map<SignalId, Double>
)
```

`effectiveHz` = klouzavý průměr z intervalu posledních N timestampů
daného signálu. To je hodnota pro UI i pro log metadata.

---

## B. CAN Bus Error Frame Handling

### B1. Proč odděleně

CAN chyby jsou **chyby komunikace**, ne diagnostický nález o vozidle.
Pokud se smíchají s `UNAVAILABLE` vehicle daty, uživatel nepozná,
zda auto údaj neposkytlo, nebo selhalo spojení (rušení, terminace,
transceiver, spící ECU).

Existující krátký dokument: `docs/DIAGNOSTIC_BUS_HEALTH.md`.
Tento soubor ho rozšiřuje o error types, TEC/REC model a recovery politiku.
Ne nahrazuje ho.

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

Pokud WiCAN firmware **skutečně** zpřístupní Transmit/Receive Error Counter
(ověřit proti meatpiHQ/wican-fw dokumentaci — **nepředpokládat**):

- zobrazit v Bus Health panelu (ne mezi vehicle diagnostics)
- sledovat trend
- stavy uzlu: `ERROR_ACTIVE` → `ERROR_PASSIVE` → `BUS_OFF`

Pokud firmware čítače **nedává**:

- odvodit nepřímo z frekvence timeoutů / chybných odpovědí
- označit jako `source: DERIVED` (ne `MEASURED`)
- konzistentní s modelem measured / OEM-reported / calculated / inferred
  z `AI_CONTEXT.md`

### B4. Bus-Off Recovery

- Detekovat opakované/trvalé selhání (ne jeden timeout).
- **Nesmí** tiše zkoušet donekonečna bez informace uživateli.
- UI stav: např. „CAN sběrnice — ztráta komunikace, pokouším se o obnovení“.
- Recovery s backoff + horní limit pokusů → pak vyžadovat ruční zásah.
- Appka **nevydává** diagnostické/control příkazy pro „oprávu“ busu
  (viz scope v `DIAGNOSTIC_BUS_HEALTH.md`).

### B5. Odlišení od AUTO TEST výsledků

Bus-level chyby ≠ diagnostický nález o vozidle.

- Zobrazovat v samostatné **Bus Health** sekci.
- Pokud bus-off přeruší AUTO TEST / PRE-PURCHASE fázi:
  - fáze = `NOT_AVAILABLE`
  - `reason: bus_error` (odlišeno od `vehicle_did_not_provide`, `timeout`, …)
  - zbytek testu může pokračovat, pokud je to bezpečné
  - závěrečný report jasně uvede, které části byly ovlivněny bus chybou

### B6. Datový model (návrh)

```kotlin
enum class CanErrorType { BIT, STUFF, CRC, FORM, ACK, BUS_OFF }
enum class NodeErrorState { ERROR_ACTIVE, ERROR_PASSIVE, BUS_OFF }

enum class DataSource { MEASURED, DERIVED, OEM_REPORTED, INFERRED }

data class BusHealthSnapshot(
    val timestamp: Instant,
    val nodeState: NodeErrorState,
    val tec: Int?,                 // null pokud firmware neposkytuje
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

// TestPhaseResult rozšířit o:
// status: OK | NOT_AVAILABLE | PARTIAL
// notAvailableReason: NotAvailableReason?
```

---

## Návaznost na existující dokumenty

| Dokument | Vztah |
|----------|--------|
| `docs/DIAGNOSTIC_BUS_HEALTH.md` | základní Bus Health view — tento doc ho **rozšiřuje**, neduplikuje |
| `docs/AUTO_TEST_SPEC.md` | fáze REST/LOAD/RECOVERY/CHARGE — adaptive sampling je jejich orchestrace |
| `docs/AUTO_TEST_RESULT_CONTRACT.md` | `NOT_AVAILABLE` + reason patří do výsledkového kontraktu |
| `docs/CAPABILITY_DISCOVERY.md` | horní limit Hz a dostupné signály zjišťovat zde |
| `docs/AUTOMATION_ENGINE.md` | session phases, dry-run/replay |
| `AI_HANDOFF.md` | adaptive sampling + bus health jsou v CURRENT TASK / planned |
| `FEATURE_PROPOSALS.md` | D5 / AUTO TEST — odkaz sem, ne kopírovat text |
| `help_content_schema.md` | přidat `ui_sampling_rate_indicator` + bus-health tooltips |

---

## Implementační pořadí (návrh)

1. Lokální `assembleDebug` + simulátor end-to-end (už doporučeno v AI_HANDOFF).
2. Mode 01 value parser + DTC presence (bez falešných hodnot).
3. Bus Health panel na základě toho, **co WiCAN skutečně reportuje**
   (error frames, bus-off flag) — TEC/REC jen pokud firmware dává.
4. SamplingState + effectiveHz indikátor v UI (i pro jednoduchý fixed poll
   nejdřív; adaptive logika až s time-series store).
5. Orchestrace AUTO TEST fází s `NOT_AVAILABLE` + `reason`.
6. Teprve pak plný adaptive BURST / noise-floor kalibrace.

---

## Co tento dokument **není**

- Není ověřená implementace proti hardwaru.
- Není seznam Tesla CAN ID ani mV prahů.
- Nepředpokládá, že WiCAN firmware dává TEC/REC — to se musí ověřit.
- Neobsahuje WRITE / bus-recovery příkazy směrem do vozidla.

*Aktualizuj při změně stavu implementace nebo po ověření proti reálnému
WiCAN PRO firmware.*
