# EV Diagnostic Core — implementation contract

This document turns the current architecture into an implementation contract for the next code cycle.

## 1. Immutable diagnostic evidence

Every decoded value must carry:

```text
MetricSample
  timestamp
  vehicle identity scope
  ECU/source
  metric
  value + unit
  phase
  context (SOC, pack voltage/current, temperature where available)
  provenance
  verification state
```

A decoder may return `unknown` rather than inventing a value.

## 2. Capability discovery

Discovery runs before vehicle-specific health tests. The result is cached using the strongest available identity:

```text
VIN + vehicle software/firmware scope + BMS/ECU software scope
```

If VIN is unavailable, the cache key must explicitly indicate that limitation rather than silently treating model/year as unique.

Capabilities are granular. Examples:

```text
Battery
  pack_voltage             AVAILABLE
  pack_current              AVAILABLE
  cell_voltage              PARTIAL
  cell_temperature          UNAVAILABLE
  module_temperature        AVAILABLE
  riso_value                UNKNOWN
```

`PARTIAL` means the application knows that only part of the requested population/metric is exposed. It must not display a complete battery grid as if every cell were known.

## 3. Session and phase model

A diagnostic session is finite and consists of explicit phases:

```text
CONNECT
DISCOVERY
REST
LOAD
RECOVERY
CHARGE_AC
CHARGE_DC
HV_ISOLATION
ANALYSIS
COMPLETE
```

A phase ends on an explicit transition, timeout, user stop, connection loss or safety/data-quality gate. Every sample retains its phase so replay and automation cannot confuse charging with driving.

## 4. Battery analysis rules

The engine computes observations, not unsupported diagnoses.

For load testing it records:

```text
baseline voltage
minimum/maximum observed values
current
pack voltage
SOC
temperature
cell population spread
module spread
recovery at timestamped intervals
```

A minimum cell voltage during high current is a load observation only. The engine must not label that cell "weak" unless additional evidence supports a persistent deviation pattern.

`delta_mv_per_100a` is available as a normalized analytical metric. It has no production threshold unless an evidence record resolves one for the applicable vehicle/pack and test context.

## 5. Charging cell tracking

During AC and DC charging, if cell-level telemetry is available, the recorder stores the full available population at every sampling point. The replay index permits:

```text
select cell → select timestamp → exact historical value/context
```

The UI can show both absolute voltage and deviation from the contemporaneous population. Cell identity must remain stable throughout a session.

## 6. Riso / HV isolation

Three data modes are mandatory:

```text
VALUE       → vehicle supplied a decoded isolation measurement
STATUS_ONLY → vehicle supplied only a state
RAW         → raw response exists but mapping is not verified
```

The application never converts `STATUS_ONLY` or `RAW` into an invented resistance value.

Vehicle-reported isolation and results of a separate physical insulation test are different evidence types and must never be merged.

## 7. Evidence-backed result resolver

The resolver receives:

```text
observation + context + vehicle profile + evidence records
```

It returns:

```text
status
reason
confidence/data-quality
supporting evidence IDs
```

No evidence → `UNASSESSED`, not `GOOD` and not `FAULT`.

Provisional evidence → visible `PROVISIONAL` marker.

Vehicle-reported status → visible `VEHICLE REPORTED` provenance.

## 8. Replay performance

Logs must maintain a timestamp index. Scrubbing uses binary search (or an equivalent indexed lookup), not a full scan of all samples. The selected timestamp becomes the single source of truth for synchronized battery, charging, thermal and event panels.

## 9. AUTO TEST / PRE-PURCHASE

`AUTO TEST` is read-only and selects stages from discovered capabilities. Unsupported stages become `NOT_AVAILABLE`.

`PRE-PURCHASE TEST` produces a buyer-oriented report containing:

- vehicle identity and market indication
- capability coverage
- battery observations
- load/recovery observations
- charging observations when actually tested
- HV isolation observations when exposed
- DTC/alert findings
- confidence/data completeness
- source/provenance links
- limitations and untested items

It must never manufacture a complete health score from missing measurements.

## 10. Diagnostic Knowledge Base

A finding may link to an OEM explanation, troubleshooting procedure or service reference only when the URL has been verified and its vehicle scope is known. Community references remain separately labelled.

The UI path is:

```text
Finding → What it means → Evidence → Official source → Troubleshooting → Service/repair reference
```

If no verified OEM procedure exists, display that fact and do not substitute an AI-generated repair procedure.

## 11. Automation boundary

Automation actions are explicitly classified:

```text
READ      read vehicle data
LOG       persist observations
ANALYZE   run deterministic analysis
NOTIFY    external notification/MQTT/Home Assistant publication
WRITE     vehicle command/control
```

`AUTO TEST` may use READ/LOG/ANALYZE. NOTIFY requires its own policy, cooldown and audit trail. WRITE is isolated and cannot be enabled implicitly by an automation rule.

## 12. Definition of done

A vehicle-specific feature is complete only when it has:

1. deterministic unit tests,
2. simulator or replay coverage,
3. provenance/evidence metadata,
4. explicit verification scope,
5. failure/unknown handling,
6. documentation,
7. real-vehicle validation where practical.
