# ROD reference analysis — 2026-09-03

## Conclusion

The uploaded `.rod` files are useful, but primarily as **diagnostic database/schema evidence**, not as directly decodable signal definitions.

A binary inspection shows a consistent tagged container structure such as `[CMP]`, `[ADP]`, `[INC]`, `[DTC]`, `[GES]`, `[MWB]`, `[SLV]`, `[XPL]`, `[FFMUX]`, `[SOT]` and `[SRI]`. The payloads have high byte entropy and are mostly non-printable, so this batch does not expose human-readable request/response bytes, formulas, scaling or ECU addresses directly.

That makes the files valuable for determining **which diagnostic feature classes exist for a module**, while they are not yet sufficient to safely populate proprietary command definitions.

## Uploaded set

| File family | Feature sections observed | What it can tell us |
|---|---|---|
| `EV_DigitMatriLightAU516*` | CMP, ADP, INC, DTC, MWB, SLV, XPL; one variant also GES | Matrix-light module has fault, measured-value and adjustment/setting-related database sections. |
| `EV_DigitMatriLightAU592*` | CMP, ADP, INC, DTC, XPL | Matrix-light variants differ in available database sections; useful for profile/variant discrimination. |
| `EV_DigitMatriLightVW336*` | CMP, ADP, INC, MWB, XPL; one variant adds GES | Variant-specific measured values and setting/diagnostic content exist. |
| `EV_DigitRoofAntenAU651_001_AU58` | CMP, ADP, INC, DTC, GES, MWB, SLV, XPL | Roof antenna module includes DTC, measured values and adjustment/setting-related content. |
| `EV_DeckLidCONTIVN46T` | CMP, INC, DTC, FFMUX, GES, SLV, SOT, XPL | Deck-lid controller exposes faults, measured/setting/test-style sections and a function-multiplex section. |
| `EV_DeckLidContrUnit` | CMP, ADP, DTC, FFMUX, GES, MWB, SLV, SOT, XPL | Same functional area with additional adaptation and measured-value content. |
| `EV_DCURearPasseMAXCONT_VW37/VW48` | CMP, ADP, INC, GES, SLV, SOT | Rear passenger door-control variants; useful for ECU/module variant matching. |
| `EV_DashBoardVISMQB37W_005_SK38P/VN3SP` | CMP, INC, FFMUX, GES, SLV, SRI | Instrument-cluster variants; variant suffixes should be treated as separate evidence until matched. |
| `EV_BrakeBoostG2BoschVW48X_011_SK48/VW37` | CMP, INC, SLV | Brake-booster variants; useful as brake-system topology/profile evidence even though the actual proprietary definitions remain opaque. |

## Important engineering use

The most valuable immediate use is **feature inventory + vehicle/ECU variant matching**.

The project can model the presence of sections without pretending to know their proprietary payload semantics. For example:

- `DTC` → DTC capability exists in the reference record.
- `MWB` → measured-value / measuring-block content exists.
- `GES` → a settings/adjustment-related capability is present in the record.
- `SLV` → a service/setting-related database section exists.
- `SOT`, `SRI`, `FFMUX`, `XPL`, `ADP`, `INC`, `CMP` → preserve as opaque database-section identifiers until their exact semantics are independently verified.

These observations should feed the same fail-closed capability model used by AutoDiag: **presence of a database section must never be treated as proof that a specific command is safe or supported by the connected vehicle.**

## What the files do NOT currently prove

This batch alone does not safely provide:

1. exact CAN/ISO-TP/UDS request IDs;
2. exact response IDs or addressing mode;
3. DID numbers;
4. byte/bit offsets and endianness for individual signals;
5. scaling/offset formulas;
6. engineering units for individual measurements;
7. security-access sequences;
8. coding/adaptation write payloads;
9. actuator-control payloads;
10. a complete mapping between a file name and a particular vehicle VIN/build.

Those items must remain `UNKNOWN`/`UNVERIFIED` until extracted from an authoritative readable source or confirmed through controlled capture/replay and real-vehicle validation.

## Recommended next step

Build a small **opaque ROD metadata importer** that records:

- file name and SHA-256;
- section identifiers;
- section byte lengths;
- module-family hints from the filename;
- variant tokens (`AU516`, `AU592`, `VW336`, `VW37`, `VW48`, etc.);
- available diagnostic feature classes.

Do not attempt to decrypt, reverse-engineer or guess the binary payload format merely from these samples. The metadata importer can already improve ECU topology and capability discovery while keeping all proprietary signal definitions fail-closed.
