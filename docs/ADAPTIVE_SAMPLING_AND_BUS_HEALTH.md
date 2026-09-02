# ADAPTIVE_SAMPLING_AND_BUS_HEALTH.md — AutoDiag-WiCAN-Pro

Návrhový dokument pro (A) adaptivní sampling + (B) CAN bus error / Bus Health.

**Status:** design proposal (ne implementace).

---

## A. Adaptive Sampling

### A0. Jak sampling technicky funguje (transportní řetězec)

```text
Vozidlo (ECU) ←→ CAN (typ. 500 kbit/s HS-CAN, sdílená)
     ↕
   WiCAN (OBD)
     ↕  Wi-Fi TCP
   Telefon (appka)
```

| Režim | Port | Chování | Co řídí sampling |
|-------|------|---------|------------------|
| **ELM327** | :3333 | Request/response, sériové dotazy | Jak často se appka **ptá** (`SamplingClass`) |
| **SLCAN** | :23 | Pasivní odposlech busu | Co appka **ukládá** z příchozího provozu |

Strop je **proměnlivý** (počet PID, ECU odezva, Wi-Fi). `effectiveHz` měří skutečnost. Ruční meze (A7) vyžadují změření — **A9**.

### A1. Princip

Frekvence není konstanta: stav testu + třída signálu + reálný transport/ECU.
Bez předstírání přesnosti. Strop z Capability Discovery / kalibrace.

### A2. Třídy

| Třída | Příklady | Cíl Hz |
|-------|----------|--------|
| SLOW | teplota, SOH | 0.1–0.5 |
| MEDIUM | SOC, pack V, teplota modulu | 1–2 |
| FAST | pack I, cell V při zátěži, **drive unit I/P** | 5–10 |
| BURST | při přechodu | 2–5× třídy |

Drive unit jen pokud ECU poskytne → jinak `UNAVAILABLE` (ne odhad z pack I).

### A3–A4. REST → TRANSITIONING → LOAD/CHARGE → RECOVERY; trigger `|ΔI/Δt|` z noise floor.

### A5. UI: efektivní Hz (ne cílová); log timestamp diff.

### A6. `SamplingState` + `SamplingMode` AUTOMATIC | MANUAL_OVERRIDE.

### A7. Ruční override per test

Default automatic. Slider: **zašedlé** min–max z Discovery/kalibrace. Bez měření needitovatelné. Horní limit nikdy nad ověřený strop.

### A8. Orientační tabulka ELM / SLCAN / Wi-Fi / ECU (ne garance).

### A9. Sampling Calibration Test

Aktivní měření dosažitelné frekvence pro vozidlo+WiCAN+síť.

- ELM: postupné navyšování frekvence dotazů; stop při odchylce/timeoutu.
- SLCAN: měření příchozí frekvence mapovaných ID.
- **Foreground service** + notifikace (ne tichý background).
- Nespouštět automaticky bez uživatele.
- Pouze READ; postupná zátěž; stop při bus chybě (sekce B).
- `CalibrationResult.perClassMaxStableHz` → `allowedRange` v A7.
- Timestamp + WiCAN FW verze; po FW update navrhnout re-kalibraci.

```kotlin
data class CalibrationResult(
    val vehicleProfileId: String,
    val wicanFirmwareVersion: String,
    val timestamp: Instant,
    val perClassMaxStableHz: Map<SamplingClass, Double>,
    val perSignalMaxStableHz: Map<SignalId, Double>,
    val completeness: CalibrationCompleteness, // FULL|PARTIAL|INTERRUPTED
    val networkConditionNote: String?
)
```

---

## B. CAN Bus Error Frame Handling

Bus chyby ≠ vehicle DTC. Panel Bus Health. TEC/REC jen pokud FW dává, jinak DERIVED.
Bus-off: UI + backoff recovery; fáze testu `NOT_AVAILABLE reason: bus_error`.

---

## Implementační pořadí

1. assembleDebug + simulátor (`SIMULATOR_TEST_SCENARIOS.md`)
2. Mode 01 / DTC parser
3. Bus Health z reálného WiCAN
4. effectiveHz indikátor
5. **Calibration Test (A9)** na hardwaru
6. A7 override z CalibrationResult
7. AUTO TEST + adaptive BURST
8. Drive unit jen verified AVAILABLE

---

*Design only. Žádné fake AVAILABLE. Žádný WRITE.*
