# AutoDiag-WiCAN Diagnostic Data

This directory defines the contract for the separate diagnostic data repository.

## Proposed repository

`AutoDiag-WiCAN-Diagnostic-Data`

The data repository should contain only data that we are legally allowed to redistribute, plus metadata derived from proprietary reference material without copying proprietary payloads.

## Initial upload set

Priority order:

1. Public SAE/J1979 OBD Mode 01/02/03/06/09 definitions and tests.
2. Public UDS service/DID definitions and standards-derived mappings.
3. Our own vehicle/ECU/signal schema.
4. Safe ROD metadata index: filename, SHA-256, section tags, platform/family hints and provenance; **not** raw `.rod` files.
5. VCDS AutoScan-derived chassis/address inventory only where redistribution is permitted; otherwise keep the source local and export only our own normalized metadata.
6. Verified vehicle captures/replays produced by AutoDiag-WiCAN.

The application must treat this repository as optional knowledge. Live vehicle evidence always has higher trust than database metadata.
