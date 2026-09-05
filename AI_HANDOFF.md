# AI_HANDOFF.md — AutoDiag-WiCAN-Pro

Živý handoff pro AI a vývojáře. Není roadmapa. Před prací vždy znovu ověř aktuální HEAD této větve a `main` — tento soubor může být o commit pozadu.

## CURRENT STATE — 2026-09-05

Aktivní větev: `feat/mitsubishi-outlander-phev`

Hlavní aktuální práce: evidence-driven reverse engineering PHEV Watchdog Lite pro Mitsubishi Outlander PHEV a převod pouze bezpečně doložených decoderů do data-driven Diagnostic-Data vrstvy.

## READ FIRST

Před architekturou nebo změnou diagnostického jádra čti:

1. `README.md`
2. `AI_CONTEXT.md`
3. `ROADMAP.md`
4. `docs/ARCHITECTURE_OVERVIEW.md`
5. `docs/DIAGNOSTIC_KNOWLEDGE_BASE.md`
6. `docs/LONG_TERM_FEATURE_PRESERVATION.md`
7. `docs/AI_APK_EXTRACTION_GUIDE.md`
8. `docs/AI_APK_EXTRACTION_GUIDE_2026-09-05.md`
9. `docs/AI_HANDOFF_APK_EXTRACTION_2026-09-05.md`

## SINGLE SOURCE OF TRUTH — MANDATORY

**`sirvan0010-alt/AutoDiag-WiCAN-Diagnostic-Data` is the sole source of truth for production diagnostic candidates, decoder definitions, extraction provenance and their manifests.**

`AutoDiag-WiCAN-Pro/diagnostic-data/` is legacy seed/staging material only. It is not a production dataset, must not receive new candidates/provenance, and must not be treated as a synchronization target. The runtime `GitHubDiagnosticDataProvider` reads the external `AutoDiag-WiCAN-Diagnostic-Data/main` repository.

Canonical locations:

```text
AutoDiag-WiCAN-Diagnostic-Data/manifest.json
AutoDiag-WiCAN-Diagnostic-Data/data/candidates/*
AutoDiag-WiCAN-Diagnostic-Data/provenance/*
```

Never write an external-repository path as bare `provenance/...` in instructions; name the repository explicitly.

## REQUIRED EXTRACTION CHAIN

Každý APK-derived diagnostický údaj musí projít celým řetězcem:

```text
ECU/address
  -> varianta Watchdog modelu
  -> request
  -> response
  -> ISO-TP normalization
  -> decoder / d[]
  -> unit/scale/offset/endian/signedness
  -> test
  -> provenance
  -> candidate
  -> ECU/address correlation
  -> real vehicle validation
  -> production promotion
```

Žádný článek řetězce se nesmí doplnit odhadem. `UNRESOLVED`, `CANDIDATE` a `BLOCKED` jsou platné výsledky.

## CURRENT OUTLANDER EXTRACTION STATE

Aktivní extraction target je **Mitsubishi Outlander PHEV**.

Referenční APK:

- `PHEV Watchdog Lite 1.9.1.2023OCT29`
- SHA-256: `9ebac53f13ba9a1d04be158e49e37b886b9c35a711c9a4e33029c16a17b86ce6`

Z APK jsou potvrzeny command literals:

```text
21 01, 21 02, 21 03, 21 04, 21 05, 21 11, 21 14, 21 15,
21 23, 21 24, 21 25, 21 26,
22 01 01, 22 01 02, 22 01 03, 22 01 04, 22 01 05, 22 B0 02
```

### Exact decoder behaviour already established

- `21 01 / Lz3/a;`: `ISOLATION_RESISTANCE = d[78]*256+d[79]`, kΩ
- `21 01 / Le4/a;`: internal resistance max/min from `d[38]` / `d[39]`, scale 0.1 MΩ
- `21 01 / Ld4/a;`: max `d[12]*256+d[13]`, min `d[14]*256+d[15]`, each scale 0.001 MΩ; max difference `d[71]`, scale 0.02 MΩ
- `21 02 / Lz3/b;`: 32 cell-voltage outputs, `d[i]/50.0`, V
- `21 03 / Lz3/c;`: 32 cell-voltage outputs, `d[i]/50.0`, V
- `21 03 / Lz3/e;`: `FRONT_MOTOR_RPM = d[31]*256+d[30]`
- `21 03 / Lz3/e;`: `GENERATOR_RPM = d[29]*256+d[26]`

For generator RPM the representation must be explicit `responseIndices: [29,26]`; never model it as a contiguous range.

### Current command/class extraction matrix

The complete matrix is stored in the canonical external repository:

`AutoDiag-WiCAN-Diagnostic-Data/provenance/apk-extraction/phev-watchdog/command-extraction-status-2026-09-05.json`

Current state:

```text
21 01      -> Lz3/a, Ld4/a, Le4/a -> decoder extracted -> candidate
21 02      -> Lz3/b              -> decoder extracted -> candidate
21 03      -> Lz3/c, Lz3/e       -> decoder extracted -> candidate
21 04      -> Lz3/d              -> decoder unresolved
21 05      -> La4/a, Lb4/a       -> decoder extracted -> candidate
21 11      -> Lc4/c              -> decoder unresolved
21 14      -> Lc4/a              -> decoder unresolved
21 15      -> Lc4/b              -> decoder unresolved
21 23      -> Lc4/h              -> decoder unresolved
21 24      -> Lc4/i              -> decoder unresolved
21 25      -> Lc4/j              -> decoder unresolved
21 26      -> Lc4/k              -> decoder unresolved
22 01 01   -> Ly3/b              -> decoder unresolved
22 01 02   -> Ly3/c              -> decoder unresolved
22 01 03   -> Ly3/d              -> decoder unresolved
22 01 04   -> Ly3/e              -> decoder unresolved
22 01 05   -> Ly3/a              -> decoder unresolved
22 B0 02   -> Lz3/g              -> decoder unresolved
```

