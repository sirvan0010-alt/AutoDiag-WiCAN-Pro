# S3XY 6.8.2 — APK reference extraction

**Artifact:** `S3XY_6.8.2.xapk`  
**Purpose:** independent behavioural/capability extraction for AutoDiag-WiCAN-Pro. No vendor binary/source/UI is copied into the repository.

## Artifact evidence

- XAPK SHA-256: `b6d5188457978b4a9fd0e4c138d316abb7144291ff0eda97b20678a135faddc9`
- XAPK size: 153,942,169 bytes
- APK splits: 3 (`base.apk`, `split_0.apk`, `split_1.apk`)
- Native library: `libS3XYButtons_arm64-v8a.so`, 299,688,392 bytes
- Native Build ID: `1148f4bc53db364e6f769b56920effed02c30a3b`
- ABI/API/toolchain: arm64-v8a, Android 28, NDK r27c (12479018)
- Qt: 6.11.2

## Static extraction result

- 64,667 native project symbols were recovered from the non-Qt application surface.
- 853 project source/header paths were recovered from DWARF debug information; third-party library paths are excluded from the repository inventory.
- More than 100 QML cache compilation units were identified.

### Major functional surfaces

- **S3XY Buttons / Commander** — configurable single/double/long actions, action metadata, custom properties and confirmation flags.
- **Dash / Dashboard** — device connection, settings, firmware/update handling, data factors and dashboard configuration.
- **Knob** — menu/page/element configuration, haptic/backlight/display settings and OTA paths.
- **Stalks** — scan/connect, basic/detailed info, battery and OTA-related flows.
- **Strip** — lighting/color/effect/segment configuration and effect settings.
- **Tesla BLE** — BLE discovery/session and Tesla BLE view/status integration.
- **Automation** — approach, reverse, ambient-light, window-drop, preconditioning, suspension, volume-on-exit and kickdown settings.
- **Transport/provisioning** — BLE transport, protobuf message model, security modules, OTA/update helpers and Wi-Fi configuration.

## Vehicle-action capability catalog

| Domain | Observed capability names | AutoDiag treatment |
|---|---|---|
| Access | Lock, Unlock, LockUnlock | `USER_CONFIRMATION` / profile-gated |
| Doors | OpenDoor, OpenDoorFL/FR/RL/RR, PresentingDoors | profile-gated write capability |
| Cargo | OpenTrunk, OpenFrunk, TrunkOpened, FrunkOpened | profile-gated write capability |
| Charging | OpenChargePort, ChargePort, ChargePortSecurity, ChargePortStatus, Charging | read state; control gated |
| Climate | ClimateKeeperMode, ClimateSystemOnOff, BatteryPrecond, RemoteBatteryPreheat, KeepClimateOn | read-first; control gated |
| Lights | low/high beam, flash/strobe, front/rear/all fog, yoke high beam | profile-gated write capability |
| Windows | Closed/Open/Vent/CarWash positions | profile-gated write capability |
| Mirrors | Fold, Tilt, Dip, Dim | profile-gated write capability |
| Seats | front-left/front-right cooling, seat profiles | profile-gated write capability |
| Driver assistance | Autopilot controls, LaneDepartureAvoidance, BlindSpot | experimental/profile-gated |
| Security | SentryMode | profile-gated |
| Convenience | CampMode, DogMode, EasyEntry, VolumeOnExit, Kickdown | profile-gated |
| Speed | SpeedControl increase/decrease actions | profile-gated |
| Ambient lighting | acceleration, charging, greeting, turn-signal, blind-spot, speed-limit effects | profile-gated |

## Automation structures recovered

- `Automation` protobuf object contains settings for actions on reverse, actions upon approach, ambient-light effects, auto-window-drop, camera-on-fold-mirror, diagnostics, emergency lane-keep-off, general ambient lighting, kickdown, lights, passenger easy-entry, remote battery preheat, scroll-wheel settings, suspension-by-gear and volume-on-exit.
- `CActionItem` exposes action type, name/description/icon, custom properties 1/2, settings type, and an action-confirmation flag.
- `CActionsModel` resolves action descriptions, custom properties and action indexes.
- Button models expose single, double and long action collections plus search/filter/try/save flows.
- Automation state is represented separately from the action item model.

## Important implementation boundary

The extraction establishes **capability and data-model evidence**, not verified vehicle protocol support. The AutoDiag evidence architecture therefore keeps vendor-derived vehicle actions profile-scoped and non-automatic until independently verified.

## Reuse mapping

| S3XY reference surface | AutoDiag target layer |
|---|---|
| Action item + action type + custom properties | typed `VehicleCapability` / `ActionDefinition` |
| Automation settings | automation rule/action provider layer |
| BLE transport/session | transport adapter boundary; not copied |
| protobuf message families | independent schema documentation / adapter only |
| device discovery | capability/discovery provider |
| OTA/update flows | adapter firmware capability boundary |
| vehicle action names | evidence-backed profile catalog; writes remain gated |

## Verification state

- Static structure: **OBSERVED_STATIC**
- Capability names: **OBSERVED_STATIC**
- Vehicle-specific CAN/UDS mappings: **not established by this extraction alone**
- Vehicle write support in AutoDiag: **not enabled by this extraction**
- Provenance: supplied `S3XY_6.8.2.xapk`, native DWARF symbols/source paths and embedded string/metadata evidence.

See `S3XY_6_8_2_ACTIONS.md`, `S3XY_6_8_2_SOURCE_INVENTORY.md` and `diagnostic-data/references/s3xy_buttons_6_8_2.json` for the machine-readable/detail inventories.
