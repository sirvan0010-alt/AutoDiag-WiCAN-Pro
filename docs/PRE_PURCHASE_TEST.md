# PRE-PURCHASE TEST

The PRE-PURCHASE TEST is the user-facing one-tap diagnostic workflow. It is evidence-first and capability-driven: it never fails a vehicle because an optional data source is unavailable.

## User experience

```text
AUTO TEST
PRE-PURCHASE INSPECTION

[ Start test ]

Vehicle: Tesla Model Y
Market: ⚠ US-market configuration
Connection: ✓
Capability discovery: ✓

Battery        collecting...
HV isolation   collecting...
DTCs           collecting...
History        collecting...
```

The workflow can be stopped safely because the initial test is read-only.

## Phase model

```text
IDENTIFY
  ↓
CAPABILITY DISCOVERY
  ↓
REST
  ↓
DTC / FREEZE FRAME
  ↓
LOAD (when safe and supported)
  ↓
RECOVERY
  ↓
CHARGING (AC/DC only when charging is observed)
  ↓
HV ISOLATION
  ↓
ODOMETER / ECU CROSS-CHECK
  ↓
MANUFACTURER-SPECIFIC TESTS
  ↓
ANALYZE
  ↓
REPORT + REPLAY
```

Each phase has an explicit start/end timestamp. A parked session, driving session and charging session are not merged into one infinite log.

## Battery replay requirement

Every available cell/brick sample is retained with timestamp and stable identity.

The replay UI supports:

```text
Pack
 └─ Module / Brick
     └─ Cell 137
          4.103 V  → 4.151 V → 4.168 V → 4.189 V
              ↑ scrub timeline ↑
```

The user can move through the recorded test and inspect the exact historical value of an individual cell at that timestamp. Timestamp indexes use binary-search lookup so large recordings remain responsive.

## Short-loan / one-hour vehicle scenario

When the vehicle has no Battery Fingerprint history, the report explicitly lowers confidence. The test does not pretend that one hour is equivalent to long-term observation.

Example:

```text
Battery Fingerprint: unavailable
Assessment: LIMITED
Reason: no historical baseline for this vehicle

Observed during this session:
- cell delta
- load response
- recovery
- temperature spread
- charging behavior
```

This is preferable to inventing a SOH percentage from a short drive.

## EV checks

Only capabilities actually discovered are executed. Typical available checks include:

- pack voltage/current/SOC
- cell/brick minimum and maximum
- cell delta
- per-cell tracking during charging
- temperature spread
- REST/LOAD/RECOVERY behavior
- AC/DC charging context
- vehicle-reported SOH
- HV isolation/Riso
- DTCs and relevant freeze frames
- 12 V behavior where exposed

A low cell voltage during acceleration is recorded as a load observation. It is not automatically labeled the weakest/faulty cell without contextual and repeated evidence.

## ICE checks

Where verified by the vehicle decoder:

- DTCs and freeze frames
- readiness
- odometer cross-check
- DPF/OPF/SCR/AdBlue/NOx data
- injector correction
- misfire counters
- boost requested vs actual
- rail pressure requested vs actual
- thermal context

## Result categories

```text
FACT
OBSERVATION
EVIDENCE-BACKED FINDING
LIMITED ASSESSMENT
NOT_AVAILABLE
UNKNOWN
```

The report must never convert `UNKNOWN` or `NOT_AVAILABLE` into a negative result.

## Knowledge-base integration

Every actionable DTC or finding may link to the Diagnostic Knowledge Base. The UI shows the source type and verification level. Official Tesla material is preferred when publicly available; community reverse engineering is visibly separated from OEM information.

If no verified repair procedure exists, the report says so.

## Safety

The pre-purchase profile is read-only. It does not perform SecurityAccess, flashing, coding, actuator activation or other write/control operations. Such operations require the separate experimental subsystem and explicit safety controls.
