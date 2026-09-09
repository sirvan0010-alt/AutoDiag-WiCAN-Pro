# PROGRESS

## 2026-09-09 — Car Scanner 2.1.50 / block 1
APK `Car Scanner_2.1.50.apk` was unpacked and its .NET MAUI/Xamarin AssemblyStore was parsed successfully. The XABA store contained 208 managed assemblies; `CarScannerMaui.dll` (39,970,304 bytes after XALZ/LZ4 decompression) was extracted for static analysis. Source APK SHA-256: `afab252c88104d1c04ed5c0751b6dbfce39be5f90d69e513e174d1ed5336336c`.

The first protocol inventory is complete at the family/architecture level. Direct evidence was found for ELM327, Bluetooth Classic, BLE, Wi-Fi/TCP, USB, OBD-II/EOBD, CAN 11-bit, CAN 29-bit, CAN multi-frame/flow control, KWP/ISO 14230, ISO 9141-2 profile support, SAE J1850 profile support, UDS, VW TP 2.0, Nissan Consult II/3, Subaru SSM2, and Mitsubishi MUT-2 compatibility. Manufacturer ECU classes/request builders also cover Renault/Dacia, Hyundai/Kia, Toyota, Nissan, Subaru, GM, Ford, BMW, Mercedes, Volvo, VAG, Mitsubishi and others.

The extraction report was committed to `AutoDiag-WiCAN-Diagnostic-Data` as `diagnostic-data/extraction/CAR_SCANNER_2_1_50_FORENSIC_REPORT.md` in commit `b399682b63d1950594df2b0fa198876a16cc6d1d`. No CAN ID, DID, byte offset, scaling or isolation-resistance mapping was promoted to verified; these remain unverified until reconstructed from request/decoder evidence or runtime capture.

Next autonomous step: continue static reconstruction around `OBDDataReader`, `RequestProducers`, Mitsubishi ECU/MUT-2 logic and embedded Mitsubishi resources, with special attention to isolation/HV resistance and any `21 01`/`0x2101` request path.
