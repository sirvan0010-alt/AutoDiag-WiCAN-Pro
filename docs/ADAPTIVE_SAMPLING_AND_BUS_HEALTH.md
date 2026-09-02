# ADAPTIVE_SAMPLING_AND_BUS_HEALTH.md — AutoDiag-WiCAN-Pro

Návrhový dokument pro dvě propojené oblasti: (A) adaptivní vzorkovací
algoritmus pro AUTO TEST / PRE-PURCHASE scénáře, včetně zobrazení aktuální
hodnoty samplingu uživateli, a (B) zpracování CAN bus error frames.

**Poznámka k ověření:** Designový návrh navazující na `AI_CONTEXT.md` /
`ROADMAP.md` / `FEATURE_PROPOSALS.md`. Před implementací ověř proti
`Elm327Session`, `ConnectionViewModel` a simulátoru.

> **Status: design proposal, ne implementace.** Nic z tohoto dokumentu
> (Adaptive Sampling ani Bus Health) není zatím v kódu.
> Doporučené pořadí: (1) zelený simulátor end-to-end, (2) ověření proti
> reálnému WiCAN PRO, (3) teprve pak implementace B13/Calibration Test —
> ne naslepo podle této specifikace.

---

## A0. Jak sampling technicky funguje (transportní řetězec)

```text
Vozidlo (ECU) ←→ CAN sběrnice (500 kbit/s HS-CAN, sdílená)
     ↕
   WiCAN (OBD port)
     ↕  Wi-Fi TCP
   Telefon / PC (appka)
```

- **ELM327 (TCP :3333)** — request/response, sériové dotazy. Appka rozhoduje,
  na co se ptá a jak často (`SamplingClass`).
- **SLCAN/raw (TCP :23)** — raw CAN transport. Protokol může být obousměrný,
  ale **AutoDiag v READ-first fázi ho používá výhradně pro příjem/logování**
  (pasivní příjem provozu, který ECU vysílá). Rozlišení READ-only *použití*
  vs. schopnost transportu je důležité pro budoucí oddělení READ/WRITE vrstev.

Reálný strop je **proměnlivý** (počet PID, ECU, Wi-Fi). `effectiveHz` měří
skutečnost; ruční override (A7) potřebuje změřené meze — viz **A9**.

---

## A. Adaptive Sampling

### A1. Princip

Frekvence není konstanta: stav testu + třída signálu + reálný transport/ECU.
Hustší log při přechodech, řidší v ustáleném stavu — bez předstírání přesnosti.

### A2. Sampling profily

| Třída | Příklady | Cíl Hz | Poznámka |
|-------|----------|--------|----------|
| SLOW | teplota, SOH | 0.1–0.5 | pomalé změny |
| MEDIUM | SOC, pack V, teplota modulu | 1–2 | výchozí battery |
| FAST | pack I, cell V při zátěži, **drive unit I/P** | 5–10 | drive unit jen pokud ECU poskytne → jinak UNAVAILABLE |
| BURST | při detekci přechodu | 2–5× třídy | časově omezené |

### A3. Stavový model

RESTING → TRANSITIONING (BURST) → LOAD/CHARGE → RECOVERY → RESTING.
Trigger přechodu: `|ΔI/Δt|` / `|ΔV/Δt|` nad noise floor z RESTING (ne fixní práh).

### A5. UI indikátor

Zobrazit **effectiveHz** (reálně dosažená), ne jen cílovou. Log: timestamp diff.

### A6–A7. Datový model + ruční override

Default `AUTOMATIC`. Override per test/profil; zašedlé min–max z Discovery/kalibrace.
Bez měření needitovatelné. Horní limit nikdy nad ověřený strop.

### A8. Transportní limity

Orientační tabulka ELM / SLCAN / Wi-Fi / ECU — **ne garance**. Zdroj pravdy:
Capability Discovery + `maxStableHz` z A9.

### A9. Sampling Calibration Test

Aktivní test: vozidlo + WiCAN + síť → nakrmí A7 meze a A2 defaulty.

**Cíl: maximum stable Hz, ne maximum observed Hz** — nejvyšší frekvence,
při které komunikace **zůstává spolehlivá**, ne frekvence, kde ještě přišla
alespoň jedna odpověď. Test není „zasypání ECU“.

Metodika explicitně sleduje:

- timeout rate
- response latency
- jitter
- dropped/missing samples
- počet po sobě jdoucích úspěšných vzorků
- stabilizační interval
- **hystereze** — hranice s rezervou **pod** bodem selhání (`hysteresisMarginHz`)

