# Diagnostic Knowledge Base

## Purpose

AutoDiag should not stop at reporting `DTC = xxx`. For supported vehicles it should connect a diagnostic observation to trustworthy technical information: what the code means, what systems are affected, what should be checked, and where the manufacturer service procedure is documented.

The Knowledge Base is therefore a provenance layer, not a collection of AI-generated repair guesses.

## Source priority

1. OEM service manual / official diagnostic documentation
2. OEM service bulletins and official technical documentation
3. Verified vehicle-specific engineering documentation
4. Reproducible community reverse engineering
5. Community discussion / anecdotal information

Lower-level sources must never be displayed as OEM instructions.

## Knowledge entry

```json
{
  "id": "tesla.bms.example",
  "vehicle_scope": {
    "make": "Tesla",
    "model": "Model Y",
    "years": [2022, 2023]
  },
  "ecu": "BMS",
  "code": "EXAMPLE",
  "type": "DTC",
  "severity": "SAFETY_RELEVANT",
  "official_description": null,
  "meaning": null,
  "possible_causes": [],
  "recommended_checks": [],
  "procedures": [],
  "sources": [],
  "verification": "unverified",
  "last_reviewed": null
}
```

`null` and empty arrays are intentional. They mean the project has not established the information yet.

## DTC / Alert distinction

The decoder must distinguish:

- manufacturer DTC
- vehicle alert
- generic OBD-II diagnostic trouble code
- raw diagnostic response
- AutoDiag observation
- AutoDiag inference

A calculated observation is never silently converted into an OEM DTC.

## UI behavior

When a code is selected:

```text
BMS / CODE

🔴 SAFETY RELEVANT

What does it mean?       >
What can be affected?    >
What should be checked?  >

Official documentation   >
Troubleshooting procedure >
Repair/service procedure  >

Source: OEM
Verification: VERIFIED
```

If an official explanation does not exist in the database:

```text
Official documentation
Not indexed yet

Community references
Available (separate section)
```

The app must not fill the missing OEM explanation with an AI hallucination.

## OEM links

A knowledge entry may contain several references:

```json
{
  "procedures": [
    {
      "kind": "troubleshooting",
      "title": "OEM troubleshooting procedure",
      "url": "https://...",
      "source_type": "oem",
      "verification": "verified"
    },
    {
      "kind": "repair",
      "title": "OEM service procedure",
      "url": "https://...",
      "source_type": "oem",
      "verification": "verified"
    }
  ]
}
```

URLs must be stored only when the destination has been actually checked. Do not generate URLs from guessed naming conventions.

## Tesla implementation

Tesla documentation should be treated as vehicle-generation-specific. A Model Y procedure must not automatically be shown for every Tesla merely because the DTC text looks similar.

Where the Tesla Service Manual provides a troubleshooting or service procedure, AutoDiag should link to the specific relevant procedure. The UI should clearly identify the source as Tesla/OEM.

For safety-critical HV work, the application should show a safety warning and defer to the complete OEM procedure. It must not replace the OEM procedure with a simplified DIY instruction.

## Community knowledge

Community sources are useful for reverse-engineering CAN signals and discovering practical symptoms. They are stored separately:

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

Thresholds use the same evidence model as battery diagnostics. Each threshold has:

- source
- source type
- vehicle scope
- test conditions
- confidence
- resolution state

No hardcoded production threshold may be introduced solely because it appeared in a forum post.

## Safety-critical classification

At minimum:

- `INFO`
- `ADVISORY`
- `WARNING`
- `SAFETY_RELEVANT`
- `CRITICAL`

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

Every entry should record:

- `source_url`
- `source_title`
- `source_type`
- `vehicle_scope`
- `verification`
- `last_reviewed`
- optional `notes`

When an OEM website changes or removes a procedure, the entry should become `needs_review` rather than silently pointing to an unverified replacement.
