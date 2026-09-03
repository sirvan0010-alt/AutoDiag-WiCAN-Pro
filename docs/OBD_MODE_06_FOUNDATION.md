# OBD-II Mode 06 foundation

## Scope

The core preserves and decodes the standardized CAN/J1979 Mode 06 response shape without inventing manufacturer-specific semantics.

A Mode 06 request is `06 <OBDMID>` and the positive response starts with `46 <OBDMID>`. A CAN test record is represented as:

`TID | UASID | Test Value (2 bytes) | Minimum (2 bytes) | Maximum (2 bytes)`

SAE J1979 defines the Mode 06 request/response messages and delegates standardized OBDMID/TID/UASID definitions to its Digital Annex. The UASID tells external test equipment how to calculate and display the result and both limits. The test value is within the decoded limits for a pass. citeturn0search29turn5search2

## Architecture

The implementation is deliberately split into three layers:

1. `Mode06Decoder` — transport-independent raw CAN/J1979 record parsing.
2. `Mode06UasRegistry` — explicit UASID → unit/scaling/signedness conversion.
3. `Mode06MonitorRegistry` — standardized OBDMID → monitor identity and Czech label.

`Mode06Interpreter` joins those layers and calculates decoded value, minimum, maximum, PASS/FAIL and normalized position inside the test band only when the UASID is known. The same raw 16-bit value must not be interpreted without its UASID because scaling and signedness are part of the protocol definition. citeturn0search29turn2search0

## Current standardized scaling coverage

The registry currently includes explicitly verified definitions for representative standard UASIDs including rotational frequency (`0x07`), vehicle speed (`0x09`), voltage (`0x0A`), time (`0x10`), ratio (`0x20`), count (`0x2B`) and signed voltage (`0x8C`). The signed voltage definition is two's-complement with 0.01 V/bit. citeturn2search13

Unknown UASIDs remain raw and produce `UNKNOWN` interpretation. The registry is intentionally extensible rather than filling unverified identifiers with guesses.

## Monitor definitions

Standardized monitor IDs currently include oxygen sensors, catalyst banks, EGR banks, VVT banks, purge flow, oxygen-sensor heaters, heated catalysts, secondary-air monitors, fuel-system monitors, boost-pressure monitors, misfire monitors and PM-filter monitors. Manufacturer-defined monitor IDs remain unresolved unless a vehicle-specific definition is supplied. Standardized monitor ranges are independently represented in open diagnostic implementations. citeturn2search0turn1search1

## Fabia/EGR path

The generic layer can now carry and interpret a non-continuous EGR monitor when the ECU reports a known UASID. It still does **not** label an arbitrary TID as a specific EGR test and does not claim a value such as `2.222 %` until the exact TID/UASID/scaling definition and vehicle provenance are verified.

The remaining vehicle-specific layer is:

1. verified OBDMID/TID/UASID combination;
2. physical unit, resolution, offset and signedness;
3. standardized or manufacturer-specific test semantics;
4. human-readable Czech label;
5. vehicle/ECU provenance and verification scope;
6. replay fixture from an actual capture.

That separation prevents a plausible-looking but incorrect Mode 06 diagnosis.
