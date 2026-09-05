# S3XY 6.8.2 capability import policy

The supplied S3XY 6.8.2 application is treated as a high-value behavioural reference. Its observed capability surface can accelerate AutoDiag implementation, but application presence is not equivalent to vehicle verification.

## Import classes

### Safe to import as application evidence

- domain names and capability taxonomy
- read-only vehicle-data field vocabulary
- observed subscription/request/response object names
- BLE/GATT architectural separation
- device identity/UUID concepts
- action taxonomy as a reference catalog
- UI-independent capability names

### Import as candidate only

- exact payload layouts reconstructed from native code
- protobuf field numbers/types reconstructed from descriptors
- BLE UUID-to-characteristic mappings reconstructed from the binary
- formulas reconstructed from code paths
- exact request bytes reconstructed from native serialization

### Require independent validation before VERIFIED

- VIN/ECU-specific mappings
- vehicle CAN identifiers
- raw CAN request/response pairs
- security/authentication procedures
- write/control operations
- safety-critical diagnostics
- production diagnostic thresholds

## Design consequence

AutoDiag should not recreate functionality that is already clearly represented by the reference application's architecture merely for the sake of being different. Instead, implement a clean, testable equivalent in the existing AutoDiag layers and attach the extraction provenance to the capability.

The preferred chain is:

`APPLICATION_OBSERVED -> APPLICATION_DERIVED -> CANDIDATE -> TEST -> VEHICLE_CAPTURE -> VERIFIED -> PRODUCTION`

A missing vehicle capture remains a blocker rather than an excuse to invent a value.
