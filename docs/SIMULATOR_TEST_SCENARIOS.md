# SIMULATOR_TEST_SCENARIOS.md — AutoDiag-WiCAN-Pro

Checklist a očekávané chování pro end-to-end test **Simulátoru** proti aktuálnímu `main`.

**Účel:** ověřit transport → ELM session → Capability Discovery → UI fáze  
**bez** falešných vehicle dat (SOC, cells, Riso, Tesla CAN).

Odpovídá kódu:

- `SimulatorWiCanTransport`
- `Elm327Session` (buffer do `>`)
- `CapabilityDiscovery` (ATI, ATDP, 0902, 03, 010C)
- `ConnectionViewModel` (CONNECTING → INITIALIZING_ELM → DISCOVERING → READY)

---

## Happy path

UI: tlačítko **Simulátor** → `connectSimulator()`.

| Fáze UI | Co běží | Očekávaná odpověď simulátoru | Očekávaný capability stav |
|---------|---------|------------------------------|---------------------------|
| CONNECTING | `SimulatorWiCanTransport.connect()` | okamžitě CONNECTED | — |
| INITIALIZING_ELM | `ATZ`, `ATE0`, `ATL0`, `ATH1`, `ATSP0` | `ELM327 v1.5` / `OK` + `>` | — |
| DISCOVERING | `ATI` | `ELM327 v1.5` | COMMUNICATION = AVAILABLE |
| | `ATDP` | `AUTO, ISO 15765-4 (CAN 11/500)` | OBD_PROTOCOL = AVAILABLE |
| | `0902` | `49 02 01 SIMTEST0AUTODIAG01` | OBD_VIN = **PARTIAL** (viz edge case) |
| | `03` | `43 00` | OBD_MODE_03 = AVAILABLE (presence only) |
| | `010C` | `41 0C 00 00` | OBD_MODE_01 = AVAILABLE (presence only) |
| READY | snapshot v UI | — | žádný ERROR; žádné EV hodnoty |

**Pravidlo:** `010C` vrací raw bytes. UI **nesmí** zobrazit RPM ani jinou dekódovanou hodnotu — jen presence Mode 01.

---

## Edge cases

### 1. VIN formát (současný simulátor)

Simulátor posílá `SIMTEST0AUTODIAG01`.  
`extractVin()` vyžaduje `[A-HJ-NPR-Z0-9]{17}` — písmeno **I** je neplatné → VIN = `null` → status **PARTIAL**, ne AVAILABLE.

Pokud UI ukáže plně AVAILABLE VIN, je to bug.

### 2. Chunked odpověď (buffer do `>`)

`Elm327Session` skládá TCP chunky až do `>`.  
Simulátor dnes emituje celou odpověď najednou. Pro robustnost (unit test / pozdější úprava simulátoru):

- emitovat např. `41 0C` a v dalším kroku `00 00\r\n>`
- session nesmí ukončit předčasně ani vrátit truncated body

### 3. `NO DATA`

Neznámý příkaz → simulátor vrací `NO DATA`.  
`looksLikeNoData` → UNAVAILABLE / PARTIAL podle probe — **ne** ERROR a **ne** vymyšlená hodnota.

### 4. Timeout

Bez emitovaného `>` → `TimeoutCancellationException` → fáze **ERROR** + humanized message (časový limit / izolace / IP).

### 5. SLCAN link-only

`connectSlcan` → `runDiscovery = false` → READY **bez** CapabilityDiscovery.  
UI: link-only, **ne** „OBD AVAILABLE jen proto, že TCP funguje“.

### 6. Opakovaný connect / disconnect

Simulátor → READY → Disconnect → IDLE → znovu Simulátor → celá sekvence bez crash a bez stale snapshot.

### 7. Neznámý AT příkaz

`ATFOO` → simulátor `OK` (větev `startsWith("AT")`). Discovery to nevolá; vhodný unit test transportu.

---

## Minimální checklist po `./gradlew :app:assembleDebug`

```text
[ ] APK se sestaví bez chyby
[ ] Simulátor: CONNECTING → INITIALIZING_ELM → DISCOVERING_CAPABILITIES → READY
[ ] COMMUNICATION / OBD_PROTOCOL / OBD_MODE_03 / OBD_MODE_01 = AVAILABLE (nebo PARTIAL u VIN)
[ ] VIN není prezentován jako plně dekódovaný 17znakový VIN (PARTIAL je OK)
[ ] Žádné SOC / cell voltage / Riso / Tesla hodnoty na obrazovce
[ ] Text „Vozidlo údaj neposkytlo“ se neobjeví u AVAILABLE probes
[ ] Disconnect → IDLE
[ ] (volitelně) unit test: Elm327Session skládá odpověď přes 2 chunky
```

---

## Co v tomto kroku **nedělat**

Tyto položky jsou záměrně odložené, dokud není zelený happy-path výše:

1. **Rozšiřovat simulátor o fake cell voltage / SOC / Riso**  
   → až po ověřeném presence path a teprve s explicitním označením *synthetic / for UI layout only*, nikdy jako AVAILABLE vehicle data.

2. **Měnit CapabilityDiscovery na „AVAILABLE protože TCP funguje“ u SLCAN**  
   → SLCAN zůstává link-only. OBD AVAILABLE jen po úspěšných OBD probes.

3. **Implementovat adaptive sampling engine**  
   → až po zeleném buildu + simulátoru, Mode 01 value parseru (verified mapování) a time-series store. Spec: `docs/ADAPTIVE_SAMPLING_AND_BUS_HEALTH.md`.

---

## Kdy se odložené položky smí dělat

```text
1. assembleDebug + simulátor happy-path (tento dokument)     ← TEĎ
2. Mode 01 value parser (jen verified OBD mapování)
3. DTC parser z Mode 03 (presence už je; dekódování kódů)
4. Help content napojený na capability ID
5. Bus Health panel z toho, co WiCAN skutečně reportuje
6. Adaptive sampling + AUTO TEST orchestrace
7. Synthetic EV data v simulátoru — pouze s jasným „synthetic“ flag
   a nikdy jako výchozí AVAILABLE pro reálné vozidlo
```

Pořadí respektuje `AI_HANDOFF.md` a `AI_CONTEXT.md`:  
READ first, žádné falešné AVAILABLE, simulator/replay před reálným vozidlem.

---

## Related

- `docs/ADAPTIVE_SAMPLING_AND_BUS_HEALTH.md`
- `docs/CAPABILITY_DISCOVERY.md`
- `docs/AUTO_TEST_SPEC.md`
- `AI_HANDOFF.md` — CURRENT TASK
