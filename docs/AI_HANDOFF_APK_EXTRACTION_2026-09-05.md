# AI handoff — APK extraction continuation — 2026-09-05

## Active strategy

The goal of APK extraction is **not** to reproduce the source application internally. Extract only the final diagnostic data contract needed by AutoDiag:

```text
request/init
 -> response
 -> normalized payload boundary
 -> exact byte/bit positions
 -> expression/scale/offset
 -> unit
 -> canonical UI signal
```

Prefer a direct, reproducible decoder contract over reconstructing the APK's class architecture, UI, database layer or internal abstractions.

Deep APK reverse engineering is required **only when a final contract is missing or ambiguous**. Once the final contract is proven, stop tracing unrelated internals.

## Source artifact

`PHEV Watchdog Lite 1.9.1.2023OCT29.apk`

SHA-256:

`9ebac53f13ba9a1d04be158e49e37b886b9c35a711c9a4e33029c16a17b86ce6`

## Current verified APK facts

- APK size: 12,127,187 bytes.
- One `classes.dex` is present; size 6,554,216 bytes.
- No native `lib/*` binaries are present.
- Recorded command set includes `21 01`, `21 02`, `21 03`, `21 04`, `21 05`, `21 11`, `21 14`, `21 15`, `21 23`, `21 24`, `21 25`, `21 26`, `22 01 01..05`, and `22 B0 02`.
- Direct decoder evidence already exists for several 21xx values, including isolation resistance, internal resistance, 32-cell groups, and motor/generator RPM.

## 21 04 current state

Static bytecode analysis of `Lz3/d;` established a final decoder shape:

```text
request:        21 04
outputs:        32
responseIndex:  0..31
raw type:       unsigned 8-bit
scale:          0.02
offset:         0
unit:           V
verification:   UNVERIFIED
```

The 32-output contract is suitable as an **unverified candidate** because the bytecode arithmetic and output cardinality are established. It does **not** prove physical cell numbering, exact ECU/address binding, vehicle generation or production applicability.

The candidate is stored in the external canonical repository:

`AutoDiag-WiCAN-Diagnostic-Data/data/candidates/outlander_phev_watchdog_21_04.json`

Its provenance is:

`AutoDiag-WiCAN-Diagnostic-Data/provenance/apk-extraction/phev-watchdog/21-04-extraction-2026-09-05.json`

## Extraction priority

For every APK/vehicle, work in this order:

1. Find an already proven final data contract in repository data/provenance.
2. Search the APK only for missing fields or ambiguous contracts.
3. Extract the smallest sufficient evidence chain.
4. Store the final contract in Diagnostic-Data with provenance.
5. Add a deterministic decoder test.
6. Keep ECU/address/vehicle scope `UNVERIFIED` until independently correlated.
7. Stop. Do not continue reverse engineering merely because more APK internals exist.

Target EV signals currently include:

- SOC
- SOH
- HV voltage
- HV current
- battery temperature
- cell voltages
- cell min/max/difference
- internal resistance where available
- front/rear motor RPM
- generator RPM
- DTCs

The same final-contract method should be reused for other EVs.

## Evidence chain when deep extraction is necessary

```text
exact request
 -> code reference
 -> actual decoder
 -> normalized payload boundary
 -> exact indices/bits
 -> arithmetic
 -> unit/scale/offset/signedness/endian
 -> provenance
 -> candidate
 -> deterministic test
 -> ECU/address correlation
 -> real vehicle validation
```

No missing link may be filled by intuition. Signal names, class names, public CAN topology and guesses are not decoder evidence.

## DEX parser gate

Before trusting custom DEX parsing, validate:

- header offsets and index ranges;
- class-data method ownership against `method_ids`;
- field ownership/types;
- `code_item` bounds and instruction widths;
- branch targets and payload instructions.

A contradictory class/method mapping means parser failure, not protocol evidence.

## Repository discipline

### `AutoDiag-WiCAN-Diagnostic-Data`

Canonical source of truth for production diagnostic candidates, decoder definitions, APK provenance and manifests.

### `AutoDiag-WiCAN-Pro`

Application/runtime repository. Use for parser/runtime implementation, tests and AI instructions. Its legacy `diagnostic-data/` directory is not the production source of truth.

## Candidate rules

- `UNRESOLVED`: final decoder contract is not safely established.
- `UNVERIFIED`: static/equivalent evidence establishes a contract but vehicle evidence is missing.
- `PARTIALLY_VERIFIED`: decoder plus external correlation exists, but scope/address/vehicle proof is incomplete.
- `VERIFIED`: real-vehicle evidence binds request, response, ECU/address, decoder and vehicle scope.
- Never promote an unverified candidate to production merely because its formula looks plausible.

## Branch/PR discipline

The Outlander work remains on `feat/mitsubishi-outlander-phev` under draft PR #10. Do not claim mergeability or production completeness from extraction progress alone.

S3XY/Tesla work is separate and must not be mixed into the Outlander candidate dataset.

## Safety

APK reverse engineering is read-only evidence work. It does not authorize coding, adaptation, actuator commands, security access, immobilizer operations, immobilizer bypass or any other write functionality.
