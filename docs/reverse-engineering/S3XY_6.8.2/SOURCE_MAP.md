# Native debug/source map extracted from DWARF

The native library contains DWARF debug information. The paths below are build-time source paths embedded in the binary; they are evidence of source structure, not a copy of the source code.

- total unique source/debug paths: 676
- generated QML cache translation units: 239

## Highest-value areas for AutoDiag reverse engineering

### Transport / protocol
- `S3XYButtons/Shared/Provisioning/Transport/BLETransport.cpp`
- `S3XYButtons/Shared/Provisioning/Transport/Transport.cpp`
- `S3XYButtons/Shared/Provisioning/Utils/MessageDataParsers.cpp`
- `S3XYButtons/Shared/Provisioning/Utils/MessageHelper.cpp`
- `S3XYButtons/Shared/Provisioning/Security/EnhSecurity0.cpp`
- `S3XYButtons/Shared/Provisioning/Security/EnhSecurity1.cpp`

### Tesla / vehicle
- `S3XYButtons/Modules/CarSubModules/TeslaAPIBLEModule.cpp`
- `S3XYButtons/Modules/DataHolders/CarSubData/CarInfoData.cpp`
- `S3XYButtons/Modules/DataHolders/CarSubData/VehicleModel.cpp`
- `S3XYButtons/Modules/DataHolders/TeslaBLEData/TeslaBLEData.cpp`

### Notifications / protocol messages
The DWARF path set contains a large `Shared/Notifications` tree including Tesla API, Tesla BLE, vehicle-data, dashboard, button, stalk, knob, strip and firmware request/response/push classes.

### UI / QML
239 generated QML cache translation units are present. Major groups include `CarSubModules/Buttons`, `Commander`, `Dash`, `Dashboard`, `Knob`, `Stalks`, `Strip`, `TeslaBLE`, `Components/TeslaAPI`, `DashboardSEXY`, `DashboardHUD`, and `DashboardMini`.

## Full extraction note

The local extraction runtime retains the complete 676-path DWARF index for subsequent passes. GitHub receives the evidence-focused map here; proprietary source text is not copied into the AutoDiag repository.
