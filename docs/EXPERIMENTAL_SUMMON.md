# Experimental: Vehicle Summon (privolání vozidla)

**Status:** scaffold in `android/core/.../experimental/summon`
**Priority:** lowest — Phase 12 WRITE, after READ diagnostics.
**Default:** disabled. **Live CAN transmit:** never until a verified binding exists.

## What this is

Tesla-like Summon product slot: user holds a control, vehicle is requested to creep toward the phone / a waypoint, session ends on release or fault.

This is actuator control of a moving vehicle. It is not OBD-II. WiCAN on the diagnostic port is a poor and dangerous channel for OEM Autopark/Summon even if someone publishes CAN IDs.

## Hard rules

1. No invented Tesla / OEM CAN IDs, UDS routines, or "known working" frames.
2. `SummonCommandBinding` starts empty. Controller returns BLOCKED without verified binding for live.
3. Allowed execution modes: DRY_RUN (default) and SIMULATOR. LIVE_VEHICLE is rejected by the safety gate.
4. Requires: Expert mode flag, two-step enable, continuous hold, PARK + speed=0 evidence.
5. Audit log every attempt.
6. UI must not look like it can move a real car.

## State machine

DISABLED → ARMED → HOLDING → COMPLETE | CANCELLED | BLOCKED | FAULT

On finger-up: immediate CANCELLED (fail-safe).

## When it may go LIVE

Only after verified vehicle-scope binding, safety review, simulator dry-run, legal/owner consent, and an explicit repo decision. Until then the feature exists so it is not deleted from the plan.