Měří odděleně SLOW/MEDIUM/FAST. Výsledek per vehicle + WiCAN FW, ne globálně.

**Android:** foreground service + notifikace; nespouštět automaticky;
částečný výsledek = platný (`PARTIAL`/`INTERRUPTED`).

**Bezpečnost:** jen READ; postupné navyšování; stop při bus chybě /
`ERROR_PASSIVE` (B3) — kalibrace nesmí způsobit `BUS_OFF`.

```kotlin
data class CalibrationResult(
    val vehicleProfileId: String,
    val wicanFirmwareVersion: String,
    val timestamp: Instant,
    val perClassMaxStableHz: Map<SamplingClass, Double>,  // stable, ne observed
    val perSignalMaxStableHz: Map<SignalId, Double>,
    val methodology: CalibrationMethodology,
    val completeness: CalibrationCompleteness, // FULL | PARTIAL | INTERRUPTED
    val networkConditionNote: String?
)

data class CalibrationMethodology(
    val timeoutRateThreshold: Double,
    val jitterThresholdMs: Double,
    val requiredConsecutiveSuccesses: Int,
    val stabilizationWindowMs: Long,
    val hysteresisMarginHz: Double
)
```

`perClassMaxStableHz` / `perSignalMaxStableHz` plní `allowedRange` v A7.

---

## B. CAN Bus Error Frame Handling

### B1–B2

Bus chyby ≠ vehicle DTC. Typy: Bit, Stuff, CRC, Form, ACK, Bus-Off.
Zobrazit v **Bus Health**, ne mezi DTC.

### B3. Error Counter model (TEC/REC)

Hardwarové/FW čítače dle ISO 11898. Appka je **nepočítá** — jen **čte**,
pokud WiCAN FW zpřístupní (ověřit proti meatpiHQ/wican-fw — **nepředpokládat**).

**Změny (zjednodušeně):** úspěch → −1; chyba TX → TEC +8 typicky; RX → REC.
Růst asymetrický.

**Stavy uzlu (standardní prahy):**

```text
ERROR_ACTIVE   TEC < 128  a  REC < 128
ERROR_PASSIVE  TEC ≥ 128  nebo  REC ≥ 128  (ještě ne BUS_OFF)
BUS_OFF        TEC ≥ 256
```

REC samo o sobě **nevede** na BUS_OFF (jen TEC ≥ 256).

| Situace | V appce |
|---------|---------|
| FW dává TEC/REC | `source: MEASURED`, konkrétní hodnoty |
| FW nedává | `source: DERIVED`, `tec`/`rec` = `null`, stav z timeoutů/CRC/ACK |

Appka **nesmí** čísla vymýšlet. Žádný user-facing „reset TEC/REC“ (recovery
na straně kontroléru / restart adaptéru, ne WRITE do vozidla).

Primárně ukazovat stav uzlu; TEC/REC čísla volitelně jako expert detail.
Trend v čase > jednorázová hodnota.

### B4. Bus-Off recovery

Explicitní UI; backoff + limit pokusů; žádné tiché nekonečné opakování.

### B5. AUTO TEST

Bus chyba během fáze → `NOT_AVAILABLE` + `reason: bus_error`
(≠ `vehicle_did_not_provide`). Report uvede ovlivněné části.

### B6. Datový model

```kotlin
enum class CanErrorType { BIT, STUFF, CRC, FORM, ACK, BUS_OFF }
enum class NodeErrorState { ERROR_ACTIVE, ERROR_PASSIVE, BUS_OFF }

data class BusHealthSnapshot(
    val timestamp: Instant,
    val nodeState: NodeErrorState,
    val tec: Int?,  // null pokud FW neposkytuje
    val rec: Int?,
    val errorCounts: Map<CanErrorType, Int>,
    val source: DataSource  // MEASURED | DERIVED
)
```

---

## Návaznost

- `help_content_schema.md` — tooltips sampling + `action_sampling_calibration_test`
- `FEATURE_PROPOSALS.md` B13 — Calibration Test
- `CAPABILITY_DISCOVERY.md`, `AUTO_TEST_SPEC.md`, `DIAGNOSTIC_BUS_HEALTH.md`
- `AI_HANDOFF.md` / ROADMAP Phase 2/7

## Implementační pořadí

1. assembleDebug + simulátor
2. Mode 01 / DTC parser
3. Bus Health z reálného WiCAN
4. effectiveHz indikátor
5. Calibration (A9) na hardwaru
6. A7 z maxStableHz
7. AUTO TEST + adaptive BURST
8. Drive unit jen verified AVAILABLE

*Design only. Žádné fake AVAILABLE. Žádný WRITE.*
