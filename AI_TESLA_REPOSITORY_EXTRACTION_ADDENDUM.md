# Tesla Repository Extraction — Addendum and Operating Workflow

## 1. Correction to the original repository list

The supplied repository `gucluceyhan/tesla-sr-bot` was omitted from the first version of `AI_TESLA_REPOSITORY_EXTRACTION_PLAN.md`. This addendum restores it so the full user-supplied set is represented.

### `gucluceyhan/tesla-sr-bot` — P2 / reference only

Verified from its README: this is a Python/Streamlit Tesla Model Y Standard Range inventory/order bot. It polls Tesla's inventory API, filters SR vehicles by color and price, automates form filling with Selenium, has logging and a Streamlit UI, and contains bot-detection avoidance techniques. The README also states that card data is stored unencrypted and explicitly warns against production use.

**Decision:** retain as historical reference only. It is not a CAN/UDS/diagnostic source and must not be integrated into AutoDiag-WiCAN-Pro. The useful part is limited to inventory filtering/UI/logging patterns if a future non-diagnostic Tesla inventory feature is requested.

**Safety:** do not extract or operationalize payment-card handling, bot-detection bypasses, or automated purchasing. fileciteturn63file0L2-L6

## 2. Recommended chat structure

Do **not** create one ChatGPT conversation per GitHub repository by default. That would fragment the architecture, decisions and evidence state.

Use this hierarchy instead:

### Chat A — MASTER / orchestration

Keep one main chat for `AutoDiag-WiCAN-Pro` as the source of truth for:
- architecture;
- repair plan;
- priorities;
- cross-repository extraction;
- verification/evidence policy;
- decisions that affect multiple repositories;
- APK integration order;
- final acceptance criteria.

This is the chat where the user can simply say `@GitHub pokračuj` and the work should continue from the repository state.

### Chat B — optional focused extraction chats

Create a separate chat only when a repository needs a long independent investigation, for example:
- `tm3diag` deep extraction;
- `tesla_can_signals` Model Y signal reconstruction;
- `OBDb/Tesla-Model-Y` schema/validation;
- `TeslaLogger` telemetry/history model;
- `S3XY-BMS` BMS observation model.

A focused chat must never become a competing source of truth. Its results must be committed back to the relevant repository as evidence/manifests/code and then summarized in the master chat.

### Chat C — APK implementation

Keep Android/Kotlin implementation work together. External repositories provide concepts/data/evidence; they are not copied wholesale into the APK.

## 3. How we will work

The repository, not a chat transcript, is the durable memory.

Every meaningful discovery must end in one of:
1. source/data manifest;
2. evidence record;
3. implementation commit;
4. explicit rejected/archived decision.

Each extracted item should preserve:
- source repository;
- source path;
- source revision/commit SHA;
- extraction date;
- extraction method;
- confidence;
- `VerificationState` mapping;
- whether the item is Model Y-specific or only Model 3/general Tesla evidence;
- target AutoDiag module;
- notes about conflicts with other sources.

## 4. Parallel extraction policy

P0 extraction runs in parallel with current APK/diagnostic implementation. It must not block the usable application architecture.

Initial parallel tracks:

**Track A — transport/diagnostics:** `outlandnish/tm3diag`

**Track B — Model Y signal data:** `talas9/tesla_can_signals` + `OBDb/Tesla-Model-Y`

**Track C — telemetry/history:** `bassmaster187/TeslaLogger` + `tfoldi/fleetwise-iot-tesla3`

**Track D — Android/live dashboard:** `ekr/candash` + `tomas7470/tesladash`

**Track E — BMS/read-only observation:** `clowrey/S3XY-BMS`

**Track F — firmware/artifact evidence:** `evoffer/instrument-cluster-firmware`

**Track G — cloud vehicle-state vocabulary:** `timdorr/tesla-api` + `barnybug/tesla-cli`

## 5. Non-negotiable evidence rules

- A repository claim is not vehicle verification.
- A Model 3 DBC is not automatically a Model Y truth source.
- A signal becomes vehicle-verified only through independent correlation with captures, multiple independent sources, or an actual vehicle test.
- Never silently overwrite a conflicting signal definition.
- Keep conflicting definitions side-by-side until resolved.
- Do not import autonomous-control logic into AutoDiag.
- Do not import ECU flashing/security-access functionality into AutoDiag without a separate explicit safety project.
- Default diagnostic operations to read-only.

## 6. Target architecture

`capture -> decode -> hunt -> correlate -> investigate`

with strict boundaries:

`SLCAN/raw transport -> ISO-TP -> UDS -> signal decoding -> evidence -> DTC history`

External projects are reference architecture, not runtime dependencies.

## 7. Definition of done for extraction

An external repository is considered extracted only when its useful artifacts have either:
- been converted into an AutoDiag-compatible data/manifest format;
- been implemented as a Kotlin/Android concept with tests;
- or been documented as a deliberate reference-only source.

Nothing is considered complete merely because a README was reviewed.
