# OBD-II Mode 06 foundation

## Scope

The core preserves and decodes the standardized CAN/J1979 Mode 06 response shape without inventing manufacturer-specific semantics.

A Mode 06 request is `06 <OBDMID>` and the positive response starts with `46 <OBDMID>`. A CAN test record is:

`MID | TID | UASID | Test Value (2 bytes) | Minimum (2 bytes) | Maximum (2 bytes)`

The UASID identifies the unit/scaling used to calculate and display the test value and both limits. A test value within the decoded minimum/maximum band is a passing result. This is why raw comparison without UASID decoding is intentionally not used for diagnosis.

## Architecture

The implementation is split into four layers:

1. `Mode06Decoder` — transport-independent raw CAN/J1979 record parsing.
2. `Mode06DiscoveryDecoder` — supported-MID bitmask discovery using 0x20-aligned windows and continuation bit 0.
3. `Mode06UasRegistry` — explicit UASID → unit/scaling/signedness conversion.
4. `Mode06MonitorRegistry` / `Mode06TestRegistry` — monitor and test identity. An exact MID+TID+UASID definition has priority over a generic MID/TID definition.

`Mode06Interpreter` joins those layers and calculates decoded value, minimum, maximum, PASS/FAIL and normalized position inside the test band only when the UASID is known. Unknown UASIDs remain raw and produce `UNKNOWN` interpretation.

## Discovery first

The application should discover supported OBDMIDs before probing the complete range. `Mode06DiscoveryPlanner` starts with `06 00` and advances to `06 20`, `06 40`, `06 60`, `06 80`, `06 A0`, `06 C0` and `06 E0` only when the current 32-bit bitmap advertises the next window. This reduces unsupported requests and keeps discovery separate from monitor semantics.

The discovery response is the five-byte payload `46 <base> <mask-hi> <mask> <mask> <mask-lo>`. Bit 31 maps to `base + 1`; bit 0 maps to `base + 0x20`. The continuation bit is therefore also the availability indication for the next discovery window.

## Current standardized scaling coverage

The registry currently includes explicit definitions for representative standard UASIDs including rotational frequency (`0x07`), vehicle speed (`0x09`), voltage (`0x0A`), time (`0x10`), ratio (`0x20`), mass per time (`0x27`), count (`0x2B`) and signed voltage (`0x8C`). UASID `0x27` is 0.01 g/s per bit; UASID `0x8C` is signed two's-complement voltage at 0.01 V/bit.

No unverified UASID is filled in merely because its raw values look plausible. This is a hard boundary for diagnostic correctness.

## Monitor and test definitions

Standardized monitor IDs currently include oxygen sensors, catalyst banks, EGR banks, VVT banks, purge flow, oxygen-sensor heaters, heated catalysts, secondary-air monitors, fuel-system monitors, boost-pressure monitors, misfire monitors and PM-filter monitors. A MID alone is not enough to identify the measurement: a monitor may expose multiple TIDs, and the TID/UASID combination determines the actual test meaning and scaling.

Manufacturer-defined monitor/test combinations remain unresolved unless a vehicle-specific definition is supplied with provenance and verification scope.

## Fabia/EGR path

The generic layer can carry and interpret a non-continuous EGR monitor when the ECU reports a known MID/TID/UASID definition. It still does **not** label an arbitrary TID as a specific EGR test and does not claim a value such as `2.222 %` until the exact TID/UASID/scaling definition and vehicle provenance are verified.

The remaining vehicle-specific layer is:

1. verified OBDMID/TID/UASID combination;
2. physical unit, resolution, offset and signedness;
3. standardized or manufacturer-specific test semantics;
4. human-readable Czech label;
5. vehicle/ECU provenance and verification scope;
6. replay fixture from an actual capture.

That separation prevents a plausible-looking but incorrect Mode 06 diagnosis.
