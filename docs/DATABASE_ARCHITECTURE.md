# AutoDiag Diagnostic Database Architecture

## Goal

AutoDiag uses an offline-first diagnostic knowledge base. The Android application must be able to decode and explain known diagnostic information without an Internet connection.

The database is contextual: the interpretation of a diagnostic item may depend on manufacturer, platform, model, model year, ECU, software/firmware variant and verification scope.

## Storage model

The Android application should use a versioned SQLite database, exposed through Android Room where appropriate.

```text
AutoDiag
└── Local Diagnostic Knowledge Base
    ├── generic DTC
    ├── manufacturer DTC
    ├── OEM / UDS identifiers
    ├── ECU definitions
    ├── vehicle/platform profiles
    ├── diagnostic tests
    ├── thresholds and units
    ├── symptoms and possible causes
    ├── procedures
    ├── sources
    ├── translations
    └── verification metadata
```

## Context-aware records

A diagnostic record may be scoped by:

- manufacturer
- platform
- model
- model-year range
- ECU
- software/firmware generation
- protocol

A generic code must not automatically override a more specific OEM record.

## Verification states

Every technical interpretation must carry a verification state:

- `VERIFIED` — reproducibly validated for the stated scope
- `PARTIALLY_VERIFIED` — independently supported but incomplete
- `UNVERIFIED` — source exists but behavior is not independently confirmed
- `INTERPRETATION` — AutoDiag-derived interpretation, not an OEM fact

Missing information must never be fabricated.

## User-facing unavailable state

Internal data may use `NOT_AVAILABLE`, but the Android UI must not expose that technical enum directly.

The user-facing Czech text is:

**Vozidlo údaj neposkytlo.**

Tooltip:

> AutoDiag se pokusil údaj vyčíst, ale vozidlo nebo příslušná řídicí jednotka jej pro tuto konfiguraci neposkytla. Samo o sobě to neznamená závadu.

## Database packs

The base application contains a small generic database. Manufacturer/platform packs can be installed separately:

```text
Generic
Tesla
VAG
BMW
Mercedes-Benz
Hyundai-Kia
Toyota
...
```

The user selects desired packs during first-run setup and can change them later. Installed packs are stored locally and remain available offline.

## Updates

Database packs are versioned independently from the Android application. A manifest identifies:

- pack ID
- version
- schema version
- minimum application version
- release timestamp
- SHA-256 checksum
- download location
- verification metadata

The application periodically checks the manifest when Internet access is available. It downloads only selected packs and verifies their checksum before activation.

Updates are atomic: a failed or corrupt update must leave the previous working database active.

## Source policy

OEM/service information, regulatory sources and community reverse engineering must remain distinguishable. A community finding must not be displayed as an OEM-confirmed repair procedure.

## Automated publishing

GitHub Actions can validate and package database packs on repository changes and on a schedule. Scheduled workflows can run periodically on the default branch. The workflow should:

1. validate source data;
2. run schema and consistency tests;
3. build SQLite packs;
4. calculate SHA-256 checksums;
5. generate a manifest;
6. publish versioned artifacts/releases.

GitHub Actions supports scheduled workflows and manual dispatch; scheduled workflows run against the latest commit on the default branch. See GitHub Actions documentation for the exact scheduling behavior.

## Important limitation

GitHub cannot legitimately invent or verify new automotive DTC knowledge by itself. The automation can validate, transform and publish data that has been added from approved sources. New technical interpretations require provenance and an explicit verification state.
