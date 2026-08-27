# InfoTooltip — reusable metric help component

## Purpose

`InfoTooltip` is a reusable Android UI component for explaining diagnostic metrics without cluttering the main dashboard.

It is intended for cell ΔV, pack voltage, voltage sag, temperature, SOC, current, power, modules, cells and future metrics.

## Visual specification

- Small 16–18 px circular `?` control.
- Thin border; no unnecessary shadow or gradient.
- Positioned immediately beside the value/unit it explains.
- Closed state uses a neutral secondary color.
- Open state uses the application accent color.
- Expand/collapse should be animated rather than an abrupt layout jump.
- Help content should be short and phase/context aware.

## API concept

```kotlin
InfoTooltip(
    anchorLabel = String,
    content = List<InfoSection>
)

InfoSection(
    heading = String,
    body = String
)
```

The exact API may change during implementation, but metric help content should remain centralized rather than duplicated throughout individual screens.

## Centralized content

Recommended implementation:

- `MetricHelpContent.kt`, or
- a localized resource/JSON representation.

Each metric has a stable key, for example:

- `cell_imbalance`
- `pack_voltage`
- `voltage_sag`
- `pack_temperature`
- `temperature_delta`
- `soc`
- `current`
- `power`
- `module`
- `cell`

## Cell imbalance wording

The UI must not present a universal statement such as `50 mV = bad`.

Cell ΔV is context-dependent. During high-current acceleration or regenerative/charging events, transient increases can be expected. The application should distinguish:

- rest / low load,
- acceleration / discharge load,
- recovery after load removal,
- AC charging,
- DC fast charging.

The important diagnostic question is not simply the largest instantaneous ΔV, but how the pack behaves under a defined condition and whether the imbalance recovers as expected.

Any numeric threshold shown to the user must be tied to a defined vehicle/battery profile and verification status. Thresholds are diagnostic guidance, not a universal manufacturer-independent failure boundary.

## Checklist

- [x] Cell imbalance ΔV
- [ ] Pack voltage
- [ ] Voltage sag
- [ ] Pack temperature
- [ ] Temperature delta
- [ ] SOC
- [ ] Battery current
- [ ] Power
- [ ] Individual module
- [ ] Individual cell
- [ ] Charging metrics
- [ ] Recovery metrics

## Synchronization rule

Help text must remain consistent with the active diagnostic rules and threshold configuration. If thresholds become vehicle-specific, the explanatory content must make that context clear.

## UX principle

The main screen should remain calm and readable. The `?` control provides deeper explanation on demand instead of putting diagnostic theory beside every number.
