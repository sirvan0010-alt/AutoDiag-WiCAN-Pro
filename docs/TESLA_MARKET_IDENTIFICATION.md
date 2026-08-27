# Tesla Market / Region Identification

## Purpose

AutoDiag should identify the market/region configuration of a supported Tesla when this can be established reliably from vehicle-reported data, VIN decoding, diagnostic metadata, or another documented source.

This is an informational vehicle-profile feature. It must never guess the market from language, IP address, GPS location, or the user's phone region.

## UI

When the market is reliably identified:

```text
TESLA MODEL Y
2022 · Long Range
Market: United States 🇺🇸
⚠️ US-market vehicle
```

The warning icon is intentionally visible because market configuration can affect equipment, regulations, connectivity, charging configuration, software features, diagnostic behavior and applicability of procedures.

When the market cannot be established:

```text
Market: Unknown
ⓘ Market could not be verified from available vehicle data.
```

Do not display the warning merely because the market is unknown.

## Evidence hierarchy

Use the strongest available evidence:

1. Explicit vehicle-reported market/region field with documented meaning
2. VIN-derived market information using a documented decoder/source
3. Verified vehicle-specific diagnostic metadata
4. Other documented evidence

Do not infer US-market status from:

- IP address
- GPS position
- phone locale
- language
- charging location
- registration plate appearance
- an unverified CAN signal

## Warning semantics

`⚠️ US-market vehicle` means only that AutoDiag has established that the vehicle is configured/identified as a US-market vehicle.

It does **not** mean:

- the vehicle is unsafe
- the vehicle cannot be operated in Europe
- every US-only feature is active
- the vehicle violates local law
- a particular retrofit or modification is required

Any regulatory or compatibility implication must have its own sourced knowledge entry.

## Why market identification matters

The same Tesla model/year can have differences in:

- homologation and regulatory configuration
- charging equipment/configuration
- connectivity hardware or regional services
- software feature availability
- diagnostic behavior
- safety equipment
- service procedures and parts

AutoDiag therefore attaches market information to the vehicle profile and uses it when selecting diagnostic knowledge and procedures.

## Verification model

```text
US_MARKET_CONFIRMED
US_MARKET_PROBABLE
MARKET_KNOWN_NON_US
MARKET_UNKNOWN
```

Only `US_MARKET_CONFIRMED` should display the prominent `⚠️ US-market vehicle` indicator.

`US_MARKET_PROBABLE` may be shown as an informational result, but must clearly state that it is not confirmed.

## Implementation requirement

The detector must return both the result and provenance:

```json
{
  "market": "US",
  "status": "confirmed",
  "source_type": "vin_decoder | vehicle_reported | diagnostic_metadata",
  "source_reference": "...",
  "confidence": "high"
}
```

If no reliable source exists, return `market: unknown` rather than guessing.

## Future use

Market information becomes an input to vehicle-profile matching:

```text
Make → Model → Year → Trim → Drive unit → Battery variant → Market → Firmware/diagnostic generation
```

This prevents an OEM procedure or diagnostic interpretation intended for one regional configuration from being silently presented as universally applicable.
