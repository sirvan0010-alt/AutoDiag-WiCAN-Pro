# APK extraction rules — 2026-09-05 addendum

This addendum is mandatory for future AI APK reverse-engineering work.

## Non-negotiable evidence rule

A plausible decompiler/disassembler output is not evidence until the parser validates the DEX structure. If class-data method IDs resolve to methods belonging to another declaring class, stop: the parser is wrong or the input is not being interpreted as expected.

Never promote a decoder from a failed parser run.

## Required DEX validation

For DEX 035, use:

```text
string_ids_size @ 0x38 / off @ 0x3C
type_ids_size   @ 0x40 / off @ 0x44
proto_ids_size  @ 0x48 / off @ 0x4C
field_ids_size  @ 0x50 / off @ 0x54
method_ids_size @ 0x58 / off @ 0x5C
class_defs_size @ 0x60 / off @ 0x64
```

Validate all index ranges and, critically, the class-data method/field ownership invariants before interpreting bytecode.

## 21 04 status

For `PHEV Watchdog Lite 1.9.1.2023OCT29`:

- SHA-256: `9ebac53f13ba9a1d04be158e49e37b886b9c35a711c9a4e33029c16a17b86ce6`
- `classes.dex`: 6,554,216 bytes
- exact command string: `21 04`
- known prior class association: `Lz3/d;`
- related labels include cell-voltage maps and cell-voltage/internal-resistance aggregates
- exact decoder expression: **UNRESOLVED**
- scale/offset/byte order/signedness: **UNRESOLVED**
- ECU/address: **UNRESOLVED**
- production promotion: **FORBIDDEN**

The previously stated “32 voltage outputs, scale unresolved” is retained only as an old working hypothesis until independently re-established from validated bytecode. It must not be converted into a candidate decoder solely on that basis.

## Correct workflow for the next pass

1. Validate the DEX parser.
2. Find every code reference to the exact `21 04` string index.
3. Trace constructor/model registration from that literal.
4. Identify the real decoder method rather than assuming the class method named `s` is the decoder.
5. Resolve the input container and prove what `d[]` represents.
6. Extract exact byte indices and arithmetic.
7. Only then create/update a Diagnostic-Data candidate.
8. Add a deterministic test for the exact expression.
9. Keep ECU/address and vehicle applicability unresolved until independently evidenced.

## Repository placement

- forensic extraction/provenance -> `AutoDiag-WiCAN-Diagnostic-Data/provenance/apk-extraction/phev-watchdog/`
- candidate diagnostic schemas -> `AutoDiag-WiCAN-Diagnostic-Data/data/candidates/` only after decoder evidence is sufficient
- application architecture/instructions -> `AutoDiag-WiCAN-Pro/docs/`
- Kotlin runtime changes -> only after the data contract exists and tests justify the integration

## Safety

APK extraction is read-only evidence work. It does not authorize coding, adaptation, actuator commands, security access, immobilizer operations or other write capabilities.
