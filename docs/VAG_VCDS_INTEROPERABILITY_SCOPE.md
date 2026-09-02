# VAG / VCDS Interoperability Scope

## Objective

AutoDiag should eventually cover as much useful VAG diagnostic functionality as can be implemented through verified protocols, documented behavior, legal/public information and licensed data.

The goal is **functional interoperability**, not cloning proprietary VCDS software, binaries, source code or protected databases.

## Target capability families

- ECU identification and software/hardware identification
- full ECU topology / gateway discovery
- DTC read, clear and freeze-frame where supported
- measuring values / live data
- basic settings
- adaptations
- output/component tests
- service resets
- gateway installation/list/topology
- coding and long coding
- adaptation channels
- UDS and KWP diagnostic services where verified
- vehicle-specific service procedures
- guided diagnostic workflows based on verified evidence

## Coding / long coding

Coding is a future high-risk subsystem, not a generic string editor.

Each coding operation must have:

- exact vehicle and ECU scope
- ECU HW/SW identification
- protocol and addressing evidence
- coding structure definition
- original coding backup where technically possible
- byte/bit or parameter provenance
- validation rules
- simulator/replay coverage where possible
- explicit user confirmation
- safe abort/recovery behavior
- post-write verification

A coding feature must remain `UNKNOWN` or `BLOCKED` until these conditions are met.

## Examples of future VAG coding targets

The catalog may eventually include lighting-related coding (including short/long-beam behavior), comfort settings, equipment configuration, retrofit coding and other adaptation/coding functions when exact vehicle/ECU behavior is verified.

The application must never infer a bit meaning merely from a similar vehicle or from a copied VCDS label.

## Legal/interoperability boundary

Do not copy VCDS proprietary code, binaries, databases, labels, security algorithms, credentials or copyrighted material without permission.

Use:

- documented automotive standards
- manufacturer documentation that may legally be used
- licensed professional data
- public technical information
- independently collected and verified engineering evidence

When a function requires OEM security access or protected credentials, expose the capability and its blocker in the catalog rather than pretending it is executable.

## Developer workflow

1. Identify the vehicle/ECU and protocol.
2. Capture read-only traffic first.
3. Establish a reproducible diagnostic transaction.
4. Record evidence and scope.
5. Implement a typed protocol/service layer.
6. Add simulator/replay tests.
7. Add read-only UI and diagnostics.
8. Only then consider a controlled write implementation.
9. Verify post-write state and recovery.

## Status rule

A feature being listed here is a **long-term target**, not a claim that the current APK supports it.
