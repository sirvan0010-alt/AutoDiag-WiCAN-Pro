# Experimental Vehicle Summon

Status: **PLANNED / DISABLED / NON-PRIORITY**

This directory is intentionally isolated from the normal diagnostic and read-only paths.

## Goal

Provide a future research boundary for a Tesla-like vehicle summon feature: a phone UI could request controlled vehicle movement through a vehicle-specific control service.

## Current scope

- Architecture and capability boundary only.
- No actuator command implementation.
- No CAN/UDS write sequence is assumed or hardcoded.
- No automatic driving, steering, throttle, brake, gear, parking-brake, or wake-up command is exposed.
- Feature must remain unavailable unless a future vehicle-specific implementation is explicitly verified.

## Required future layers

1. `SummonCapability` — explicit vehicle/profile capability declaration.
2. `SummonPreconditions` — ignition/state, doors, parking brake, obstacle/environment and communication prerequisites.
3. `SummonSession` — bounded session with timeout, heartbeat/liveness and immediate stop semantics.
4. `SummonTransport` — isolated write-capable transport boundary; diagnostic read-only transport must not gain write access implicitly.
5. `SummonVehicleAdapter` — manufacturer/model-specific implementation backed by verified protocol evidence.
6. `SummonAuditEvent` — immutable event/capture record for every request, state transition and stop/failure.
7. UI remains a thin client of the capability/session layer and must not construct vehicle commands.

## Safety boundary

A discovered CAN/UDS service, writable identifier, or proprietary packet is **not** evidence that summon is safe or supported. Reverse engineering alone is insufficient for enabling the feature.

The feature should fail closed when capability, prerequisites, protocol mapping, authorization, or verification scope is missing.

## Relationship to the main diagnostic stack

```text
Vehicle Profile
      |
      +--> Read-only diagnostics (normal priority)
      |
      `--> Experimental control capability (disabled)
                 |
                 +--> Preconditions
                 +--> Verified vehicle adapter
                 +--> Bounded session
                 `--> Explicit stop/fail-closed path
```

The summon experiment must never weaken the existing read-only diagnostic safety model.
