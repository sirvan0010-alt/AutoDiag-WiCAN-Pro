# AI APK Diagnostic Extraction Guide

## Purpose

This document is the working protocol for another AI/developer extracting diagnostic behaviour from Android APKs used as behavioural references for AutoDiag-WiCAN-Pro, especially Mitsubishi Outlander PHEV applications such as PHEV Watchdog and Torque.

The goal is **reproducible evidence extraction**, not copying application code or proprietary databases.

## Mandatory order

For every diagnostic command or signal, work through this chain and do not skip unresolved links:

```text
APK -> command literal -> model/class/variant -> transport/address evidence
    -> response shape -> normalized payload d[] -> decoder expression
    -> unit/scale/offset/signedness/byte order -> candidate record
    -> unit test -> ECU/address correlation -> real-vehicle validation
    -> production promotion
```

If a link is not proven, record `UNRESOLVED` or `CANDIDATE`. Never fill it with a plausible value.

## 1. Inventory the APK first

Record:

- filename and version
- SHA-256
- classes.dex size/count if available
- all diagnostic command literals (`21 xx`, `22 xx xx`, `03`, `09 xx`, etc.)
- signal/label strings
- transport strings (`ATSH`, `ATFC*`, `CAN_RECEIVE_ADDRESS`, protocol names)
- SQL tables/columns if the APK stores logs
- assets/databases/CSV files and their schemas

Keep the APK itself in the provenance/incoming area when licensing permits. Do not copy proprietary binary/database content into production data.

## 2. Extract command literals

Search the decoded string pool and code for exact command literals. Normalize formatting only for comparison:

- `21 01` == `2101`
- `22 B0 02` == `22B002`

For each command record every class/method in which it occurs. A command can have more than one model variant.

Do not assume that one command means one ECU.

## 3. Find the response parser boundary

The critical question is what `d[index]` means in the reference application.

For PHEV Watchdog, the useful model is a normalized `ArrayList<Integer>`/`d[]`. The Android decoder indexes the **diagnostic payload**, not the CAN header and not ISO-TP PCI bytes.

Example observed wire response:

```text
762 10 37 61 01 82 83 0F 8B
762 21 24 0F 88 03 0C 6E 52
```

For the `21 01` Watchdog path, the normalized decoder payload begins:

```text
d[0]=82, d[1]=83, d[2]=0F, d[3]=8B, ...
```

Therefore a decoder expression must be mapped against normalized `d[]`, not raw frame bytes.

### Do not make this mistake

Never model `d[0]` as the CAN ID, PCI byte, first-frame length byte, response service, or PID. Those belong to the transport/protocol boundary and are removed before the Watchdog decoder indexes the payload.

## 4. Distinguish ISO-TP framing from payload

At minimum identify:

- CAN arbitration/header bytes printed by the adapter
- ISO-TP Single Frame (`0x0n`)
- ISO-TP First Frame (`0x1n`)
- ISO-TP Consecutive Frame (`0x2n`)
- ISO-TP Flow Control (`0x3n`)
- diagnostic positive-response prefix (`61 xx`, `62 xx xx`, etc.)

For a multi-frame response, concatenate diagnostic payload bytes in order and remove transport framing before comparing with decoder indices.

The AutoDiag Outlander parser currently implements this normalization for the Watchdog `21 xx` response family. If another command family has a different positive-response structure, extend the parser deliberately and add a test rather than silently changing existing indexing.

## 5. Extract decoder expressions exactly

For every signal capture the actual arithmetic/bit operation from the reference implementation.

Record at least:

- signal ID/name
- request
- class/variant
- first/last index when contiguous
- explicit `responseIndices` when non-contiguous
- decoder kind
- byte order
- signed/unsigned
- scale
- offset
- unit
- any clamp/validity rule
- evidence source and location

Examples already established from PHEV Watchdog:

```text
21 01 / Lz3a:
  ISOLATION_RESISTANCE = d[78] * 256 + d[79]
  unit = kOhm

21 02 / Lz3b:
  CELL_VOLTAGE[i] = d[i] / 50.0
  32 cell outputs

21 03 / Lz3c:
  cell-voltage outputs use d[i] / 50.0

21 03 / Lz3e:
  FRONT_MOTOR_RPM = d[31] * 256 + d[30]
  GENERATOR_RPM   = d[29] * 256 + d[26]
```

The generator expression is deliberately represented with explicit indices `[29,26]`. Do **not** convert it into a contiguous range `26..29`.

## 6. Map command to ECU/address only with evidence

Use three evidence classes:

### A. Direct APK transport evidence

Strongest APK-derived evidence is code showing the command is sent after an explicit `ATSH`/address selection or an equivalent receive-address configuration.

Trace:

```text
command -> address selection -> send -> receive filter -> parser/model
```

Do not infer the address from a class name alone.

### B. Independent vehicle capture/community correlation

A public capture may establish a request/response pair such as:

```text
BMU: 0x761 request -> 0x762 response
request: 21 01
```

