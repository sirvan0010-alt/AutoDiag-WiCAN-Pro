# WiCAN PRO LED Activity Indicator

**Status:** `RESEARCH / FIRMWARE-INTEGRATION-TARGET`
**Date:** 2026-09-02

## Purpose

Define a long-term implementation for using the physical WiCAN PRO LEDs as a useful diagnostic/activity indicator while keeping the Android application and vehicle diagnostics safe.

The goal is not merely to blink an LED from Android. The target is a deterministic adapter-state indicator driven by the adapter's own communication state:

| Adapter state | Target indication |
|---|---|
| Powered / idle | steady blue |
| Connection established | slow blue pulse |
| Diagnostic communication | short activity blink |
| High CAN traffic | faster activity indication |
| Communication / internal fault | distinct fault pattern |
| Firmware update | dedicated update pattern, if firmware exposes a safe state |

## What is already evidenced

Upstream WiCAN documentation identifies the OBD hardware LED mapping as:

- Blue LED → GPIO7
- Green LED → GPIO8
- Yellow LED → GPIO9

Recent WiCAN PRO firmware release notes for v4.47/v4.48 also explicitly mention that the console LED command supports RGB color and blink options, including an LED CLI improvement.

This is important: the hardware is not missing an LED-control path. The open question is how the current installed firmware exposes that path, what command syntax is stable, and whether it is appropriate for automatic activity indication.

## Why the current firmware does not automatically provide the requested behavior

There are two separate capabilities:

1. **LED control primitive** — firmware can set/blink an LED when instructed.
2. **LED policy/state machine** — firmware automatically decides when to blink because CAN/ELM327/OBD traffic is occurring.

The public evidence confirms the first capability exists in recent firmware, but does not establish that the second capability is already implemented as a configurable feature.

Therefore AutoDiag must not assume that sending arbitrary LED commands is equivalent to having a supported activity-indicator API.

The firmware already uses LEDs for device states and fault indication in some circumstances. A public report also documents a slowly blinking blue LED during a boot-loop/crash condition. That means an activity feature must avoid stealing or overriding fault states.

## Desired architecture

```text
WiCAN firmware
    |
    +-- power / boot state
    +-- Wi-Fi / BLE state
    +-- CAN RX/TX activity
    +-- ELM327 request/response activity
    +-- error / fault state
    +-- firmware update state
    |
    v
LED state arbiter
    |
    +-- FAULT (highest priority)
    +-- UPDATE
    +-- CONNECTION
    +-- COMMUNICATION
    +-- IDLE
    v
Physical LEDs
```

The state arbiter should have explicit priorities so a diagnostic activity blink can never hide a critical firmware fault or update indication.

## Proposed state model

```text
OFF
IDLE
CONNECTED
RX_ACTIVITY
TX_ACTIVITY
HIGH_TRAFFIC
FAULT
UPDATING
```

The final mapping remains firmware-version dependent.

### Activity throttling

Do not toggle a GPIO for every individual CAN frame. At high bus load this would create excessive interrupts, visual flicker and unnecessary firmware work.

Instead:

- accumulate RX/TX activity over a short time window;
- classify traffic as LOW / NORMAL / HIGH;
- emit a bounded visual pulse;
- cap the maximum blink frequency;
- preserve fault/update indications.

Suggested conceptual classifier:

```text
no activity window       -> IDLE
some activity            -> RX/TX_ACTIVITY
sustained high activity  -> HIGH_TRAFFIC
```

The exact thresholds must be measured and tuned; they must not affect CAN timing.

## Android integration

AutoDiag should treat LED control as an optional adapter capability:

```text
WiCAN capabilities
  └── ledControl
       ├── supported
       ├── firmwareVersion
       ├── commandInterface
       ├── rgbSupported
       ├── blinkSupported
       └── activityModeSupported
```

If `activityModeSupported` is false or unknown, Android must not pretend that automatic activity indication is available.

The app may expose a manual test only when the firmware capability is positively identified. A manual LED test is useful for commissioning the adapter and verifying firmware behavior before enabling automatic activity mode.

## Firmware version matrix

| Firmware | LED command | Blink | Automatic activity mode | Status |
|---|---:|---:|---:|---|
| older/unknown | unknown | unknown | unknown | do not assume |
| WiCAN PRO v4.47+ | evidenced in release notes | evidenced | not established by public evidence | research |
| current installed adapter | **must be read from device** | **must be tested** | **must be tested** | pending real-device validation |

## Safety and reliability rules

1. LED handling must never block CAN RX/TX.
2. LED handling must never delay diagnostic request/response timing.
3. Fault and firmware-update indications have priority over cosmetic activity indication.
4. Android must not repeatedly send high-frequency LED commands over the diagnostic transport as a substitute for firmware-side activity handling.
5. If the adapter firmware does not expose a stable activity API, keep the feature `BLOCKED: firmware activity API not verified` rather than using undocumented commands in production.
6. Any firmware modification must be tested on the bench before use on a vehicle.
7. Firmware update procedures must preserve a recovery path.

## Verification plan

1. Read the exact WiCAN PRO firmware version from the physical adapter.
2. Confirm whether the installed version exposes the LED command in the Web UI/terminal.
3. Determine the exact syntax and semantics from the firmware source/release.
4. Test steady, blink and RGB behavior without vehicle communication.
5. Observe whether existing fault/boot/update patterns override manual LED commands.
6. Connect to a simulator and generate controlled RX/TX activity.
7. Measure visual response while confirming diagnostic timing is unchanged.
8. Test low, medium and high traffic rates.
9. Test reconnect, sleep/wake and firmware update states.
10. Only after successful bench testing, consider a vehicle validation.

## Future enhancement

If upstream WiCAN firmware does not already provide a dedicated activity mode, a future contribution could add one directly to the firmware, preferably as a single local state machine rather than requiring the Android application to issue one LED command per message.

A possible future configuration would be conceptually:

```text
LED activity: OFF | COMMUNICATION | AUTO
LED idle: steady-blue
LED communication: pulse
LED high-traffic: fast-pulse
LED fault: firmware-defined
LED update: firmware-defined
```

This is a target specification, not a claim that these configuration options already exist.

## Non-regression requirement

The LED activity feature must remain documented even if implementation is blocked by firmware/API limitations. Do not delete it from the roadmap merely because the currently installed firmware does not expose the required control path.
