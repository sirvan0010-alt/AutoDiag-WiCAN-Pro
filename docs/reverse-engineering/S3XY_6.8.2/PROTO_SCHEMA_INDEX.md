# S3XY 6.8.2 embedded protobuf schema index

The native binary contains serialized protobuf `FileDescriptorProto` data. Descriptor roots were located and parsed for 11 protocol files. This index records message names and field counts; it does not copy the original `.proto` source.

## Descriptor roots recovered

| proto | native offset |
|---|---:|
| `actions.proto` | `0x5d4f20` |
| `buttons.proto` | `0x5d7d94` |
| `dash.proto` | `0x5dcd2c` |
| `dashboard.proto` | `0x5de628` |
| `enhapi.proto` | `0x5e1658` |
| `enhapi_vehicle_data.proto` | `0x5e95c8` |
| `enhapi_vehicle_events_data.proto` | `0x5ed208` |
| `commander.proto` | `0x5d9688` |
| `ota.proto` | `0x5f2808` |
| `stalks.proto` | `0x5f3f00` |
| `tesla_api.proto` | `0x5f6bdc` |

## Exact message inventory

### `enhapi.proto` — 1
- `EnhApiPayloadHolder` — 157 fields

### `enhapi_vehicle_data.proto` — 3
- `ReqSubscribeVehicleData` — 4
- `RespSubscribeVehicleData` — 1
- `PushVehicleDataHolder` — 127

### `enhapi_vehicle_events_data.proto` — 3
- `ReqSubscribeVehicleEventsData` — 4
- `RespSubscribeVehicleEventsData` — 1
- `PushVehicleEventsDataHolder` — 10

### `tesla_api.proto` — 16
- `NVS` — 3
- `ReqGetApiStatus` — 0
- `SessionStatusData` — 3
- `RespGetApiStatus` — 6
- `CmdConnectToCar` — 1
- `CmdSendPublicKeyToCar` — 0
- `CmdStartSession` — 1
- `PushApiStatus` — 2
- `PushSessionStatus` — 3
- `PushUIStatus` — 1
- `ReqConnectToCar` — 1
- `RespConnectToCar` — 3
- `ReqSendPublicKeyToCar` — 1
- `RespSendPublicKeyToCar` — 1
- `ReqStartSession` — 1
- `RespStartSession` — 3

### `dashboard.proto` — 10
- `CmdDashTriggerAllDataPush` — 0
- `ReqDashQuickDataFactors` — 0
- `RespDashQuickDataFactors` — 13
- `PushDashQuickData` — 13
- `ReqDashMediumDataFactors` — 0
- `RespDashMediumDataFactors` — 37
- `PushDashMediumData` — 40
- `ReqDashSlowDataFactors` — 0
- `RespDashSlowDataFactors` — 57
- `PushDashSlowData` — 66

### `ota.proto` — 6
- `ReqOTA` — 5
- `RespOTA` — 3
- `CmdOTASendData` — 2
- `PushOtaStatus` — 2
- `ReqOTAEncryptionV2` — 1
- `RespOTAEncryptionV2` — 1

### `stalks.proto` — 18
- `StalkInfo` — 8
- `StalkBaseInfo` — 7
- `StalkStat` — 2
- `StalkBasicInfo` — 2
- `StalkDetailedInfo` — 2
- `PushStalkBasicInfo` — 6
- `ReqStartStalkScan` — 2
- `RespStartStalkScan` — 1
- `ReqStopStalkScan` — 0
- `RespStopStalkScan` — 1
- `ReqGetStalksBasicInfo` — 0
- `RespGetStalksBasicInfo` — 1
- `ReqGetStalksDetailInfo` — 1
- `RespGetStalksDetailInfo` — 1
- `ReqRemoveAllStalks` — 0
- `RespRemoveAllStalks` — 1
- `ReqStartStalkOtaUpdate` — 1
- `RespStartStalkOtaUpdate` — 1

