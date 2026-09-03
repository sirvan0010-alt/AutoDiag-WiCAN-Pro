# OBD-II Mode 06 foundation

## Scope

The core now preserves and decodes the standardized CAN/J1979 Mode 06 response shape without inventing manufacturer-specific semantics.

A Mode 06 request is `06 <OBDMID>` and the positive response starts with `46 <OBDMID>`. A test record is represented as:

`TID | UASID | Test Value (2 bytes) | Minimum (2 bytes) | Maximum (2 bytes)`

The SAE J1979 material defines the Mode 06 request/response messages and points the standardized OBDMID/TID/UASID definitions to the Digital Annex. citeturn0search34turn0search8

## Safety boundary

`Mode06Decoder` preserves the raw 16-bit values and UASID. It does **not** guess units, signedness, offsets, resolution, monitor names, or manufacturer-specific meanings. Therefore the initial result status is `UNKNOWN` until an explicit UASID/TID decoder is available.

This is intentional: the same raw value can represent different physical quantities depending on the unit/scaling identifier, and identifiers at or above `0x80` can require signed interpretation. citeturn0search0

## Fabia/EGR path

The generic layer is now ready to carry a non-continuous monitor result such as an EGR test, but it does **not** label an arbitrary TID/CID as EGR and does not convert it to `%` until the vehicle/monitor definition is verified.

The next layer is therefore a verified monitor definition registry:

1. OBDMID/TID/UASID definition.
2. Physical unit, resolution, offset and signedness.
3. Test-limit interpretation.
4. Human-readable Czech label.
5. Vehicle/manufacturer provenance and verification scope.
6. Replay fixture from an actual capture before presenting a value such as `2.222 %` or `OK`.

That separation prevents a plausible-looking but incorrect Mode 06 diagnosis.
