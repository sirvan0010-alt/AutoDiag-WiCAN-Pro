# PROGRESS

## 2026-09-09 — Car Scanner 2.1.50 / block 1
APK `Car Scanner_2.1.50.apk` was unpacked and its .NET MAUI/Xamarin AssemblyStore was parsed successfully. The XABA store contained 208 managed assemblies; `CarScannerMaui.dll` (39,970,304 bytes after XALZ/LZ4 decompression) was extracted for static analysis. Source APK SHA-256: `afab252c88104d1c04ed5c0751b6dbfce39be5f90d69e513e174d1ed5336336c`.

The first protocol inventory is complete at the family/architecture level. Direct evidence was found for ELM327, Bluetooth Classic, BLE, Wi-Fi/TCP, USB, OBD-II/EOBD, CAN 11-bit, CAN 29-bit, CAN multi-frame/flow control, KWP/ISO 14230, ISO 9141-2 profile support, SAE J1850 profile support, UDS, VW TP 2.0, Nissan Consult II/3, Subaru SSM2, and Mitsubishi MUT-2 compatibility. Manufacturer ECU classes/request builders also cover Renault/Dacia, Hyundai/Kia, Toyota, Nissan, Subaru, GM, Ford, BMW, Mercedes, Volvo, VAG, Mitsubishi and others.

The extraction report was committed to `AutoDiag-WiCAN-Diagnostic-Data` as `diagnostic-data/extraction/CAR_SCANNER_2_1_50_FORENSIC_REPORT.md` in commit `b399682b63d1950594df2b0fa198876a16cc6d1d`. No CAN ID, DID, byte offset, scaling or isolation-resistance mapping was promoted to verified; these remain unverified until reconstructed from request/decoder evidence or runtime capture.

Next autonomous step: continue static reconstruction around `OBDDataReader`, `RequestProducers`, Mitsubishi ECU/MUT-2 logic and embedded Mitsubishi resources, with special attention to isolation/HV resistance and any `21 01`/`0x2101` request path.

## 2026-09-09 — Car Scanner 2.1.50 / block 2
Managed metadata was parsed far enough to recover concrete type/method inventories rather than relying only on raw strings. `OBDDataReader` exposes `SendRequest`, `SendString`, `ReadData`, `SearchForProtocol`, `InitializeDefaultInitString`, `SendInitCommandsNissanConsult2`, `SendATZ`, `ReadDataWithManualFlowControl_CAN`, `SendLongCanRequest`, `SendLongCanRequestSTN`, `SendLongCANMultiRequest`, `ParseFlowControlFrame`, `DecodeCAN11bit`, `DecodeCAN29bit`, `DecodeMultiResponseCanData`, `DecodeKWP`, `DecodeDTC_CAN11bit/29bit`, `DecodeDTC_KWP`, and `DecodeToCANFrames`.

The actual data contracts are now also evidenced: `ELMFormat` has `Unknown`, `KWP`, `CAN11bit`, `CAN29bit`, `VwTp20`; `ConnectionTypes` has `WiFi`, `BluetoothLE`, `Bluetooth`, `USB`, `MFI_OBDLinkMXPlus`; `KWPBaudRates` contains 10400/4800/9600; `CANFrame` contains Header, Data, ExpectedLength, ExtendedAddress, HasExtendedAddress, Type, CANFormat and RawHexData; `OBDRequest` contains Command, ResponseMarker, Header, Before/AfterCommands, PIDs, Payload, ELMFormat, OBDMode, progress/retry state and explicit manual-flow-control flags.

`MitsubishiCANECU` is a concrete ECU model type with a `BuildList` method and ECU collection, while the generic protocol machinery remains in `OBDDataReader`. This means Mitsubishi support is not merely a database label, but the current static evidence still does not expose the actual Mitsubishi request bytes or prove the isolation `21 01` operation. The second-block metadata evidence will be committed as a separate extraction artifact and the main progress file will be appended after that save.

## 2026-09-09 — Car Scanner 2.1.50 / block 3
A focused static search for the suspected isolation command `21 01` was completed. The DLL contains 94 ASCII `2101` occurrences, but contextual inspection identifies them as DTC/diagnostic text such as P2101, B2101 and U2101; no delimited `2101` command string or `21 01` textual request was found. One raw `0x21 0x01` byte pair exists inside high-entropy binary data and is not structurally attributable to a diagnostic request, so it remains unverified.

Isolation-related terminology is present, including hybrid-battery isolation-fault DTCs and `Insulation control; Insulation monitoring`, but no dedicated live isolation-resistance/Riso decoder was recovered. The APK does contain substantial Mode 22 request infrastructure (`GetPID22F...` and long `220...`/`221...` request lists), so extended manufacturer requests are definitely supported, but this block found no Mitsubishi isolation `21 01` mapping.

The focused result was saved as `diagnostic-data/extraction/CAR_SCANNER_2_1_50_ISOLATION_21_01_SEARCH.md` in commit `a6bde033619c50f90de8243f28a59b05c68d49a6`. Current AutoDiag status for Car Scanner 2.1.50 and the specific Outlander `21 01` hypothesis is therefore `UNKNOWN/CANDIDATE`, not VERIFIED. Next autonomous step is embedded-resource/request-list reconstruction, especially Mitsubishi-specific ECU collections and any hidden manufacturer command tables.
