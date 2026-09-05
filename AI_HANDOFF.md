# AI_HANDOFF.md — AutoDiag-WiCAN-Pro

Živý handoff pro AI a vývojáře. Není roadmapa. Před prací vždy znovu ověř aktuální HEAD této větve a `main` — tento soubor může být o commit pozadu.

---

## PROJECT

AutoDiag-WiCAN-Pro — open, modulární Android automotive diagnostická a automatizační platforma nad **WiCAN PRO** (ESP32, meatpiHQ/wican-fw).

- WiCAN PRO = hardware / interface / firmware
- AutoDiag = diagnostická a automatizační vrstva nad ním

## READ FIRST

Před architekturou nebo změnou diagnostického jádra čti:

1. `README.md`
2. `AI_CONTEXT.md`
3. `ROADMAP.md`
4. `docs/ARCHITECTURE_OVERVIEW.md`
5. `docs/DIAGNOSTIC_KNOWLEDGE_BASE.md`
6. `docs/LONG_TERM_FEATURE_PRESERVATION.md`
7. `docs/AI_APK_EXTRACTION_GUIDE.md` — **povinné pro APK/reverse-engineering extraction**

---

## ARCHITECTURAL PRINCIPLE

Nebudeme optimalizovat projekt na „co nejjednodušší implementaci“. Stavíme rozsáhlý diagnostický systém v pořadí:

```text
hardware → transport → evidence → diagnostika → automatizace → analýza → UI
```

Vždy rozlišovat:

1. co umí WiCAN PRO
2. co poskytuje konkrétní auto
3. co umíme bezpečně přečíst
4. co umíme odvodit (inference)
5. co je experimentální / reverse-engineered
6. co ještě nemáme ověřené

AI nesmí smazat plánovanou funkci jen proto, že ji teď nelze implementovat. Použít `BLOCKED: <důvod>`.

---

## CURRENT OUTLANDER EXTRACTION STATE

Aktivní extraction target je **Mitsubishi Outlander PHEV**. Nešiř scope bez explicitního požadavku.

U PHEV Watchdog Lite 1.9.1.2023OCT29 byly potvrzeny command literals:

```text
21 01, 21 02, 21 03, 21 04, 21 05, 21 11, 21 14, 21 15,
21 23, 21 24, 21 25, 21 26,
22 01 01, 22 01 02, 22 01 03, 22 01 04, 22 01 05, 22 B0 02
```

Důležité již potvrzené decoder behaviour:

- `21 01` / `Lz3a`: `ISOLATION_RESISTANCE = d[78]*256+d[79]`, kΩ
- `21 02` / `Lz3b`: 32 cell-voltage outputs, `d[i]/50.0`, V
- `21 03` / `Lz3c`: cell-voltage outputs, `d[i]/50.0`, V
- `21 03` / `Lz3e`: `FRONT_MOTOR_RPM = d[31]*256+d[30]`
- `21 03` / `Lz3e`: `GENERATOR_RPM = d[29]*256+d[26]` — **non-contiguous indices; use `responseIndices`**

`21 01`, `21 02` and `21 03` have tests in AutoDiag. These tests prove decoder arithmetic, not universal ECU applicability.

### Important unresolved transport mapping

Public Outlander CAN evidence correlates:

```text
BMU       0x761 -> 0x762   21 01   strong community correlation
rear motor 0x753 -> 0x754  candidate
front motor 0x755 -> 0x756 candidate
generator 0x73C -> 0x73D   candidate
```

Do **not** bind Watchdog `Lz3e` to a motor/generator address merely from signal names. Direct APK transport tracing or reproducible vehicle capture is required.

`21 03` cells and motor/generator variants remain address-unresolved unless new evidence is found.

---

## REQUIRED EXTRACTION CHAIN

Every APK-derived diagnostic item must follow:

```text
ECU/address → variant/model → request → response → normalized d[]
→ decoder → unit/scale/offset/signedness/byte order
→ test → provenance → candidate → vehicle validation → production
```