### `actions.proto` — 30
- `Segments` — 5
- `EffectsState` — 3
- `AmbientLightEffect` — 5
- `AmbientLightEffectsAutomationSettings` — 10
- `KickdownAutomationSettings` — 5
- `PassengerEasyEntryAutomationSettings` — 2
- `VolumeOnExitAutomationSettings` — 1
- `ScrollWheelButtonSettings` — 3
- `DiagnosticSettings` — 4
- `AutoWindowDropSettings` — 3
- `ActionsUponApproachSettings` — 2
- `LightsAutomationSettings` — 2
- `EmergencyLaneKeepOffSettings` — 2
- `SuspensionByGearSettings` — 2
- `CameraOnFoldMirrorsSettings` — 2
- `GeneralAmbientLightSettings` — 2
- `RemoteBatteryPreheatSettings` — 2
- `ActionsOnReverseSettings` — 2
- `Automation` — 4
- `PressActionConfig` — 4
- `PressActionGroup` — 3
- `ReqSetMacroAction` — 1
- `RespSetMacroAction` — 1
- `ReqCreateActionGroup` — 1
- `RespCreateActionGroup` — 1
- `ReqRemoveActionGroup` — 1
- `RespRemoveActionGroup` — 1
- `ReqUpdateActionGroup` — 1
- `RespUpdateActionGroup` — 1
- `PushAutomationUpdate` — 1

### `buttons.proto` — 37
- `ButtonBasicInfo` — 5
- `DetailInfo` — 15
- `PushButtonPressState` — 2
- `PushButtonLedOnState` — 2
- `PushButtonBasicInfo` — 1
- `CmdExecuteButtonOneClick` — 1
- `ReqStartButtonScan` — 1
- `RespStartButtonScan` — 1
- `ReqStopButtonScan` — 0
- `RespStopButtonScan` — 1
- `ReqStartKnobScan` — 1
- `RespStartKnobScan` — 1
- `ReqStopKnobScan` — 0
- `RespStopKnobScan` — 1
- `ReqGetButtonBasicInfoList` — 0
- `RespGetButtonBasicInfoList` — 1
- `ReqGetButtonDetailInfo` — 1
- `RespGetButtonDetailInfo` — 1
- `ReqRemoveAllButtons` — 0
- `RespRemoveAllButtons` — 1
- `ReqRemoveDevice` — 1
- `RespRemoveDevice` — 1
- `ReqSetAction` — 1
- `RespSetAction` — 1
- `ReqTryAction` — 1
- `RespTryAction` — 1
- `ReqExecuteButtonOneClick` — 1
- `RespExecuteButtonOneClick` — 1
- `ReqSetButtonPostfix` — 1
- `RespSetButtonPostfix` — 1
- `ButtonLabel` — 2
- `ReqGetButtonLabelsList` — 0
- `RespGetButtonLabelsList` — 1
- `ReqSetButtonLabel` — 2
- `RespSetButtonLabel` — 1
- `ReqGetButtonLabel` — 1
- `RespGetButtonLabel` — 1

