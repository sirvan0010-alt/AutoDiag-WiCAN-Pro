# S3XY 6.8.2 — action and automation extraction

Static evidence from `libS3XYButtons_arm64-v8a.so`. This inventory describes interfaces and capability names; it does not copy vendor implementation.

## Core action model

- `CActionItem`: action type, name/description (including translated description), icon, custom properties 1/2, settings type, confirmation flag and new-action flag.
- `CActionsModel`: action lookup/indexing, descriptions, custom properties and indexed button actions.
- Button data/models expose single, double and long action collections and save/try/search/filter flows.

## Automation object

The `Automation` message references these setting families:

- `ActionsOnReverseSettings`
- `ActionsUponApproachSettings`
- `AmbientLightEffectsAutomationSettings`
- `AutoWindowDropSettings`
- `CameraOnFoldMirrorsSettings`
- `DiagnosticSettings`
- `EmergencyLaneKeepOffSettings`
- `GeneralAmbientLightSettings`
- `KickdownAutomationSettings`
- `LightsAutomationSettings`
- `PassengerEasyEntryAutomationSettings`
- `RemoteBatteryPreheatSettings`
- `ScrollWheelButtonSettings`
- `SuspensionByGearSettings`
- `VolumeOnExitAutomationSettings`

## Ambient-light automation fields

Static protobuf accessors expose acceleration mode, autopilot, battery preconditioning, blind-spot assist, charging mode, greeting animation, open-door warning, speed-limit mode and turn signals.

The `CAllAmbientLightEffectsSmartActionObj` surface additionally exposes configuration for:

- acceleration: Chill / Sport / Insane, brightness and dash/front-door/rear-door segments
- autopilot: NAG / On, brightness/color and segments
- battery preconditioning segments
- blind-spot: always / on-turn, brightness/color and reverse/split/door/dash segments
- charging: brightness/color and segments
- greeting rainbow: brightness/color and segments
- open-door warning: active, brightness/color
- speed-limit: brightness/color, sign index/offset and segments
- turn signals: on-turn / hazard, brightness/color and segments
- disabled blinking on turn
- active-heating and requested-state lighting

## Vehicle action families observed in the binary

| Domain | Observed names / families |
|---|---|
| Access | `Lock`, `Unlock`, `LockUnlock` |
| Doors | `OpenDoor`, `OpenDoorFL`, `OpenDoorFR`, `OpenDoorRL`, `OpenDoorRR`, `PresentingDoors*` |
| Cargo | `OpenTrunk`, `TrunkOpened`, `OpenFrunk`, `FrunkOpened`, `FrunkOpenLeftDoor`, `FrunkOpenRightDoor` |
| Charging | `OpenChargePort`, `ChargePort*`, `Charging` |
| Climate | `ClimateKeeperMode`, `ClimateSystemOnOff`, `BatteryPrecond`, `RemoteBatteryPreheat`, `KeepClimateOn` |
| Lights | low beam, high beam, flash, strobe 2–10, front/rear/all fog, yoke high beam |
| Windows | `WindowsPosition_Closed/Open/Vent/CarWash` plus 0–100 positions |
| Mirrors | `MirrorsFold`, `MirrorsTilt`, `MirrorsDip`, `MirrorsDim` |
| Seats | front-left/front-right cooling and seat profiles |
| Driver assistance | `Autopilot*`, `LaneDepartureAvoidance`, `BlindSpot*` |
| Security | `SentryMode` |
| Convenience | `CampMode`, `DogMode`, `EasyEntry*`, `VolumeOnExit*`, `Kickdown*` |
| Speed | `SpeedControl_Increase_1/5`, `SpeedControl_Decrease_1/5` |

## Device surfaces

The binary also contains device models and flows for Commander, Dash/Dashboard, Knob, Stalks and Strip, including BLE discovery/connection, Wi-Fi configuration, security state and OTA/update handling.

## AutoDiag mapping

These observations become **evidence-backed capability names**, not direct protocol implementations. Vehicle writes stay profile-gated and require independent CAN/UDS or official-protocol verification before being enabled. This preserves the repository's existing safety/evidence boundary.

## Verification

- Structure: `OBSERVED_STATIC`
- Action names: `OBSERVED_STATIC`
- Vehicle CAN/UDS mappings: `UNVERIFIED`
- Vendor binary/source copied: **NO**
- AutoDiag vehicle writes enabled by this extraction: **NO**