Never skip an unresolved link. Use `UNRESOLVED`, `CANDIDATE`, or `BLOCKED`, never a guessed value.

See `docs/AI_APK_EXTRACTION_GUIDE.md` for the detailed procedure.

### Parser boundary is critical

Watchdog decoder indices refer to normalized diagnostic payload bytes. They do **not** include:

- CAN arbitration/header bytes
- ISO-TP PCI bytes
- first-frame length bytes
- consecutive-frame sequence numbers
- diagnostic positive-response service/PID prefix

AutoDiag's `OutlanderPhev21ResponseParser` currently performs this normalization for the `21 xx` Watchdog family. Add tests before changing the boundary.

### Variant rule

The same request may have multiple decoder classes. If variants disagree, require an explicit `variantId`; do not guess. Resolver behaviour must fail closed on ambiguity.

### Candidate rule

Static APK extraction can create a diagnostic-data **candidate**. It cannot make a signal `VERIFIED`.

`VERIFIED` requires reproducible vehicle evidence tying together ECU identity/address, request, response payload, decoder variant and vehicle/software scope.

---

## DIAGNOSTIC-DATA

The normalized data repository is:

`AutoDiag-WiCAN-Diagnostic-Data`

Current manifest reports `candidates: 2`. The provider loads both Outlander Watchdog candidate JSON files:

```text
data/candidates/outlander_phev_watchdog_resistance.json
data/candidates/outlander_phev_watchdog_cells_and_motor.json
```

Do not duplicate signal maps in Kotlin when the data-driven decoder can represent them.

For non-contiguous bytes use `responseIndices`, never a fake contiguous range.

---

## SAFETY / VERIFICATION

- READ has priority over WRITE.
- No coding/adaptation/actuator/security/write capability may be enabled by static APK extraction.
- Community evidence is research input, not OEM authority.
- Do not infer an MΩ Riso value from an OK/fault status.
- A single low cell voltage under load is not proof of a defective cell.
- Missing data is `NOT_AVAILABLE` / `UNAVAILABLE`, not `ERROR`.
- Production thresholds require explicit evidence and scope.

---

## CI

The Android workflow now runs:

```text
gradle :core:testDebugUnitTest --stacktrace
gradle :app:assembleDebug --stacktrace
```

Tests must pass before the debug APK is built/uploaded.

Recent CI failure was caused by stale tests referring to removed APIs (`DtcKnowledgeEntry`, obsolete resistance aggregator APIs) and an `Int`/`Byte` literal mismatch in `CanFrameTest`. Those tests have been migrated to the current APIs. Always inspect the latest run before claiming the build is green.

---

## CURRENT TASK ORDER

Continue autonomously in this order:

1. Confirm latest CI run is green after the test migrations.
2. If CI fails, fix the actual failure and rerun; do not mask failures.
3. Continue forensic extraction from the local PHEV Watchdog APK for the remaining commands:

```text
21 04, 21 05, 21 11, 21 14, 21 15, 21 23, 21 24, 21 25, 21 26,
22 01 01, 22 01 02, 22 01 03, 22 01 04, 22 01 05, 22 B0 02
```

4. For each command complete `variant → request → response → decoder → test` before promoting it.
5. Trace `ATSH` and `CAN_RECEIVE_ADDRESS` to establish ECU/address context wherever possible.
6. Add only evidence-backed candidates to Diagnostic-Data.
7. Do not promote a candidate to verified/production without real-vehicle evidence.
8. Update provenance after each meaningful extraction step.
9. Keep this handoff and `docs/AI_APK_EXTRACTION_GUIDE.md` synchronized with the actual repository state.

---

## WORK STYLE

- Work against current GitHub, not an old ZIP.
- Small reviewable commits.
- Tests with every decoder/parser change.
- Documentation = specification; code = implementation; simulator/replay = deterministic validation; vehicle = final validation.
- Never claim `AVAILABLE` merely because code exists.
- Never invent missing ECU/address/decoder links.
