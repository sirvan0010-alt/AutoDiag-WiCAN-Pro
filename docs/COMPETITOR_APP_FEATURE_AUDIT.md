# Competitor app feature audit (READ-only takeaways)

**Date:** 2026-09-03
**Sources inspected as APKs (not copied):** Torque 1.10.144, Car Scanner 2.1.50, PHEV Watchdog Lite 1.9.1, Mitsubishi Remote Ctrl A.3.1.6

## Legal / safety rules

- Do not copy proprietary PID tables, DTC text DBs, OEM remote-control protocols, bitmaps, or UI.
- Do copy ideas and public protocol facts (SAE J1979, ELM327 AT commands).
- Design is not taken from these apps. Target is Tesla-like light/dark — see `docs/UI_TESLA_THEME.md`.
- Vehicle motion / lock / HVAC WRITE stays in `experimental/` and Phase 12.

## Already in AutoDiag main (do not duplicate)

| Competitor idea | Already in repo |
|-----------------|-----------------|
| ELM TCP session + `>` buffering | `Elm327Session` |
| Mode 01 registry + live poll engine | `ObdPidRegistry`, `ObdLiveDataEngine`, `ObdCapabilityScanner` |
| DTC 03/07/0A decode + Mode 04 model | `ObdDtcDecoder`, `ObdDtcClearResult` |
| RAW CAN monitor + capture CSV + replay | `can/` |
| Transport RX/TX metrics | `TransportMetrics` |
| ISO-TP RX reassembly + UDS risk class | `isotp/`, `uds/UdsService` |
| Simulator path | `SimulatorWiCanTransport` |

## Torque — inspiration only

Patterns: selectable PID dashboard, user-PID CSV columns (`Name, ShortName, ModeAndPID, Equation, Min, Max, Units, Header, startDiagnostic, stopDiagnostic`), vehicle profiles for 0-60, freeze frame, Mode 05, fault log, BLE, HUD/floating display, alarms, GPS track.

Take later: user-PID schema with `verification: unverified`, alarms, HUD, 0-60 with explicit mass.

Do not import: `faultcodes.dat`, Prius/Orion/VW ECU CSV from the APK.

## Car Scanner

Xamarin/AOT; no plaintext PID DB extracted. Take: 1-16 widgets, raw vs decoded, connection quality chip. Skip OEM Mode 22 packs without evidence.

## PHEV Watchdog Lite — public ELM AT pattern

```
ATZ / ATE0 / ATL0 / AT ST C8 / AT SP 6 / ATH1
ATSH <ecu>
ATFCSH <ecu> / ATFCSM1 / ATFCSD300000
01 xx / 09 02 / 21 xx (OEM KWP read)
```

Implemented as command builders in `ElmIsoTpAtCommands`. No ECU IDs marked AVAILABLE.

PHEV measurement *names* (capability IDs only): displayed SOC, pack current/power, aux 12V, cell min/max/avg, OBC, front/rear motor, fan PWM. All `UNAVAILABLE` until a verified profile exists.

Illustrative headers from one log (unverified): `7E0`, `761`, `765`, `724`, `755`, `753`, `73C`.

## Remote Ctrl (Inventec iMobile2)

Cloud/Wi-Fi telematics: climate timer, charge schedule — not OBD. Future official OEM API only. Do not port this APK protocol.

## Next (after green assembleDebug)

1. Live Data UI on existing engine
2. DTC list UI + freeze/readiness
3. Dashboard + Tesla chrome
4. ELM ISO-TP AT helpers for OEM multi-frame READ
5. Experimental Summon remains dry-run
