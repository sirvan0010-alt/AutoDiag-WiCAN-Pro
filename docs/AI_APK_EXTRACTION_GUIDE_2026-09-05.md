# AI APK extraction guide — current revision 2026-09-05

This guide defines the extraction strategy for AutoDiag. It supersedes stale instructions that treat full APK reconstruction as the goal.

## 1. Objective

Extract **final diagnostic data contracts**, not the internal architecture of the source APK.

The desired result is:

```text
request/init
 -> response
 -> normalized payload boundary
 -> exact byte/bit positions
 -> expression/scale/offset
 -> unit
 -> canonical signal/UI value
```

The new AutoDiag runtime needs the final decoder behaviour. It normally does not need the APK's original class hierarchy, UI implementation, database implementation or internal naming.

### Deep reverse engineering rule

Use deep APK reverse engineering **only to resolve a missing or ambiguous final contract**. Once request, response, extraction and formula are proven, stop. Do not spend time reconstructing unrelated APK internals.

## 2. Extraction priority

For each vehicle/application:

1. Search existing Diagnostic-Data and provenance for an already proven contract.
2. Identify the requested final signals that are still missing.
3. Search the APK for those exact requests, signal labels and decoder paths.
4. Extract the smallest sufficient evidence chain.
5. Write the contract and provenance to the canonical Diagnostic-Data repository.
6. Add deterministic tests.
7. Correlate ECU/address and vehicle scope separately.
8. Validate with a real vehicle before promotion.

This is the default method for Outlander PHEV and later EVs.

Typical EV targets include SOC, SOH, HV voltage/current, battery temperature, cell voltages, cell min/max/difference, internal resistance, motor RPM, generator RPM and DTCs.

## 3. Mandatory evidence chain when deep extraction is necessary

```text
APK
 -> exact request literal
 -> code/string reference
 -> actual decoder
 -> normalized payload boundary
 -> exact byte/bit positions
 -> arithmetic
 -> byte order/signedness/scale/offset/unit
 -> provenance
 -> candidate (only if sufficient)
 -> deterministic test
 -> ECU/address correlation
 -> real-vehicle capture
 -> VERIFIED
```

If a link is missing, record `UNRESOLVED` or `UNVERIFIED`. Never fill a gap from signal names, class names, public CAN topology or intuition.

## 4. Canonical data ownership — SINGLE SOURCE OF TRUTH

**`sirvan0010-alt/AutoDiag-WiCAN-Diagnostic-Data` is the sole source of truth for production diagnostic candidates, decoder definitions, extraction provenance and manifests.**

`AutoDiag-WiCAN-Pro` is the application/code repository. Its `diagnostic-data/` directory is legacy seed/staging/compatibility material only and is not a second production data store.

Runtime direction:

```text
AutoDiag-WiCAN-Pro
      |
      v
GitHubDiagnosticDataProvider
      |
      v
AutoDiag-WiCAN-Diagnostic-Data/main
      +--> manifest.json
      +--> data/candidates/*
      +--> provenance/*
```

New or changed production candidates/provenance MUST be written to the external Diagnostic-Data repository.

## 5. DEX parser gate

Before trusting custom DEX parsing, validate:

- header offsets and index ranges;
- class-data method ownership against `method_ids`;
- field ownership/types;
- `code_item` bounds and instruction widths;
- branch targets;
- switch/array-data payload handling.

A contradictory class/method mapping means **parser failure**, not protocol evidence.

## 6. PHEV Watchdog — 21 04

Source APK:

`PHEV Watchdog Lite 1.9.1.2023OCT29.apk`

SHA-256:

`9ebac53f13ba9a1d04be158e49e37b886b9c35a711c9a4e33029c16a17b86ce6`

Established static decoder contract:

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

This is sufficient for an **unverified candidate**. It does not by itself establish physical cell numbering, ECU/address binding, vehicle generation or production applicability.

Canonical candidate:

`AutoDiag-WiCAN-Diagnostic-Data/data/candidates/outlander_phev_watchdog_21_04.json`

Provenance:

`AutoDiag-WiCAN-Diagnostic-Data/provenance/apk-extraction/phev-watchdog/21-04-extraction-2026-09-05.json`

Do not reopen the entire APK architecture merely to reconfirm this contract. Reopen deep tracing only if a concrete contradiction appears.

## 7. Candidate/verification levels

- `UNRESOLVED`: final decoder contract is not safely established.
- `UNVERIFIED`: static/equivalent evidence establishes a decoder contract, but vehicle evidence is missing.
- `PARTIALLY_VERIFIED`: decoder plus external correlation exists, but scope/address/vehicle proof is incomplete.
- `VERIFIED`: real-vehicle evidence binds request, response, ECU/address, decoder and vehicle scope.

Static APK evidence can never directly create `VERIFIED`.

## 8. Deterministic implementation rule

A final contract should be representable independently of the source APK, for example:

```text
request = 010C
signal  = ENGINE_RPM
extract = unsigned16(response[B3], response[B4])
scale   = 0.25
unit    = RPM
```

or:

```text
request = 21 04
signals = CELL_VOLTAGE[0..31]
extract = unsigned8(response[index])
scale   = 0.02
unit    = V
verification = UNVERIFIED
```

The exact request, indices, expression and normalization boundary must come from evidence. These examples do not authorize guessing unknown vehicle-specific values.

## 9. Repository checklist

Before committing extraction results:

- [ ] Did I search existing final contracts first?
- [ ] Am I extracting only missing/ambiguous fields?
- [ ] Is the request/response and normalized payload boundary proven?
- [ ] Are exact indices/bits and arithmetic proven?
- [ ] Is unit/scale/offset/signedness/endian proven?
- [ ] Is provenance stored in `AutoDiag-WiCAN-Diagnostic-Data`?
- [ ] Is a deterministic test present?
- [ ] Is ECU/address/vehicle scope clearly marked if still unresolved?
- [ ] Does the external manifest include the candidate?
- [ ] Does `GitHubDiagnosticDataProvider` load every advertised production candidate?
- [ ] Have I avoided creating a second authoritative copy in `AutoDiag-WiCAN-Pro/diagnostic-data/`?

## 10. Branch/PR discipline

Outlander work remains on `feat/mitsubishi-outlander-phev` under draft PR #10. Do not claim mergeability or production completeness merely because an APK contract has been extracted.

S3XY/Tesla extraction is separate and must not be mixed into Outlander candidate data.

## 11. Safety

APK reverse engineering is read-only evidence work. It does not authorize coding, adaptation, actuator commands, security access, immobilizer operations, immobilizer bypass or any other write functionality.
