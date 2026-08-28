# AUTO TEST — result contract

This contract defines what the application is allowed to claim after an automated EV test.

## Result states

```text
GOOD
REVIEW
LIMITED_ASSESSMENT
NOT_TESTED
NOT_AVAILABLE
UNASSESSED
ERROR
```

`NOT_AVAILABLE` means the vehicle/interface does not expose the required telemetry. `NOT_TESTED` means the capability exists but the test phase was not performed. Neither is a failure.

## Finding structure

```json
{
  "finding_id": "stable-id",
  "metric": "cell_imbalance",
  "status": "REVIEW",
  "phase": "RECOVERY",
  "observed_value": 30,
  "unit": "mV",
  "context": {
    "soc_pct": 72,
    "battery_temp_c": 24.1,
    "current_a": 12.0
  },
  "reason": "Persistent deviation observed during recovery",
  "confidence": "MEDIUM",
  "evidence_ids": ["evidence-001"],
  "knowledge_entry_id": "tesla-example-code",
  "source_links": [],
  "verification": "PARTIALLY_VERIFIED"
}
```

The `reason` is generated from observed evidence and deterministic rules. It must not imply a diagnosis that the evidence cannot support.

## Cell selection

For every available cell sample, preserve a stable `cell_id`, timestamp and value. Replay must expose:

```text
Cell 137
4.118 V
+4 mV from population reference

Time 00:07:13.420
Phase LOAD
Current 286 A
```

Changing the replay timestamp updates the exact historical cell value. Changing the selected cell updates its synchronized chart and topology position.

## Charging replay

AC and DC charging have independent phase identifiers. If cell telemetry is available, the same cell can be followed from the beginning to the end of the recorded charging interval. The report must distinguish:

- absolute voltage rise,
- population-relative deviation,
- temperature behavior,
- SOC context,
- pack current and voltage.

## Tooltip contract

Every evaluated metric can expose the reusable `InfoTooltip`. Tooltip text comes from a central knowledge resource and must be consistent with the currently resolved evidence profile. It must not present a provisional community observation as an OEM limit.

## Knowledge link contract

When a DTC/alert has a verified official source, the result screen provides a direct navigation action:

```text
What does it mean?
Official Tesla explanation / service information
Troubleshooting
Repair / service reference
```

Each link records its source type, vehicle scope, verification state and last review. Broken or stale links become `NEEDS_REVIEW`.

## Market warning

If the vehicle market is reliably decoded as US/Canada/etc., the result shows the market explicitly. A US-market vehicle may display a prominent `! US MARKET` notice when regional differences could affect charging, connectivity, homologation, equipment or diagnostics. If market cannot be reliably determined, show `MARKET UNKNOWN` rather than guessing.

## Buyer report

The PRE-PURCHASE report must include an explicit coverage section:

```text
DATA COVERAGE
Battery cells          96% available
Battery temperatures    PARTIAL
Riso                    VEHICLE STATUS ONLY
AC charging             NOT TESTED
DC charging             NOT TESTED
DTC                     COMPLETE

ASSESSMENT
LIMITED ASSESSMENT
Reason: charging and long-term battery history unavailable.
```

This makes the report useful for a one-hour borrowed vehicle without pretending that a short test proves long-term battery health.
