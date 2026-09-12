# AI Master Working Protocol

## Source of truth

The GitHub repositories are the durable source of truth. ChatGPT conversations are working sessions, not the canonical project state.

Primary repository:
- `sirvan0010-alt/AutoDiag-WiCAN-Pro`

Diagnostic-data repository:
- `sirvan0010-alt/AutoDiag-WiCAN-Diagnostic-Data`

## User command convention

When the user says `@GitHub pokračuj`, continue from the current GitHub state and the committed plans/manifests. Do not restart the audit and do not ask the user to restate the plan unless a required decision is genuinely missing.

## Chat strategy

### One master chat

Use one main conversation for cross-repository decisions, architecture, priorities, evidence rules and APK integration.

### Focused chats only when needed

A separate conversation is useful for a repository that requires a long independent investigation. Examples:
- tm3diag
- tesla_can_signals
- OBDb/Tesla-Model-Y
- TeslaLogger
- S3XY-BMS

A focused chat must commit its conclusions/artifacts to GitHub. The master chat then consumes the committed result.

### No one-chat-per-repository rule

Do not create dozens of chats just because there are dozens of repositories. That creates fragmented state and makes cross-source verification harder.

## Work loop

For each source:

1. Inspect current repository state and revision.
2. Identify concrete useful artifacts, not only README claims.
3. Extract or translate the useful concept/data.
4. Preserve provenance.
5. Cross-correlate against independent sources.
6. Assign verification state.
7. Add tests where behavior is implemented.
8. Commit the result.
9. Continue to the next parallel track.

## Verification states

- `EXTERNAL_REFERENCE` — found in an external repository; not independently verified.
- `CROSS_CORRELATED` — supported by an independent second source or compatible capture.
- `VEHICLE_VERIFIED` — confirmed using actual vehicle evidence/reproducible vehicle test.

Never promote a static GitHub signal directly to `VEHICLE_VERIFIED`.

## Architecture rule

External projects are reference architecture, not runtime dependencies.

Target:

`capture -> decode -> hunt -> correlate -> investigate`

Transport/diagnostic boundary:

`SLCAN/raw -> ISO-TP -> UDS -> decoder -> evidence -> DTC history`

Android/Kotlin is the implementation target for AutoDiag. Python/C++/Go projects should be translated conceptually rather than mechanically copied.

## Safety rules

AutoDiag extraction is read-only by default.

Do not import:
- autonomous vehicle control;
- ECU flashing;
- seed/key or immobilizer bypass;
- automated purchasing/payment handling;
- bot-detection bypass;
- battery contactor/balancing actuation.

## Current parallel priority

P0 tracks are active in parallel with APK implementation. Extraction must not block the working application.

See:
- `AI_TESLA_REPOSITORY_EXTRACTION_PLAN.md`
- `AI_TESLA_REPOSITORY_EXTRACTION_ADDENDUM.md`
- `TESLA_P0_EXTRACTION_MANIFEST.md` in the diagnostic-data repository

## Next execution order

1. Complete tm3diag architecture extraction.
2. Build Model Y signal inventory from `tesla_can_signals`.
3. Build Model Y generation/schema inventory from OBDb.
4. Cross-correlate the two Model Y datasets.
5. Add FleetWise/TeslaLogger evidence models.
6. Map CANdash/Tesladash concepts to Android live inspection UI.
7. Extract S3XY-BMS read-only observation/test concepts.
8. Continue with P1 repositories.

A task is complete only when the repository contains a durable artifact or an explicit reference-only decision.
