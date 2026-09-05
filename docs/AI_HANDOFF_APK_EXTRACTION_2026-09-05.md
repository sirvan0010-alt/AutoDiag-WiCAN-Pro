# AI handoff — APK extraction continuation — 2026-09-05

## Active task

Continue evidence-driven extraction from `PHEV Watchdog Lite 1.9.1.2023OCT29.apk`, prioritizing request `21 04`.

Source artifact SHA-256:

`9ebac53f13ba9a1d04be158e49e37b886b9c35a711c9a4e33029c16a17b86ce6`

## Current verified facts

- APK size: 12,127,187 bytes.
- One `classes.dex` is present; size 6,554,216 bytes.
- No `lib/*` native binaries are present in this APK.
- Exact string literal `21 04` exists in the DEX string pool.
- The APK contains related diagnostic labels for cell-voltage maps and cell-voltage/internal-resistance aggregates.
- The broader command set already recorded in provenance includes `21 01`, `21 02`, `21 03`, `21 04`, `21 05`, `21 11`, `21 14`, `21 15`, `21 23`, `21 24`, `21 25`, `21 26`, `22 01 01..05`, and `22 B0 02`.

## 21 04 state

`Lz3/d;` is the previously recorded class associated with `21 04`.

Current production state:

```text
request:       21 04
class:         Lz3/d;
decoder:       UNRESOLVED
scale/offset:  UNRESOLVED
byte order:    UNRESOLVED
signedness:    UNRESOLVED
ECU/address:   UNRESOLVED
verification:  UNVERIFIED
candidate:     NO
```

The earlier shorthand “32 voltage outputs, scale unresolved” is **not sufficient evidence for a candidate**. Treat it as a hypothesis until re-established from valid bytecode/data-flow analysis.

Detailed provenance is in:

`AutoDiag-WiCAN-Diagnostic-Data/provenance/apk-extraction/phev-watchdog/21-04-extraction-2026-09-05.json`

## Important correction to the supplied ad-hoc script

The supplied script produced a plausible-looking `Lz3/d.s` disassembly, but its result must not be accepted automatically. The extraction pass exposed contradictory class/method relationships when interpreted through the hand-written DEX parser. That is a parser-validation failure, not protocol evidence.

Before decoding `21 04`, the AI must:

1. validate DEX header offsets and all index ranges;
2. validate class-data method ownership against `method_ids`;
3. validate field ownership/types;
4. validate `code_item` bounds and instruction widths;
5. only then follow `21 04` references into constructor/model registration and decoder data flow.

## Required extraction chain

```text
21 04 literal
 -> code reference(s)
 -> model/class registration
 -> actual decoder method
 -> input container
 -> normalized d[] boundary
 -> exact indices
 -> arithmetic
 -> unit/scale/offset/signedness/endian
 -> provenance
 -> candidate
 -> test
 -> ECU/address correlation
 -> real vehicle validation
```

No skipped link may be filled by intuition.

## Repository discipline

### Diagnostic-Data

Use for:

- APK provenance
- extraction matrix
- candidate JSON
- decoder definitions
- evidence metadata

Do not put an unresolved hypothesis into `data/candidates/`.

### AutoDiag-WiCAN-Pro

Use for:

- extraction rules/instructions
- parser/runtime implementation
- tests
- architecture documentation

Do not hardcode a new `21 04` decoder until the data contract is justified by evidence.

## Branch/PR discipline

The Outlander implementation remains on `feat/mitsubishi-outlander-phev` under draft PR #10. Do not claim it is mergeable or production-complete merely because extraction work advances.

The S3XY work is separate and remains on its own PR/branch. Do not mix Tesla/S3XY schema documentation into the Outlander Diagnostic-Data candidates.

## Verification gate

`VERIFIED` requires real-vehicle evidence binding request/response, ECU/address, decoder and vehicle scope. APK evidence alone can never set `VERIFIED`.

## Safety gate

This extraction is read-only. No coding, adaptation, actuator, security-access, immobilizer or other write operation may be enabled as a result of APK reverse engineering.
