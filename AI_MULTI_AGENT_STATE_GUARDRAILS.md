# AI Multi-Agent State Guardrails

Status: ACTIVE

This document exists to prevent one AI agent from accidentally overwriting, shortening, restarting or contradicting work already performed by another AI agent.

## 1. Authority order

For execution state, use this order:

1. current GitHub tree and committed durable artifacts;
2. `AutoDiag-WiCAN-Diagnostic-Data/TESLA_P0_EXTRACTION_MANIFEST.md`;
3. source-specific extraction records under `tesla/sources/`;
4. implementation/tests already committed;
5. CI and security results;
6. `AI_MASTER_WORKING_PROTOCOL.md`;
7. `AI_TESLA_REPOSITORY_EXTRACTION_PLAN.md`;
8. chat/session discussion.

A lower item cannot silently override a higher item.

## 2. Detect stale instructions

If a planning document says a repository is `NEXT`, `PENDING` or `ACTIVE`, first verify whether the manifest and source artifact already mark that repository complete. If so, treat the planning statement as stale and continue from the manifest.

Never repeat a completed extraction only because an older plan still says it is next.

## 3. Detect destructive rewrites

When editing AI instructions, preserve:

- completed repository decisions;
- extraction artifacts;
- known signal conflicts;
- verification states;
- provenance requirements;
- current execution order;
- explicit safety boundaries;
- newly discovered repositories/dependencies;
- security findings and accepted risk decisions.

A shorter document is not an improvement if it removes project state.

If a rewrite would remove historical detail, add an addendum instead of replacing the detailed record.

## 4. Detect work performed by another AI

All agents may use the same GitHub account, so GitHub author/committer identity is not proof of which AI performed a change.

Therefore, any unexpected recent commit must be audited by content and diff, not by author name.

For each unexpected change:

1. inspect the changed files;
2. compare them with the manifest;
3. check whether completed artifacts still exist;
4. identify stale, contradictory, destructive or useful content;
5. inspect applicable CI/security results;
6. repair contradictions immediately;
7. preserve useful discoveries.

Do not delete valid prior work merely because its commit author is the same shared account.

## 5. Extraction duplication rule

Before extracting a repository, search for an existing source-specific artifact. If it exists, deepen or correct it instead of creating a competing duplicate artifact.

## 6. Evidence rule

Static GitHub data is `EXTERNAL_REFERENCE` by default. `CROSS_CORRELATED` requires independent technical agreement. `VEHICLE_VERIFIED` requires actual vehicle evidence or an independently reproducible vehicle test.

Cloud API, CAN, UDS, BLE, firmware, UI and checklist evidence remain separate domains until an explicit technical correlation exists.

## 7. Universal AutoDiag scope

AutoDiag is a universal vehicle interface platform. Do not narrow it to read-only diagnostics or passive CAN sniffing.

Legitimate scope includes:

- diagnostics;
- CAN/OBD/ISO-TP/UDS and manufacturer protocols;
- CAN capture/analysis;
- live vehicle data;
- Android user application/dashboard/history;
- Wi-Fi/BLE/USB and other transports;
- vehicle profiles and decoders;
- user-authorized vehicle control where technically and safely supported.

Control is classified as `CONTROL`, not automatically rejected. Safety-critical and irreversible commands require dedicated capability, policy, evidence, testing and audit layers.

## 8. Agent separation and gates

Specialist agents must stay within their authority. The persistent role registry is `AI_AGENT_ARCHITECTURE.md`.

Minimum roles:

- Build/CI;
- CodeQL Security;
- Diagnostic Evidence;
- Protocol/Decoder Audit;
- Diagnostic Data Build;
- Dependency/Actions Hygiene;
- Documentation/State Consistency;
- Repository Extraction.

The gate chain is:

`BUILD -> TEST -> SECURITY -> DATA/EVIDENCE -> PROTOCOL/DECODER -> RELEASE`

A specialist may report its own result but may not promote that result into another domain's verification state. In particular:

- CodeQL green does not mean protocol verified;
- build green does not mean vehicle verified;
- evidence validated does not mean a decoder is vehicle-correct;
- an external repository does not become an AutoDiag dependency merely because an agent extracted useful code.

## 9. Security automation rule

CodeQL Advanced Setup is part of the durable project structure. The workflow is `.github/workflows/codeql.yml`.

Security automation should use least-privilege permissions and maintained action versions. Security findings must not be silently suppressed. Any accepted risk requires a durable decision record.

Security analyzers may report vulnerabilities, workflow risks and unsafe data flows. They do not replace manual review of vehicle-control or diagnostic semantics.

## 10. Continuous execution

Do not ask for confirmation between extraction steps unless there is a genuinely blocking technical decision.

After each meaningful discovery:

1. create/update durable evidence;
2. commit it;
3. update the manifest;
4. run applicable gates;
5. continue automatically.

When the initial queue is exhausted, perform a second pass for cross-correlation, contradictions, security findings and newly discovered repositories/dependencies, then continue the cycle.
