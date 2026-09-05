# AI APK extraction guide — current revision 2026-09-05

This document supersedes ambiguous or stale instructions when they conflict with `AI_HANDOFF_APK_EXTRACTION_2026-09-05.md` and the current provenance records.

## Objective

Extract diagnostic behaviour from Android APKs as reproducible evidence for AutoDiag. The output is evidence and data contracts, not copied proprietary source/database content.

## Mandatory evidence chain

```text
APK
 -> exact command literal
 -> code/string reference
 -> model/class/variant
 -> transport/address evidence
 -> response shape
 -> normalized diagnostic payload d[]
 -> exact decoder expression
 -> byte order/signedness/scale/offset/unit
 -> provenance
 -> candidate (only if sufficient)
 -> deterministic test
 -> ECU/address correlation
 -> real-vehicle capture
 -> VERIFIED
```

If any link is missing, record `UNRESOLVED`/`UNVERIFIED`. Never fill gaps from signal names, class names, public topology or intuition.

## Canonical data ownership — SINGLE SOURCE OF TRUTH

**`sirvan0010-alt/AutoDiag-WiCAN-Diagnostic-Data` is the sole source of truth for production diagnostic candidates, decoder definitions, extraction provenance and their manifests.**

The repository `AutoDiag-WiCAN-Pro` is the application/code repository. Its `diagnostic-data/` directory is **legacy seed/staging material only** and must not be used as a second production data store. New or changed candidates/provenance MUST NOT be written there.

Runtime direction is explicit:

```text
AutoDiag-WiCAN-Pro code
        |
        v
GitHubDiagnosticDataProvider
        |
        v
AutoDiag-WiCAN-Diagnostic-Data/main
        |
        +--> manifest.json
        +--> data/candidates/*
        +--> provenance/*
```

The local `diagnostic-data/` directory is not a synchronization target and must not be treated as an authoritative cache. If legacy files are needed temporarily for migration, they must be clearly labelled legacy and never edited as production data.

### Three locations — exact roles

| Location | Role | Production authority |
|---|---|---|
| `AutoDiag-WiCAN-Diagnostic-Data/manifest.json` | canonical dataset manifest/counts | **YES** |
| `AutoDiag-WiCAN-Diagnostic-Data/data/candidates/` + `provenance/` | canonical candidates and evidence | **YES** |
| `AutoDiag-WiCAN-Pro/diagnostic-data/` | legacy seed/staging/compatibility only | **NO** |

A path mentioned from the `AutoDiag-WiCAN-Pro` repository must be prefixed with the repository name when it refers to the external repository. Never write `provenance/...` as if it were local when the intended location is the external repository.

## DEX parser gate

Before interpreting bytecode, validate the DEX parser.

For DEX 035 the header fields are:

```text
string_ids_size @ 0x38
string_ids_off  @ 0x3C
type_ids_size   @ 0x40
type_ids_off    @ 0x44
proto_ids_size  @ 0x48
proto_ids_off   @ 0x4C
field_ids_size  @ 0x50
field_ids_off   @ 0x54
method_ids_size @ 0x58
method_ids_off  @ 0x5C
class_defs_size @ 0x60
class_defs_off  @ 0x64
```

Required assertions:

- index bounds are valid;
- class-data method IDs resolve to methods belonging to the class being decoded;
- field references have valid declaring class/type;
- code offsets and instruction lengths stay within the DEX;
- branch targets are valid;
- payloads such as switch/array-data are handled according to DEX instruction rules.

A contradictory class/method mapping means **parser failure**. It must never be converted into a protocol claim.

## PHEV Watchdog 21 04

Source:

`PHEV Watchdog Lite 1.9.1.2023OCT29.apk`

SHA-256:

`9ebac53f13ba9a1d04be158e49e37b886b9c35a711c9a4e33029c16a17b86ce6`

Observed:

- one `classes.dex`, 6,554,216 bytes;
- no native `lib/*` binaries;
- exact command string `21 04`;
- prior class association `Lz3/d;`;
- related strings for cell-voltage maps and cell-voltage/internal-resistance aggregates.

Current status:

```text
21 04 decoder      = UNRESOLVED
scale/offset       = UNRESOLVED
byte order         = UNRESOLVED
signedness         = UNRESOLVED
ECU/address        = UNRESOLVED
verification       = UNVERIFIED
candidate creation = BLOCKED
```

The phrase “32 voltage outputs, scale unresolved” is an old working hypothesis, not sufficient evidence for a candidate. It remains explicitly unpromoted until validated from bytecode/data flow.

## Extraction procedure for 21 04

1. Locate all references to the exact `21 04` string.
2. Trace the constructor/model registration around the reference.
3. Identify the actual decoder method; do not assume a method name such as `s` is a decoder.
4. Trace the input list/array and establish the meaning of `d[]`.
5. Establish the response prefix and ISO-TP normalization boundary.
6. Extract exact indices and arithmetic.
7. Determine scale, offset, unit, endian and signedness only from code/data evidence.
8. Record the result in `AutoDiag-WiCAN-Diagnostic-Data/provenance/apk-extraction/phev-watchdog/`.
9. Create a candidate under `AutoDiag-WiCAN-Diagnostic-Data/data/candidates/` only after the decoder contract is sufficiently explicit.
10. Add a deterministic unit test.
11. Keep ECU/address and vehicle scope unresolved until independently supported.

## Repository placement

### `AutoDiag-WiCAN-Diagnostic-Data`

Use for APK provenance, extraction matrices, decoder candidates and evidence metadata. This is the **only authoritative production data repository**.

Do not place unresolved formulas into production candidate files.

### `AutoDiag-WiCAN-Pro`

Use for parsers, runtime integration, tests, architecture and AI instructions. Its `diagnostic-data/` directory is legacy seed/staging only and is not a production source of truth.

Do not hardcode a new decoder before its Diagnostic-Data contract is justified.

## Pre-commit data-location checklist

Before committing any extraction result:

- [ ] Is this production candidate/provenance data? If yes, commit it only to `AutoDiag-WiCAN-Diagnostic-Data`.
- [ ] Is the path explicitly prefixed with the correct repository when referenced from `AutoDiag-WiCAN-Pro`?
- [ ] Did I avoid creating/updating a second candidate copy under `AutoDiag-WiCAN-Pro/diagnostic-data/`?
- [ ] Does `AutoDiag-WiCAN-Diagnostic-Data/manifest.json` count match the actual candidate files?
- [ ] Does `GitHubDiagnosticDataProvider` load every production candidate file that the manifest advertises?
- [ ] Is provenance present for every promoted candidate?
- [ ] Is `VERIFIED` still gated by real-vehicle evidence?

If any answer is no, stop before extraction continues.

## Verification levels

- `UNRESOLVED`: the requested relationship has not been extracted safely.
- `UNVERIFIED`: a static extraction/candidate exists but vehicle evidence is missing.
- `PARTIALLY_VERIFIED`: decoder and some external correlation exist, but scope/address/vehicle proof is incomplete.
- `VERIFIED`: reproducible vehicle evidence binds request, response, ECU/address, decoder and vehicle scope.

Static APK evidence can never directly create `VERIFIED`.

## Safety

APK reverse engineering is read-only evidence work. It does not authorize coding, adaptation, actuator commands, security access, immobilizer operations or other write functionality.
