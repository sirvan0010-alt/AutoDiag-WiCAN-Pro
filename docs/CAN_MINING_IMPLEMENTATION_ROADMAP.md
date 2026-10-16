# CAN-mining implementation roadmap

Status: planning baseline for the read-only CAN-mining milestone.
Related data contract: [private AutoDiag-WiCAN-Diagnostic-Data repository](https://github.com/sirvan0010-alt/AutoDiag-WiCAN-Diagnostic-Data), plan at `docs/CAN_MINING_PIPELINE_PLAN.md` and schemas under `schema/can-mining-*.schema.json`.

## Objective

Add a testable, evidence-first pipeline to ingest passive CAN captures, compare baseline/event windows, detect signal/counter/checksum candidates, validate hypotheses offline, and emit descriptive evidence packs. The app remains useful without the private data repository.

## Architecture

```text
CaptureSource (read-only)
  -> CaptureIngest / RawArtifactStore
  -> NormalizedFrameStream
  -> EventWindowDiff
  -> CandidateScoring
  -> CounterDetector / ChecksumCandidateValidator
  -> OfflineValidationReport
  -> EvidencePackWriter
  -> Diagnostic-Data schemas + review/CI
```

## Safety boundary

- First milestone is strictly read-only.
- Do not add CAN transmit, replay-to-vehicle, vehicle wake, actuator commands, fuzzing, security bypass, or firmware writing.
- Offline replay is only against local test fixtures and parser/decoder code.
- Tesla API data, when available, may annotate events or corroborate state; it does not prove a specific CAN frame caused the state.
- Do not infer vehicle-specific signal meaning, scaling, DLC or firmware applicability without evidence.
- Do not commit API tokens, private vehicle telemetry, or proprietary source payloads.

## Module ownership

| Module | Responsibility |
| --- | --- |
| `capture/CaptureSource` | Read-only source adapter; no transmit capability in its interface. |
| `capture/CaptureMetadata` | Capture timestamps, interface, format, clock source, known bus parameters and artifact hash. |
| `capture/CaptureIngest` | Parse one confirmed input format, validate records and preserve original artifact. |
| `analysis/EventWindowDiff` | Compare baseline and event windows; explain each candidate change. |
| `analysis/CounterDetector` | Deterministic counter candidates, wraparound and repeated-capture checks. |
| `analysis/ChecksumCandidateValidator` | Bounded allowlist of algorithms, independent train/held-out checks and ambiguity reporting. |
| `validation/OfflineValidationReport` | Reproducible results linked to input hashes and tool versions. |
| `evidence/EvidencePackWriter` | Emit non-executable evidence packs compatible with Diagnostic-Data schemas. |

Names above are target responsibilities, not claims that these classes already exist. Follow the current project language/package conventions after inspecting the repository before implementing.

## Milestones

### M1 — capture ingest (start here)
- Inventory current WiCAN connection and logging code plus real sample captures.
- Confirm actual stream framing and timestamp source before selecting a parser.
- Add immutable capture metadata and a read-only parser adapter for one confirmed format.
- Preserve the raw input and compute SHA-256.
- Add tests for malformed records, DLC bounds, timestamp order, empty capture and unknown metadata.
- Do not add a new dependency unless current project conventions and test setup require it.

### M2 — event-window diff
- Configurable baseline and event windows; ±5 seconds is a starting default, not a hard-coded assumption.
- Account for timestamp drift, capture loss and concurrent traffic.
- Produce candidate scores and the exact frame/evidence references supporting them.

### M3 — counter and checksum candidates
- Counter analysis uses repeated captures, bit-level deltas and wraparound checks.
- Checksum validation uses allowlisted known algorithms; LLM output is only a hypothesis and never executes as trusted production code.
- Require independent held-out captures and report ambiguity/coverage.

### M4 — evidence packs and schemas
- Emit non-executable JSON packs with provenance, source revision, hashes, validation versions and limitations.
- Validate generated records against the three CAN-mining JSON Schemas in Diagnostic-Data.
- Never promote a source-only candidate directly to VEHICLE_VERIFIED.

### M5 — source sync and firmware regression
- Pin sources and emit meaningful diffs instead of silently replacing records.
- Re-run offline tests when firmware/source revisions change.
- Mark affected records STALE or NEEDS_REVALIDATION.

## Definition of done for M1

1. A sample capture can be ingested without modifying its source bytes.
2. The artifact SHA-256 and capture metadata are recorded.
3. Valid frames are normalized deterministically; invalid records are rejected or reported with reasons.
4. Automated tests cover the supported format and malformed input.
5. No code path can transmit frames or issue vehicle commands.
6. Documentation states format assumptions and unknown metadata explicitly.

## Immediate work queue

1. Inspect current repository structure and identify the existing WiCAN read path and test conventions.
2. Inspect available sample capture files and confirm format, timestamps, CAN ID representation and DLC encoding.
3. Implement the smallest M1 vertical slice and tests.
4. Record results and next blockers in this document and the data repository extraction ledger.

## Data contract

CAN-mining schemas and provenance rules live in the separate Diagnostic-Data repository. Runtime must treat imported data as reference knowledge, preserve verification status, and operate with local validated cache when the private repository is unavailable.
## Implementation log

### 2026-10-16 — initial implementation slice

- Repository inspection confirmed an existing versioned capture artifact format (`autodiag-can-capture-v1`), JSON encode/decode support, bounded in-memory capture, CSV export and offline replay. These are reused; no duplicate capture format was introduced.
- Added `CanMiningDiff.kt`, a deterministic offline baseline/event-window comparator. It groups by CAN ID, extended/remote flags and DLC; reports event-only layouts, new payload variants and byte positions whose observed value sets differ.
- Added `CanMiningDiffTest.kt` for event-only identifiers, changed byte positions, identical observations, out-of-window exclusion and overlapping-window rejection.
- The diff engine is a candidate finder only: it has no transport/TX API and does not assign signal semantics or prove causality.
- Test/build execution has not yet been confirmed in this session. Do not label the implementation CI-verified until the branch workflow reports success.

Next: run the Android core unit tests and debug build on this branch, fix any failures, then add capture provenance/hash metadata and the first real artifact-ingest boundary.