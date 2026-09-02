# Diagnostic write capability architecture

Diagnostic writes are **not forbidden by the architecture**. They are a separate capability class from passive observation and must be exposed only when the exact vehicle/ECU/procedure is supported and verified.

## Capability classes

```text
OBSERVE
  CAN / OBD / UDS read / measuring values / DTC / identification

SERVICE_WRITE
  service resets / adaptations / basic settings / component learning
  only where the vehicle-specific procedure is verified

CONFIG_WRITE
  coding / long coding / configuration / parameter changes

PROGRAMMING
  ECU software flashing / module programming

SECURITY_CRITICAL
  immobilizer / SecurityAccess-dependent operations / safety-critical routines
```

## Long coding is a write

VAG long coding, adaptation changes and other configuration changes are explicitly **write operations**. They are not to be hidden under a generic read-only diagnostic session.

The architecture therefore separates the ability to *decode/inspect* coding from the ability to *change/write* it.

A safe implementation path is:

```text
Read ECU identification
  ↓
Read current coding / configuration
  ↓
Verify exact vehicle + ECU + HW/SW scope
  ↓
Decode with evidence-backed definition
  ↓
Show proposed change + before/after
  ↓
Explicit user confirmation
  ↓
Required security/preconditions
  ↓
WRITE through isolated service
  ↓
Read-back verification
  ↓
Diagnostic evidence + audit record
```

No proprietary VCDS code, database, security credentials or copied proprietary material is required by this architecture. The goal is interoperability through documented protocols, independently developed decoders and evidence-backed vehicle definitions.

## Current implementation boundary

The current ISO-TP/UDS foundation remains transport/response handling. It must not be interpreted as a promise that every UDS service or every vehicle coding operation is supported.

Future write implementations must remain disabled unless the relevant capability is positively established. `UNKNOWN` is not equivalent to `SUPPORTED`.
