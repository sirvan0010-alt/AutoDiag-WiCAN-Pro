# Diagnostic Knowledge Base

## Purpose

The Diagnostic Knowledge Base (DKB) connects a recognized DTC, alert, warning or diagnostic observation with transparent explanations and trustworthy technical sources. It is a provenance layer, not a collection of AI-generated repair guesses.

## Source priority

1. OEM service manual / official diagnostic documentation
2. OEM service bulletins and official technical documentation
3. Verified vehicle-specific engineering documentation
4. Reproducible community reverse engineering
5. Community discussion / anecdotal information

Lower-level sources must never be displayed as OEM instructions.

## Knowledge entry

Each entry should support:

```text
code / alert ID
vehicle scope
ECU / subsystem
official description
severity
symptoms / effects
possible causes
related measurements
recommended checks
OEM troubleshooting procedure
OEM service / repair reference
community references
source URLs
verification state
last reviewed
```

## DTC / Alert distinction

The decoder must distinguish manufacturer DTCs, vehicle alerts, generic OBD-II DTCs, raw diagnostic responses, AutoDiag observations and AutoDiag inferences. A calculated observation is never silently converted into an OEM DTC.

## UI behavior

When a code is selected:

```text
BMS / CODE

🔴 SAFETY RELEVANT

What does it mean?        >
What can be affected?     >
What should be checked?   >

Official documentation    >
Troubleshooting procedure >
Repair/service procedure  >

Source: OEM
Verification: VERIFIED
```

If an official explanation is not in the database, the UI says so and can show separately labeled community/engineering references. It must not fill the missing OEM explanation with an AI hallucination.

## OEM links

A knowledge entry may contain several references:

```json
{
  "kind": "troubleshooting",
  "title": "OEM troubleshooting procedure",
  "url": "https://...",
  "source_type": "oem",
  "verification": "verified",
  "last_verified": "ISO8601"
}
```

URLs must be stored only when the destination has actually been checked. Do not generate URLs from guessed naming conventions. OEM links are reviewed because manufacturers can move or remove service pages.

For Tesla, prefer official Tesla owner/support material for explanations and official Tesla service information where legitimately available. If a repair procedure is not publicly accessible, the app says so rather than pretending a community guide is an OEM procedure.

## Tesla implementation

Tesla documentation is vehicle-generation-specific. A Model Y procedure must not automatically be shown for every Tesla because the DTC text looks similar. The knowledge entry therefore carries model/year/ECU scope and verification status.

When a matching official source is verified, AutoDiag should expose a direct **Tesla explanation** or **Tesla service information** action. The link is a navigation aid; AutoDiag does not claim that an external page is available unless it has been verified.

## Community knowledge

Community sources are useful for reverse-engineering CAN signals and discovering practical symptoms, but are stored separately:

```text
OEM
 └── official procedure

Engineering
 └── verified reverse-engineered behavior

Community
 └── observed behavior / discussion
```

A community statement such as "this usually means a weak cell" is not sufficient to create a production diagnostic rule.

## Evidence for thresholds

Thresholds use the same evidence model as battery diagnostics. Each threshold records source, source type, vehicle scope, test conditions, confidence and resolution state. No production threshold may be hardcoded solely because it appeared in a forum post.

## Safety-critical classification

At minimum:

- `INFO`
- `ADVISORY`
- `WARNING`
- `SAFETY_RELEVANT`
- `CRITICAL`
- `UNKNOWN`

Safety classification itself requires a source. When unknown, use `UNKNOWN` rather than guessing.

## Repair language rules

Allowed:

> "The OEM procedure identifies the following checks..."

> "This value was reported by the vehicle; AutoDiag did not calculate it."

> "A matching OEM procedure is available."

Not allowed:

> "Replace the battery" — unless that conclusion is explicitly supported by the source and diagnostic conditions.

> "This cell is bad" — from one voltage sample alone.

> "Riso is 8 MΩ" — when the vehicle supplied only an OK/fault status.

## Knowledge-base maintenance

Every entry should record `source_url`, `source_title`, `source_type`, `vehicle_scope`, `verification`, `last_reviewed`, and optional notes. When an OEM website changes or removes a procedure, the entry becomes `needs_review` rather than silently pointing to an unverified replacement.
