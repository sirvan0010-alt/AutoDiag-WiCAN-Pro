# DTC Database Architecture

## Goal

AutoDiag uses an offline-first diagnostic knowledge base. The Android application ships with a small core database and lets the user select manufacturer packs during installation or later from **Settings → Databases**.

A DTC is never interpreted from the numeric code alone. Matching uses, where available:

- manufacturer
- model family
- model year / production range
- platform
- ECU / subsystem
- protocol (OBD-II / UDS / manufacturer-specific alert)
- code format and raw response

This prevents a generic or manufacturer-specific code from being incorrectly mapped to another vehicle generation.

## Data states

Every displayed value and interpretation has an explicit state:

- `REPORTED` — directly supplied by the vehicle
- `ESTIMATED` — calculated by AutoDiag
- `VEHICLE_DID_NOT_PROVIDE` — the ECU/vehicle did not provide the requested datum
- `UNVERIFIED` — a decoder or interpretation exists but has not been sufficiently validated
- `VERIFIED` — validated for the declared vehicle/HW/FW scope

The UI must use the Czech phrase **„Vozidlo údaj neposkytlo“** for `VEHICLE_DID_NOT_PROVIDE`; never display `N/A` or `NOT_AVAILABLE` to the user.

## Package layout

```text
diagnostics/
  manifest.json
  core/
    generic_obd.json
  manufacturers/
    tesla/
      manifest.json
      dtc.json
      alerts.json
    vag/
      manifest.json
      dtc.json
      alerts.json
  sources/
    source-index.json
```

Manufacturer packs are independent and versioned. The app can install only the packs selected by the user.

## Record example

```json
{
  "id": "tesla:model3:2021-2024:bms:EXAMPLE",
  "code": "EXAMPLE",
  "kind": "manufacturer_dtc",
  "manufacturer": "Tesla",
  "model_scope": ["Model 3"],
  "year_from": 2021,
  "year_to": 2024,
  "ecu": "BMS",
  "description": "Verified description goes here.",
  "severity": "UNKNOWN",
  "verification": "unverified",
  "sources": []
}
```

The repository must not fill records with guessed Tesla DIDs, CAN IDs, thresholds or repair instructions. Those are added only when the source and vehicle scope are documented.

## Offline-first behavior

1. Application starts with the core generic pack.
2. User selects manufacturers and optional model families.
3. Selected packs are downloaded and stored locally.
4. Diagnostics continue to work without Internet access.
5. When online, AutoDiag checks the signed/versioned manifest for updates.
6. Only changed packs are downloaded.
7. A failed update leaves the previous known-good pack installed.

## Database size

Do not bundle one enormous universal document into the APK. Use compressed, versioned manufacturer packs. The UI should show the estimated download/storage size before installation.

Example selection screen:

```text
Diagnostické databáze

☑ Tesla                 18 MB
☑ Škoda / VAG MEB        9 MB
☐ Volkswagen             7 MB
☐ Audi                    8 MB
☐ Generic OBD-II         2 MB

Celkem ke stažení:       29 MB
Úložiště po instalaci:   ~54 MB

[ Stáhnout vybrané databáze ]
```

## Update policy

GitHub Actions may automatically build and validate database packages, but production data is not blindly scraped from the Internet. An update must pass schema validation and provenance checks. OEM/community sources remain explicitly separated.

For Tesla and other manufacturers, the updater can monitor declared source feeds/repositories and create a proposed database update. A maintainer or validation process must approve new diagnostic interpretations before they become `verified` production knowledge.

## Tooltip requirements

Every diagnostic field that is not self-explanatory gets a `?` tooltip. Tooltips are localized and should explain:

- what was measured/read
- where it came from
- whether it was reported or calculated
- what the result means
- important limitations
- what **„Vozidlo údaj neposkytlo“** means

Example:

> **SOH baterie ?**
> Stav zdraví trakční baterie. Pokud je hodnota přímo načtená z BMS, je označena jako „Údaj z vozidla“. Pokud daná řídicí jednotka SOH neposkytuje, aplikace zobrazí „Vozidlo údaj neposkytlo“ a nebude hodnotu dopočítávat bez jasně označené metodiky.

## Security and integrity

Each installed pack records:

- package version
- schema version
- SHA-256 checksum
- source revision
- creation/update timestamp
- verification state

The application refuses malformed packs and does not silently replace a valid installed pack with an invalid update.
