# Diagnostic Knowledge Base

## Purpose

Every user-visible DTC, alert or diagnostic finding should open an evidence trail instead of a bare code.

```text
Finding
  -> what it means
  -> affected system/component
  -> observed vehicle data
  -> why AutoDiag flagged it
  -> official source
  -> troubleshooting/service reference
  -> related measurements
```

## Source hierarchy

1. **OEM service documentation** — highest priority for meaning, conditions, affected components and repair procedure.
2. **Regulatory / safety authority** — recalls, safety defects, emissions requirements and regulatory context.
3. **Verified engineering documentation** — protocol/decoder information with explicit scope.
4. **Community verified** — useful when OEM information is unavailable, but never presented as OEM guidance.
5. **Generated explanation** — language assistance only; it must not invent a repair procedure or source.

Every knowledge item carries:

```json
{
  "source_type": "oem_service | regulatory | engineering | community_verified | generated_explanation",
  "verification": "unverified | partially_verified | verified",
  "scope": "vehicle/model/software scope",
  "url": "https://...",
  "last_checked": "ISO8601",
  "status": "active | needs_review | broken | unavailable"
}
```

## Tesla official links

AutoDiag should prefer the official Tesla Service portal for Tesla explanations. Tesla publishes repair and maintenance information, service manuals, wiring diagrams and diagnostic information there. The portal also documents Service Mode, DTCs and diagnostic software.

- Tesla Service: https://service.tesla.com/
- Czech Tesla Service portal: https://service.tesla.com/cs-CZ/
- Tesla Diagnostic Software: https://service.tesla.com/cs-CZ/diagnostic-software
- Tesla Remote Connections and Diagnostics: https://service.tesla.com/en-US/remote-connections-diagnostics

For a Tesla DTC/alert, the UI should expose an **Official Tesla explanation** button when a matching official article is known. If the official article requires Tesla authentication/subscription, the UI must say so rather than pretending the article is publicly available.

## Repair guidance rule

The app may summarize a verified source. It must not fabricate torque values, component locations, connector pins, isolation procedures, calibration sequences or software commands.

If no verified repair procedure exists:

> Official repair procedure not available in the AutoDiag knowledge base. Open the manufacturer service source or consult a qualified technician.

## Explanation card

```text
DTC / ALERT
P0XXX / vehicle-specific alert

WHAT IT MEANS
Short source-backed explanation.

WHY IT APPEARED
Observed signal(s), ECU and conditions.

IMPACT
Driver-facing consequence, if documented.

AUTO DIAGNOSIS
STATIC / LOAD / RECOVERY / TREND / CONFIDENCE

SOURCES
✓ OEM Service Documentation
  [Open official explanation]

RELATED DATA
Battery voltage · temperature · current · Riso · charging state
```

## Link integrity

Links are periodically checked. A broken or redirected source becomes `needs_review`; it is never silently replaced with a random third-party page.
