# AutoDiag AI / Automation Agent Architecture

Status: ACTIVE

This document defines the persistent roles used to maintain AutoDiag-WiCAN-Pro. The word `agent` includes AI agents, GitHub Actions automation and specialist analyzers. An automated analyzer is not treated as an AI authority; it produces findings that must be interpreted within the project governance model.

## 1. Authority

The repository remains the durable source of truth. Agents must never create a second hidden project state.

Authority order:

1. committed source code and tests;
2. committed evidence/manifests and diagnostic-data repository;
3. CI results and security findings;
4. AI instructions and plans;
5. chat/session discussion.

If an agent discovers a contradiction, it must preserve the evidence and record the contradiction instead of silently choosing a convenient value.

## 2. Agent roles

### A. Build / CI Agent

Purpose:
- compile Android modules;
- run unit tests;
- build debug APKs where configured;
- report exact failing task and commit SHA.

Authority:
- build correctness only.

It may not declare vehicle behavior verified merely because compilation is green.

### B. CodeQL Security Agent

Purpose:
- static security analysis of Java/Kotlin and relevant repository code;
- detect data-flow vulnerabilities, unsafe APIs and security defects;
- analyze GitHub Actions workflow security where applicable.

Implementation:
- GitHub CodeQL Advanced Setup;
- `github/codeql-action/init@v4`;
- `github/codeql-action/analyze@v4`;
- manual Gradle build for compiled Kotlin/Java code.

Reference:
- https://docs.github.com/en/code-security/how-tos/find-and-fix-code-vulnerabilities/configure-code-scanning/configuring-advanced-setup-for-code-scanning
- https://github.com/github/codeql-action

CodeQL findings are security findings, not proof of a vehicle protocol fact.

### C. Diagnostic Evidence Agent

Purpose:
- validate evidence manifests;
- check provenance and verification state;
- detect unsupported claims;
- enforce the distinction between `EXTERNAL_REFERENCE`, `CROSS_CORRELATED` and `VEHICLE_VERIFIED`.

Hard rule:
- no static source may silently become vehicle-verified.

### D. Protocol / Decoder Auditor

Purpose:
- audit transport -> CAN -> ISO-TP -> UDS/manufacturer protocol -> decoder boundaries;
- check declared ISO-TP length;
- reject truncated payloads;
- verify CAN addressing and response semantics;
- detect decoder reads outside the available payload;
- require positive and negative tests for implemented layouts.

Hard outcomes include:
- `PAYLOAD_TOO_SHORT`;
- `WRONG_CAN_ID`;
- `WRONG_RESPONSE`;
- `INVALID_ISOTP`;
- `UNKNOWN_LAYOUT`;
- `INVALID_FIELD`;
- `NO_VERIFIED_DECODER_MATCH`.

### E. Diagnostic Data Build Agent

Purpose:
- validate external diagnostic-data manifests;
- build/normalize generated diagnostic catalogs;
- ensure candidate-file lists and generated artifacts remain consistent;
- produce deterministic artifacts where applicable.

The MeatPi/WiCAN vehicle-profile pattern is used as an architectural reference: validate source fragments first, then generate a deterministic merged runtime artifact. AutoDiag must retain provenance and must not copy that repository's exact implementation blindly.

### F. Dependency / Actions Hygiene Agent

Purpose:
- review GitHub Actions versions;
- detect obsolete action versions;
- identify risky workflow permissions;
- review dependency updates;
- keep security tooling current.

Preferred practice:
- use maintained major versions such as CodeQL Action `v4` unless a documented compatibility reason requires otherwise;
- use least-privilege workflow permissions;
- review action pinning and untrusted checkout boundaries.

### G. Documentation / State Consistency Agent

Purpose:
- detect stale AI instructions;
- detect duplicated or contradictory plans;
- ensure implementation changes are reflected in durable documentation;
- preserve historical evidence rather than shortening it away.

This role follows `AI_MULTI_AGENT_STATE_GUARDRAILS.md`.

### H. Repository Extraction Agent

Purpose:
- inspect external repositories for useful transport, protocol, UI, control, testing and architecture patterns;
- classify findings as `KEEP`, `ADAPT`, `VEHICLE_SPECIFIC`, `CONTROL`, `REFERENCE` or `REJECTED`;
- preserve source revision, path and provenance;
- cross-correlate before promoting confidence.

External repositories are reference/evidence sources, not automatic dependencies.

## 3. Agent handoff protocol

Every agent run should leave one durable result:

- code/test commit;
- evidence record;
- manifest update;
- CI/security result;
- explicit decision or contradiction record.

An agent must report:

- repository and branch;
- exact commit SHA analyzed;
- files changed or inspected;
- tests/checks executed;
- result;
- unresolved findings;
- verification level where relevant.

## 4. Gate model

The project uses separate gates rather than one generic green light:

`BUILD -> TEST -> SECURITY -> DATA/EVIDENCE -> PROTOCOL/DECODER -> RELEASE`

A green Build gate does not imply a green Evidence gate.
A green CodeQL scan does not imply a green Protocol gate.
A green Evidence gate does not imply vehicle verification.

Release/merge decisions must consider all applicable gates.

## 5. No silent authority escalation

No agent may:

- turn `EXTERNAL_REFERENCE` into `VEHICLE_VERIFIED` without vehicle evidence;
- replace conflicting candidate layouts without recording the conflict;
- remove negative tests because they fail;
- suppress a security finding without documenting why;
- rewrite AI instructions in a way that deletes durable project state;
- treat a successful build as proof that a decoder is semantically correct.

## 6. Recommended execution order

For normal implementation work:

1. inspect current GitHub state;
2. run/inspect Build and Test;
3. run CodeQL/security checks;
4. validate diagnostic data/evidence;
5. audit protocol/decoder behavior;
6. update durable documentation;
7. commit;
8. re-run affected gates;
9. only then continue to the next architectural task.