“Complete matrix” means every queued command has an explicit status. It does not mean unresolved commands are silently promoted.

## 21 04 PARSER BOUNDARY

`21 04 / Lz3/d;` remains `UNRESOLVED`. Do not promote the old hypothesis “32 voltage outputs, scale unresolved”. The supplied hand-written DEX disassembler produced contradictory class/method relationships and therefore failed the parser-validation gate. Validate DEX indices, class-data method ownership, field ownership, code-item bounds and instruction widths before interpreting `Lz3/d` bytecode.

Canonical provenance:

`AutoDiag-WiCAN-Diagnostic-Data/provenance/apk-extraction/phev-watchdog/21-04-extraction-2026-09-05.json`

## ECU / ADDRESS EVIDENCE

Independent public Outlander evidence supports:

```text
BMU          0x761 -> 0x762   21 01   strong correlation
BMU          0x761 -> 0x762   21 02   explicit cell-voltage addressing evidence
rear motor   0x753 -> 0x754           candidate topology
front motor  0x755 -> 0x756           candidate topology
generator    0x73C -> 0x73D           candidate topology
```

The 21 02 BMU address correlation is preserved in Diagnostic-Data candidate metadata. The same request `21 03` remains deliberately unbound because the available evidence does not prove which Watchdog model/address combination selects `Lz3/c` or `Lz3/e`.

Topology does not by itself prove Watchdog class-to-address binding. In particular, do not bind `Lz3e` to motor/generator addresses from signal names alone.

## PARSER BOUNDARY

Watchdog decoder `d[]` indexes normalized diagnostic payload bytes, not raw CAN/ISO-TP frames.

Remove/handle explicitly:

- CAN header/arbitration identifier
- ISO-TP PCI
- first-frame length
- diagnostic response service/PID prefix for the applicable family
- consecutive-frame sequence byte
- flow-control traffic

`OutlanderPhev21ResponseParser` implements the current `21 xx` boundary and has tests. A different response family must get its own parser logic/tests if its prefix differs.

## DATA-DRIVEN ARCHITECTURE

Decoder definitions belong in the external Diagnostic-Data repository, not hardcoded Kotlin business logic.

Current `DataDecoderSpec` supports:

- unsigned/signed 8 bit
- unsigned/signed 16 bit BE/LE
- scale
- offset
- unit
- explicit byte `indices` / JSON `responseIndices`

`SignalDecoderDefinition` additionally preserves optional proven `requestCanId` / `responseCanId`; missing IDs remain null rather than guessed.

`OutlanderPhevDecoderResolver` fails closed on ambiguity rather than guessing.

The provider must load every candidate file advertised by the external manifest. Current canonical candidate files are:

```text
data/candidates/outlander_phev_watchdog_resistance.json
data/candidates/outlander_phev_watchdog_cells_and_motor.json
data/candidates/outlander_phev_watchdog_21_05.json
```

The external manifest must report `candidates: 3`.

## DATA LOCATION CHECK BEFORE EVERY EXTRACTION COMMIT

- [ ] Production candidate/provenance -> external `AutoDiag-WiCAN-Diagnostic-Data` only.
- [ ] No new candidate/provenance under local `AutoDiag-WiCAN-Pro/diagnostic-data/`.
- [ ] External manifest count matches actual candidate files.
- [ ] Provider loads every advertised candidate file.
- [ ] Provenance exists before promotion.
- [ ] `VERIFIED` still requires real-vehicle evidence.

If any item fails, stop before continuing the extraction queue.

## TESTING

The decoder tests establish arithmetic correctness. They do not establish ECU addressing or universal vehicle applicability.

Current regression coverage includes:

- ISO-TP first-frame boundary
- multiline ISO-TP payload
- incomplete First Frame rejection
- all extracted resistance decoder variants
- 32-cell voltage scale
- front motor RPM little-endian decoding
- generator RPM exact non-contiguous byte selection
- decoder CAN-ID provenance parsing
- resolver ambiguity/fail-closed behaviour

Mandatory levels:

1. unit test
2. simulator/replay test where practical
3. real-vehicle capture for `VERIFIED`

Static APK evidence can create a candidate, never a verified production signal.

## CI

Workflow:

```text
:core:testDebugUnitTest
-> :app:assembleDebug
-> upload debug APK
```

Run `33945628477` was still `in_progress` when last checked, with `Run core unit tests` active. It must not be called green until the workflow completes successfully. Subsequent source commits require a fresh CI run as well.

## SAFETY

- READ-only extraction.
- No coding/adaptation/actuator/security/write capability is unlocked by APK evidence.
- Community evidence is research input, not OEM authority.
- Never derive Riso MΩ from an OK/fault status.
- A single low cell voltage during load is not proof of a defective cell.
- Missing data is `UNAVAILABLE`/`NOT_AVAILABLE`, not `ERROR`.

## WORK STYLE

Work against current GitHub. Use small commits. Test every decoder/parser change. Update provenance after meaningful extraction. If a link cannot be proven, record `UNRESOLVED` and continue with independently extractable evidence — never invent the missing link.
