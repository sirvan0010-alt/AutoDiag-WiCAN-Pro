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

## Message inventory

### `enhapi.proto` — 1 message
- `EnhApiPayloadHolder` — 157 fields

### `enhapi_vehicle_data.proto` — 3 messages
- `ReqSubscribeVehicleData` — 4 fields
- `RespSubscribeVehicleData` — 1 field
- `PushVehicleDataHolder` — 127 fields

### `enhapi_vehicle_events_data.proto` — 3 messages
- `ReqSubscribeVehicleEventsData` — 4 fields
- `RespSubscribeVehicleEventsData` — 1 field
- `PushVehicleEventsDataHolder` — 10 fields

### `tesla_api.proto` — 16 messages
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
- `ReqSendPublicKeyToCar` — 1
- `ReqStartSession` — 1
- `RespConnectToCar` — 3
- `RespSendPublicKeyToCar` — 1
- `RespStartSession` — 3

### `dashboard.proto` — 10 messages
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

### `ota.proto` — 6 messages
- `ReqOTA` — 5
- `RespOTA` — 3
- `CmdOTASendData` — 2
- `PushOtaStatus` — 2
- `ReqOTAEncryptionV2` — 1
- `RespOTAEncryptionV2` — 1

### `stalks.proto` — 18 messages
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

### `actions.proto` — 30 messages
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
- `ActionUponApproachSettings` — 2
- `ActionsOnReverseSettings` — 2
- `LightsDRLSettings` — 2
- `MuteLaneDepartureSettings` — 2
- `SafeSecuritySettings` — 2
- `SuspensionByGearSettings` — 2
- `CarUnlockScreenSettings` — 2
- `ActionGroup` — 4
- `Action` — 4
- `ActionValue` — 3
- `SmartAction` — 4
- `SmartActionGroup` — 3
- `Automation` — 4
- `ActionMacro` — 3
- `ActionMacroStep` — 3
- `ActionDelay` — 1
- `ActionTrigger` — 3
- `ActionCondition` — 3
- `ActionExecution` — 3
- `ActionTarget` — 2

### `buttons.proto` — 37 messages
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
- `ReqGetButtonBasicInfoList` — 0 (generated alias/descriptor duplicate observation)
- `RespGetButtonBasicInfoList` — 1 (generated alias/descriptor duplicate observation)
- `ReqStartButtonScan` — 1 (generated alias/descriptor duplicate observation)
- `RespStartButtonScan` — 1 (generated alias/descriptor duplicate observation)

### `commander.proto` — 67 messages

The descriptor contains 67 commander-domain messages covering initialization, settings, smart actions, car configuration, vehicle subscriptions, Wi-Fi, ambient lighting, action groups and device permissions. Representative names include:
- `KnobBasicInfo`
- `ReqGetCommanderInitInfo` / `RespGetCommanderInitInfo`
- `ReqGetSmartActionsList` / `RespGetSmartActionsList`
- `ReqSetSmartActionState` / `RespSetSmartActionState`
- `ReqSetAutomationSettings` / `RespSetAutomationSettings`
- `ReqGetAutomationSettings` / `RespGetAutomationSettings`
- `ReqGetCarConfig` / `RespGetCarConfig`
- `ReqGetCarSettings` / `RespGetCarSettings`
- `ReqGetCarSettingsAndConfigs` / `RespGetCarSettingsAndConfigs`
- `ReqSetCarSettingsAndConfigs` / `RespSetCarSettingsAndConfigs`
- `ReqSubscribeByDeviceType` / `RespSubscribeByDeviceType`
- `ReqSubscribeVehicleData` / `RespSubscribeVehicleData`
- `ReqSubscribeVehicleEventsData` / `RespSubscribeVehicleEventsData`

The authoritative vehicle/event schemas are documented separately because they are the highest-value AutoDiag extraction target.

### `dash.proto` — 25 messages

The descriptor contains dashboard initialization, settings, device scan, Wi-Fi, firmware and status messages, including:
- `ErrorInfo`
- `DashSettings`
- `ReqGetDashInitInfo` / `RespGetDashInitInfo`
- `ReqStartDashScanForDevice` / `RespStartDashScanForDevice`
- `ReqSetDashSettings` / `RespSetDashSettings`
- `ReqGetDashSettings` / `RespGetDashSettings`
- `ReqDashStartStop` / `RespDashStartStop`
- `ReqDashWiFiConnection` / `RespDashWiFiConnection`
- `PushDashStatus`
- `PushDashErrorEvent`

## Total

Across the 11 recovered descriptor roots, **216 message definitions** were identified. Some generated/descriptor name observations can duplicate symbol-level aliases; the source of truth for wire decoding is the embedded descriptor itself.

## AutoDiag status

**Wire schema recovery: verified for the recovered descriptor data.**

Physical meaning, ECU origin, units, scaling, vehicle coverage, firmware compatibility and transport framing remain separate evidence questions.
