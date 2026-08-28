# Diagnostic database packs

This directory is the source tree for the offline Diagnostic Knowledge Base.

## Planned structure

```text
database/
├── schema/
├── generic/
├── manufacturers/
│   ├── tesla/
│   ├── vag/
│   ├── bmw/
│   ├── mercedes/
│   ├── hyundai_kia/
│   └── toyota/
├── translations/
└── manifest.json
```

The repository source is not the runtime Android database. CI will validate the source and later build versioned SQLite/Room-compatible packs.

## First-run Android setup

The application should present a manufacturer selection screen during first run:

- Generic / OBD-II — included
- Tesla
- Volkswagen Group / VAG
- BMW
- Mercedes-Benz
- Hyundai / Kia
- Toyota
- Other supported packs

Each pack shows its approximate download size and database version. The user can select multiple packs, install them over Wi-Fi, and change the selection later in Settings.

The generic pack is always retained because it contains common diagnostic infrastructure and shared definitions.

## Offline behavior

After installation, selected packs remain usable without Internet access. An update check is optional and only occurs when connectivity is available.

The app must never require a live server to explain a DTC that is already present in the installed local pack.

## Update behavior

Updates are pack-specific. For example, a Tesla database update must not require downloading the VAG database again.

Each pack will eventually contain:

- pack ID
- version
- schema version
- minimum app version
- checksum
- release timestamp
- source/provenance metadata

Updates are downloaded to a temporary location, verified, and activated atomically. If verification fails, the previous database remains active.
