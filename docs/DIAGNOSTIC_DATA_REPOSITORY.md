# Diagnostic Data Repository Architecture

AutoDiag-WiCAN-Pro keeps transport, decoding, evidence and UI logic separate from the growing diagnostic knowledge base.

The future `AutoDiag-WiCAN-Diagnostic-Data` repository is an external data source. The app must work without it and cache validated data locally.

## Trust model

Remote database entries are reference knowledge, not proof that a function works on the connected vehicle.

- `UNVERIFIED`: source exists but has not been validated.
- `PARTIALLY_VERIFIED`: schema/profile evidence exists.
- `VERIFIED`: the vehicle/ECU response has been observed and decoded successfully.

A database hit may raise a capability to `PARTIAL`; it must never by itself make a capability `AVAILABLE` or `VERIFIED`.

## Data domains

- OBD Mode 01/02/03/06/09
- UDS services and DIDs
- ECU identification
- vehicle/ECU topology
- live-data signal definitions and scaling
- DTC metadata
- measuring blocks / observed measurement schemas
- service and adjustment metadata
- provenance and verification records

Proprietary raw ROD/Haynes/VCDS databases are not stored in the public repository. Safe metadata extracted from them may be represented with explicit provenance such as `SCHEMA_ONLY`.

## Runtime resolution

`vehicle/VIN -> ECU identity -> data provider -> cached/remote definition -> decoder -> DiagnosticEvidence`

The diagnostic engine remains functional when the provider is unavailable. Unknown data remains unknown instead of being guessed.
