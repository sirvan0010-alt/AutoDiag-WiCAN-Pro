# AutoDiag diagnostic-data schema v1

This directory defines the **public metadata contract** for the optional diagnostic knowledge repository. It deliberately stores definitions and provenance, not proprietary HaynesPro/VCDS database payloads.

## Files

- `manifest.json` — dataset version, schema version, source classes and integrity metadata.
- `vehicles.json` — vehicle identities and optional VIN-scoped definitions.
- `ecus.json` — ECU identities and verified semantic names.
- `signals.json` — live/measuring-value definitions.
- `dtcs.json` — DTC descriptions and system ownership.

## Common rules

1. Every record has a stable `id` and `verification` state.
2. `provenance` identifies the source class and dataset revision.
3. A database match never implies that the physical vehicle supports the feature.
4. Unknown data remains unknown; the app must not synthesize values from a missing record.
5. Manufacturer-specific requests require an explicit verified definition before they can be sent.
6. Read-only metadata may be cached locally; cache age and dataset version must remain observable.
7. Raw proprietary `.rod` files, extracted commercial database payloads, credentials and copied vendor assets are excluded.

## Verification values

- `UNVERIFIED` — imported/observed but not independently validated.
- `PARTIALLY_VERIFIED` — source and decoding are supported, but vehicle-level validation is incomplete.
- `VERIFIED` — decoding/meaning is backed by an authoritative public specification or reproducible vehicle evidence.

## Signal request policy

A `request` field is descriptive until the diagnostic engine independently verifies transport, ECU addressing, session state and safety policy. Presence in this repository must never by itself authorize a write/control operation.
