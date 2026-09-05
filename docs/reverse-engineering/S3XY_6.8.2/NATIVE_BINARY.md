# Native binary findings

Primary module: `lib/arm64-v8a/libS3XYButtons_arm64-v8a.so`

## Binary characteristics
- ELF 64-bit AArch64 shared object
- Android 28 native target
- NDK r27c (`12479018`)
- Build ID: `1148f4bc53db364e6f769b56920effed02c30a3b`
- DWARF debug information: present
- Dynamic/static symbol information: extensive; library is not stripped
- Size: 299,688,392 bytes

## Major classes / modules observed
- `CBLETransport`
- `CSession`
- `CBLECommander`
- `CTeslaAPIBLEModule`
- `CCarModule`
- `CButtonsModule`
- `CDashboardModule`
- `CStalksModule`
- `CKnobModule`
- `CStripModule`
- `CFirmwareModule`
- `EnhApiPayloadHolder`
- `PushVehicleDataHolder`
- `PushVehicleEventsDataHolder`
- `CEnhSecurity0`
- `CEnhSecurity1`

## Interpretation for AutoDiag

The native library is the primary reverse-engineering target. Java/Kotlin DEX contains the Android shell and support stack, while the substantial vehicle/device logic is native Qt/C++.

The presence of symbols and debug/source metadata is strong evidence for architectural structure, but it is **not** proof of field units, scaling, vehicle coverage, or safety. Those remain evidence-gated under the AutoDiag protocol.