This is useful correlation evidence, but it does not automatically prove that every Watchdog `21 01` variant is the same ECU on every model year.

### C. Real-vehicle capture

This is the promotion gate. Record:

- VIN scope or anonymized vehicle scope
- ECU identity if available
- request CAN ID/address
- response CAN ID/address
- exact raw response
- normalized payload
- decoder variant
- decoded value
- repeatability

## 7. Variant handling

The same request can have multiple decoder classes or model variants. Never merge them merely because the command literal is equal.

Current known Watchdog examples include multiple classes for `21 01` and `21 03`.

When variants disagree:

```text
variantId required -> resolver selects only explicit matching variant
otherwise -> NOT_FOUND / AMBIGUOUS
```

Do not guess between two decoders.

## 8. Candidate data format

Production candidates belong in `AutoDiag-WiCAN-Diagnostic-Data`, not hardcoded in Kotlin.

Use the normalized decoder schema and preserve provenance. For non-contiguous fields use `responseIndices`.

Minimum evidence fields should identify:

- source application/version
- source artifact hash
- command/request
- reference class/variant
- decoder expression or normalized decoder representation
- signal ID
- unit
- verification state
- ECU/address correlation state
- notes about unresolved transport details

## 9. Tests are mandatory

Every promoted candidate gets a deterministic unit test using a synthetic payload with values chosen to expose byte order and scale.

Examples:

- u8 cell voltage: `190 * 0.02 = 3.8 V`
- u16 LE RPM: bytes `34 12` -> `0x1234`
- non-contiguous generator RPM: `d[29]=12`, `d[26]=34` -> `0x1234`

Also test parser normalization separately from decoder arithmetic.

A passing unit test proves the implementation matches the recorded expression. It does **not** prove the ECU address or real-vehicle applicability.

## 10. Verification states

Use:

- `UNVERIFIED` — extracted/reasoned candidate, no sufficient vehicle correlation
- `PARTIALLY_VERIFIED` — decoder behaviour and some independent correlation exist, but scope/address/vehicle validation is incomplete
- `VERIFIED` — reproducible vehicle evidence binds ECU/address/request/response/decoder and scope

Never promote a static APK extraction directly to `VERIFIED`.

## 11. Special rule for Watchdog 21 03

The public Outlander CAN map provides candidate topology for motor/generator addresses, but current evidence does **not** prove that Watchdog `Lz3e` is bound to those addresses.

Therefore keep these separate until direct transport/capture evidence appears:

```text
front motor: 0x755 -> 0x756  CANDIDATE
rear motor:  0x753 -> 0x754   CANDIDATE
generator:   0x73C -> 0x73D   CANDIDATE
```

Do not write one of these addresses into the `21 03` candidate merely because the signal is named `FRONT_MOTOR_RPM` or `GENERATOR_RPM`.

## 12. SQL/log extraction is a separate layer

If the APK stores decoded values in SQLite, extract the schema and normalization rules separately from wire-level decoder formulas.

Do not confuse:

```text
wire decoder -> physical value
```

with:

```text
SQL/log normalization -> display/storage value
```

For example, a database transformation such as SOC display clamping is not evidence that the ECU transmits SOC with that same formula.

## 13. What to do when decompilers are unavailable

Do not stop.

Use the APK directly:

1. unzip `classes.dex`
2. inspect DEX string/type/method/class tables
3. locate command strings and signal labels
4. locate references to those strings
5. inspect surrounding bytecode or invoke a locally available DEX parser
6. correlate constructor/method offsets with command/model classes
7. record exact findings and confidence

If a full decompiler becomes available later, use it to confirm rather than overwrite the existing evidence without comparison.

## 14. Current high-priority extraction queue

For PHEV Watchdog Lite 1.9.1.2023OCT29, continue in this order:

```text
21 04
21 05
21 11
21 14
21 15
21 23
21 24
21 25
21 26
22 01 01
22 01 02
22 01 03
22 01 04
22 01 05
22 B0 02
```

For each command complete the full chain before moving on. Do not bulk-create speculative candidate JSON.

## 15. Required final audit before production promotion

Before a candidate becomes production-capable, confirm all of the following:

- [ ] request is exact
- [ ] response family is exact
- [ ] ISO-TP normalization is tested
- [ ] decoder indices are against normalized payload
- [ ] byte order is explicit
- [ ] signedness is explicit
- [ ] scale/offset/unit are explicit
- [ ] variant is explicit when multiple variants exist
- [ ] ECU/address is evidence-backed
- [ ] vehicle scope is defined
- [ ] provenance points to the source artifact
- [ ] unit test passes
- [ ] simulator/replay test exists where practical
- [ ] real-vehicle evidence exists for `VERIFIED`
- [ ] no write/security capability is enabled by this extraction

## Safety boundary

This workflow is for read-only diagnostic evidence extraction. Extracted commands must not be treated as permission to implement coding, adaptation, actuator tests, immobilizer/security access, or other write operations.

Static APK evidence may create a **candidate**. It never overrides the project's vehicle-specific verification and safety gates.
