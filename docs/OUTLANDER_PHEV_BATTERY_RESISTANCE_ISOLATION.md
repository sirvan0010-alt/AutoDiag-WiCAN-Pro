# Mitsubishi Outlander PHEV — battery resistance and isolation diagnostics

## Purpose

This document defines how AutoDiag should present battery resistance and electrical-isolation information for the Mitsubishi Outlander PHEV. The UI must distinguish:

1. **internal cell/module resistance** — a battery-condition measurement,
2. **HV isolation resistance** — electrical isolation of the high-voltage system from the vehicle/chassis reference,
3. **cell-voltage spread** — a voltage-balance indicator, not a resistance measurement.

The source material currently available is the reverse-engineered PHEV Watchdog APK. These mappings are **source-extracted and unverified on a real vehicle** until a request/response exchange is captured and the decoder is independently validated.

## 1. What is being measured?

### 1.1 Internal resistance

The Watchdog source exposes battery internal-resistance values as separate maximum/minimum measurements. In the extracted `e4.a` decoder they are named `CELL_MAX_INT_RESISTANCE` and `CELL_MIN_INT_RESISTANCE` and are decoded from single response bytes with a `/10` scale and a unit represented by the source as **MΩ**.

The application must explain this as an indication of the electrical resistance associated with the battery-cell/module system, not as the resistance of one externally accessible resistor. A higher resistance can be associated with greater internal losses and cell ageing, but AutoDiag must not diagnose battery health from this value alone.

The source also exposes `MAX_INT_RESISTANCE_DIF`, which is a difference indicator between resistance extremes. It is decoded from one byte with `/10` scaling in the `e4.a`/`d4.a` family.

### 1.2 HV isolation resistance

The Watchdog `z3.a` decoder exposes `ISOLATION_RESISTANCE` from two response bytes. The source represents this value in **kΩ**:

- raw value = unsigned 16-bit value from the two response bytes,
- displayed value = raw value in kΩ.

Conceptually, isolation resistance answers a different safety question from internal battery resistance: **how well the HV electrical system is isolated from the vehicle/chassis reference**. A low isolation resistance indicates increased leakage coupling toward the vehicle reference and can indicate an insulation fault. The exact physical test topology, diagnostic threshold and Mitsubishi service-limit interpretation are not established by the extracted APK alone and must not be invented.

### 1.3 Cell-voltage spread

`CELL_MAX_DIF_VOLT` is calculated from maximum and minimum cell voltage. It is a voltage imbalance indicator:

`delta = max cell voltage - min cell voltage`

It is shown alongside resistance/isolation because it helps interpret battery condition, but it is **not** a resistance measurement.

## 2. ECU / module attribution

The application must show the ECU/module responsible for the value whenever the evidence identifies it. The current source extraction identifies these values through the PHEV Watchdog battery-state decoder, but the available static source does **not** provide sufficient independent evidence to claim a definitive Mitsubishi ECU name/address for every value.

Therefore the UI should initially use a neutral label such as:

> **HV Battery / battery-management diagnostic data**

and display the exact ECU identity/address only after it has been observed and recorded from the vehicle.

Do not hard-code a guessed CAN request ID, ECU address, UDS DID, or physical module name merely because the value appears in a Watchdog decoder.

## 3. Required application presentation

Create a dedicated **HV Battery → Resistance & Isolation** screen/card. It should contain three clearly separated sections.

### A. Internal battery resistance

Display:

- Current maximum internal resistance
- Current minimum internal resistance
- Difference between maximum and minimum
- Unit
- Source / verification state
- Timestamp
- ECU identity, when known
- Raw response bytes, expandable under an Expert/diagnostic details section

For each value provide:

- `current`
- `minimum observed during this capture/session`
- `maximum observed during this capture/session`
- `source-defined range/limit`, only when a verified service specification exists
- verification state

**Important:** session min/max are statistical observations, not manufacturer limits.

### B. HV isolation resistance

Display:

- Current isolation resistance
- Minimum observed during the session
- Maximum observed during the session
- Manufacturer/service threshold, only after independently verified evidence exists
- Status interpretation
- Timestamp
- ECU identity, when known
- Raw bytes / request-response evidence in Expert view

The primary status should be:

- **OK / within verified limit** — only when a verified limit exists,
- **Warning** — when a verified warning threshold is exceeded,
- **Critical** — when a verified critical threshold is exceeded,
- **Measured / limit unknown** — when the value is known but no trustworthy threshold is available,
- **Not available** — when communication/value is absent,
- **Unverified** — when the mapping itself has not yet been validated on the vehicle.

Never turn an unverified threshold into a safety claim.

### C. Cell balance context

Display:

- maximum cell voltage
- cell ID of maximum cell
- minimum cell voltage
- cell ID of minimum cell
- voltage delta
- target cell voltage

This section should make clear that cell-voltage delta and target voltage are supporting battery-state information and are not isolation-resistance measurements.

## 4. "What does it mean?" help text

The user-facing explanation should be understandable without automotive engineering knowledge:

> **Internal resistance** describes how much electrical loss is associated with the battery under its operating conditions. Higher resistance can be associated with ageing or imbalance, but this value must be interpreted together with cell voltage, temperature, current and battery state.

> **Isolation resistance** describes how strongly the high-voltage electrical system is isolated from the vehicle/chassis reference. A lower value can indicate unwanted leakage toward the vehicle reference and may be relevant to high-voltage safety. AutoDiag will show the measured value and the verified manufacturer threshold separately; it will not invent a limit.

> **Cell voltage difference** is the difference between the highest and lowest measured cell voltage. It helps show how evenly the battery cells are operating.

## 5. Source-extracted mappings currently available

### Watchdog `z3.a` — command `21 01`

The extracted decoder exposes:

- `ISOLATION_RESISTANCE`: two-byte unsigned value, source unit kΩ.
- maximum/minimum cell voltage and IDs,
- cell-voltage delta,
- target cell voltage,
- maximum/minimum module temperature and IDs,
- battery current and voltage,
- maximum input/output power,
- cooling-fan PWM,
- charged/discharged Ah and kWh.

The exact byte positions are retained in the candidate extraction data and must be treated as unverified until captured from the target vehicle.

### Watchdog `e4.a` — command `21 01`

The extracted decoder exposes:

- maximum internal resistance: response byte `/10`, source unit MΩ,
- minimum internal resistance: response byte `/10`, source unit MΩ,
- maximum internal-resistance difference: response byte `/10`, source unit MΩ,
- maximum voltage-difference failure judgement: two-byte value `/1000`, source unit V,
- maximum/minimum cell voltage and IDs,
- cell-voltage delta,
- target cell voltage,
- battery voltage/current/capacity,
- battery cooling fan PWM.

A separate `d4.a` decoder uses the same general battery layout and additionally exposes battery SOH. These decoder families must remain separate until applicability to a particular Outlander configuration is confirmed.

## 6. Limits: three different concepts

The UI and data model must never mix these:

### Manufacturer limit

A documented Mitsubishi/service specification. This is the only value allowed to produce a definitive "within/outside specification" judgement.

### Decoder/source range

A range implied by an encoding, byte width or source application. This is **not automatically a safe operating limit**.

### Session observed min/max

The minimum and maximum values actually observed by AutoDiag during a capture or live-data session. These are useful for trending but are not manufacturer limits.

Example:

```text
Isolation resistance
Current:          1850 kΩ
Session minimum:  1720 kΩ
Session maximum:  1910 kΩ
Manufacturer min: unknown
Status:           measured / limit unknown
Verification:     partially_verified
```

The example values above are illustrative only and must never be shipped as vehicle data.

## 7. Verification and evidence

The existing Outlander architecture deliberately keeps source knowledge separate from vehicle evidence. The resolver can expose a database definition without claiming that the value has been verified on the connected vehicle. This is consistent with the existing profile policy.

Verification states:

- `UNVERIFIED`: decoder/source information only,
- `PARTIALLY_VERIFIED`: matching request/response observed but interpretation or full vehicle validation remains incomplete,
- `VERIFIED`: request/response and decoder/value interpretation validated on the scoped vehicle configuration.

For safety-relevant isolation information, the UI must retain the evidence status next to the value.

## 8. Safety rule

Do not perform a high-voltage insulation test by commanding an unknown actuator or service routine merely to obtain a number. Initial AutoDiag evidence capture is read-only. Any active test requiring a vehicle service routine must be implemented separately with explicit safety gates and verified manufacturer procedure information.

## 9. Planned live-data behavior

The dedicated screen should update continuously when a verified/readable candidate is available and retain a short history so the user can see:

- current value,
- recent trend,
- session min/max,
- timestamp of last valid response,
- communication freshness,
- verification state.

For cell voltage, a compact min/max/delta view should be expandable into all individual cell values when the corresponding map is available.

## 10. Implementation priority

This domain is now a **high-priority Outlander feature**. Before promoting the values to production `signals.json`, complete:

1. full extraction of the remaining Watchdog battery decoders,
2. candidate records with source class/method and formulas,
3. request/response capture on the actual Outlander,
4. decoder replay tests,
5. independent verification of units and thresholds,
6. UI with current + session min/max + verified limits + ECU/evidence details,
7. only then promotion from candidate to verified diagnostic data.