### `commander.proto` — 67
- `KnobBasicInfo` — 5
- `ReqGetCommanderInitInfo` — 0
- `RespGetCommanderInitInfo` — 19
- `SmartActionInfo` — 2
- `ReqGetSmartActionsList` — 0
- `RespGetSmartActionsList` — 2
- `ReqSetSmartActionState` — 1
- `RespSetSmartActionState` — 1
- `ReqSetAutomationSettings` — 2
- `RespSetAutomationSettings` — 2
- `ReqGetAutomationSettings` — 0
- `RespGetAutomationSettings` — 1
- `ReqGetCommanderExtraInfo` — 0
- `RespGetCommanderExtraInfo` — 1
- `ReqSendUUID` — 1
- `RespSendUUID` — 1
- `ReqDashStartStop` — 1
- `RespDashStartStop` — 1
- `ReqRemoveAllButThisUUID` — 1
- `RespRemoveAllButThisUUID` — 1
- `PushSmartActionUpdate` — 1
- `ReqSetWiFiSsidAndPass` — 2
- `RespSetWiFiSsidAndPass` — 1
- `ReqGetWiFiSsidAndPass` — 0
- `RespGetWiFiSsidAndPass` — 2
- `ReqDashTriggerAllDataPush` — 0
- `RespDashTriggerAllDataPush` — 1
- `ReqGetCarConfig` — 0
- `RespGetCarConfig` — 1
- `ReqDefault` — 0
- `RespDefault` — 1
- `ReqIsFeautureSupported` — 1
- `RespIsFeautureSupported` — 1
- `PushDeviceConnectionState` — 2
- `PushAnotherCommanderDetected` — 1
- `ReqSetMultyCommandersPerm` — 1
- `RespSetMultyCommandersPerm` — 1
- `ReqGetMultyCommandersInfo` — 0
- `RespGetMultyCommandersInfo` — 1
- `CurrentActionValueInfo` — 4
- `ReqGetActionValueInfo` — 1
- `RespGetActionValueInfo` — 1
- `PushActionValueInfo` — 1
- `ReqRemoveDevicesByType` — 1
- `RespRemoveDevicesByType` — 1
- `ReqSimple` — 0
- `RespSimple` — 1
- `ReqSubscribeByDeviceType` — 1
- `RespSubscribeByDeviceType` — 1
- `ReqGetCarSettings` — 0
- `RespGetCarSettings` — 1
- `ReqGetAmbientLightEffect` — 1
- `RespGetAmbientLightEffect` — 1
- `ReqSetAmbientLightEffect` — 1
- `RespSetAmbientLightEffect` — 1
- `ReqIsPhoneConnected` — 0
- `RespIsPhoneConnected` — 1
- `ReqStartCommanderAdvForDevice` — 1
- `RespStartCommanderAdvForDevice` — 1
- `ReqSetDashBasicInfo` — 1
- `RespSetDashBasicInfo` — 1
- `ReqGetDashBasicInfo` — 0
- `RespGetDashBasicInfo` — 1
- `ReqSetCarSettingsAndConfigs` — 1
- `RespSetCarSettingsAndConfigs` — 1
- `ReqGetCarSettingsAndConfigs` — 0
- `RespGetCarSettingsAndConfigs` — 1

### `dash.proto` — 25
- `ErrorInfo` — 3
- `DashSettings` — 17
- `ReqGetDashInitInfo` — 0
- `RespGetDashInitInfo` — 5
- `ReqStartDashScanForDevice` — 1
- `RespStartDashScanForDevice` — 2
- `ReqSetDashSettings` — 1
- `RespSetDashSettings` — 2
- `ReqGetDashSettings` — 0
- `RespGetDashSettings` — 3
- `PushDashSettingsUpdated` — 1
- `ReqAbort` — 0
- `RespAbort` — 1
- `DashFirmwareUpdateInfo` — 6
- `ReqStartDashUpdate` — 1
- `RespStartDashUpdate` — 1
- `WiFiNetwork` — 4
- `ReqDashWiFiConnection` — 1
- `RespDashWiFiConnection` — 1
- `PushDashWiFiConnectionStatus` — 1
- `PushDashAvailableWiFiList` — 1
- `ReqConnectToWiFiNetwork` — 1
- `RespConnectToWiFiNetwork` — 1
- `PushDashStatus` — 1
- `PushDashErrorEvent` — 1

## Total

Across the 11 recovered descriptor roots, **216 message definitions** were identified.

## AutoDiag status

**Wire schema recovery: verified for the recovered descriptor data.** Physical meaning, ECU origin, units, scaling, vehicle coverage, firmware compatibility and transport framing remain separate evidence questions.
