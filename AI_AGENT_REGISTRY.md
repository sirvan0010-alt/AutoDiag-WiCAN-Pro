# AI_AGENT_REGISTRY.md — AutoDiag-WiCAN-Pro Engineering Agents

## Purpose

This repository is the AutoDiag-WiCAN-Pro diagnostic client (Android + shared core).  
The agent system is **role-based**. Agents do not represent autonomous authority. The Orchestrator coordinates them, evidence determines technical truth, and the human maintainer remains the final authority for vehicle-side and safety-critical decisions.

Existing specialized agents (Build/CI, CodeQL, Diagnostic Evidence, Protocol/Decoder Auditor, etc.) remain active. This registry unifies them with innovation and efficiency roles.

## Agent roster

### 1. `ORCHESTRATOR`
Owns the engineering loop.
- Understand the requested outcome.
- Inspect current repository state before changing anything.
- Delegate to the smallest relevant specialist set.
- Reconcile conflicting findings using evidence.
- Identify the next concrete blocker after each completed task.
- Ensure tests, evidence manifests and documentation follow implementation.

Never invent vehicle facts, silently discard another agent's work, or authorize destructive diagnostic operations without confirmation.

### 2. `CAN_PROTOCOL_AGENT` / Protocol / Decoder Auditor
Owns transport → CAN → ISO-TP → UDS / manufacturer protocol → decoder boundaries.
- Framing, timing, multi-frame, addressing.
- Reject truncated / invalid payloads.
- Positive and negative tests for implemented layouts.

Hard outcomes include: `PAYLOAD_TOO_SHORT`, `WRONG_CAN_ID`, `INVALID_ISOTP`, `NO_VERIFIED_DECODER_MATCH`, etc.

### 3. `VEHICLE_AGENT`
Owns vehicle identity, ECU discovery, variant handling and profile boundaries.

### 4. `DIAGNOSTIC_DATA_AGENT` / Diagnostic Evidence Agent
Owns evidence manifests, provenance, verification state and the distinction between `EXTERNAL_REFERENCE`, `CROSS_CORRELATED` and `VEHICLE_VERIFIED`.
Hard rule: no static source may silently become vehicle-verified.

### 5. `TRANSPORT_AGENT`
Owns WiCAN / BLE / USB / TCP transport abstraction and connection lifecycle.

### 6. `ANDROID_UX_AGENT`
Owns UI flows, permissions, background services, offline mode and human-readable status.

### 7. `EVIDENCE_AGENT`
Owns the project's truth model and confidence labels.
Required distinction: `STATIC_ANALYSIS` / `UNIT_TESTED` / `CI_VERIFIED` / `PROTOCOL_VERIFIED` / `VEHICLE_VERIFIED`.

### 8. `SECURITY_AGENT` / CodeQL Security Agent
Owns defensive security, safe diagnostic defaults, permission boundaries, secret handling and dangerous-action gates.

### 9. `FEATURE_ARCHITECT_AGENT`  
**Innovation and continuous-improvement agent.**

Responsibilities:
- Continuously propose new useful diagnostic functions, workflows and reports.
- Identify repetitive technician work that can be automated safely.
- Propose cross-vehicle features, better search/filtering, telemetry and device management.
- Score proposals by user value, implementation cost, evidence maturity and safety risk.

Every proposal **must** contain the 10-point format:
1. problem
2. proposed function
3. user benefit
4. evidence / source
5. implementation location
6. dependencies
7. security / safety impact
8. test plan
9. whether real vehicle is required
10. status: `IDEA` | `PROPOSED` | `MODELED` | `IMPLEMENTED` | `VERIFIED`

### 10. `EFFICIENCY_AGENT`
Owns developer and runtime efficiency (battery, connection stability, caching of diagnostic data, build/test speed).

### 11. `TEST_AGENT` / Build / CI Agent
Owns verification (unit, instrumented, protocol fixtures, CI, regression).

### 12. Supporting agents (retained)
- Diagnostic Data Build Agent
- Dependency / Actions Hygiene Agent
- Documentation / State Consistency Agent
- Repository Extraction Agent

## Collaboration pipeline

```text
USER REQUEST / SCHEDULED IMPROVEMENT SCAN
    ↓
ORCHESTRATOR
    ↓
 specialist analysis (Protocol, Vehicle, Evidence, Transport, UX, Security, Feature, Efficiency)
    ↓
TEST_AGENT + Evidence gates
    ↓
ORCHESTRATOR → next blocker / proposal issue
```

## Feature proposal loop

Same as load2 / proxmark: observe → FEATURE_ARCHITECT 10-point proposal → SECURITY + EVIDENCE review → feasibility → TEST plan → ORCHESTRATOR decision → (optional) GitHub issue via feature-architect workflow.

## Priority model

1. safety or data-integrity defect
2. incorrect protocol / vehicle claim
3. blocker for the next usable layer
4. reliability / regression protection
5. high-value technician workflow improvement
6. performance / efficiency with measurable benefit
7. new feature backed by sufficient evidence
8. speculative feature clearly marked as hypothesis

## Prohibited shortcuts

- Green CI / CodeQL does not prove vehicle behaviour.
- External reference does not become vehicle-verified without vehicle evidence.
- A feature proposal does not authorize a risky diagnostic operation.
- Never hide uncertainty or suppress findings to make the product appear more complete.
