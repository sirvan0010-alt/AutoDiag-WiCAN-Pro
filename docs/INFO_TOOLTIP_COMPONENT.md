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
- `charging_cell_imbalance`
- `recovery`

## Evidence review — cell ΔV

The project must not encode the earlier simple rule `50 mV = bad` as a universal threshold.

The evidence reviewed for the project supports a contextual interpretation:

- Tesla community data show examples around 10 mV at high SOC after settling and examples around 15–20 mV at rest / 45–46 mV during high-SOC charging. These are observations from individual vehicles, not manufacturer limits.
- Tesla community reports also show very large module voltage spreads under heavy load, including an example of roughly 500 mV at 500 A. This demonstrates why peak loaded ΔV cannot by itself be treated as a failure threshold.
- Published EV-battery research confirms that differences in cell capacity, temperature and internal resistance produce voltage variation, and that increasing load can expose differences in resistance. A 2026 study on a degraded EV battery reports triple-digit millivolt divergence under load as an advanced-degradation indicator in its tested vehicle, but that result is vehicle-specific and must not be generalized to every Tesla.
- A peer-reviewed Tesla Model 3 vehicle study directly measured individual-cell voltages during charging and observed different cell-voltage trajectories as charging progressed.
- Temperature materially affects resistance, voltage behavior and available power. Therefore temperature must be recorded alongside ΔV and used as context, not treated as an isolated pass/fail number.

References used during specification review:

- Tesla Motors Club — `Acceptable cell imbalance range?`
- Tesla Motors Club — `Wiki - Sudden Loss Of Range With 2019.16.x Software`
- MDPI / Applied Sciences (2026) — `Voltage Collapse and Early Failure Indicators in a Degraded EV Battery Under High-Current Load`
- MDPI / World Electric Vehicle Journal (2024) — `Quantifying the State of the Art of Electric Powertrains in Battery Electric Vehicles: Comprehensive Analysis of the Tesla Model 3 on the Vehicle Level`
- NREL / Batteries (2024) — `Li-Ion Battery Thermal Character`

The application must distinguish at least:

- rest / low load,
- acceleration / discharge load,
- recovery after load removal,
- AC charging,
- DC fast charging.

The diagnostic question is not simply `what was the maximum ΔV?`. It is `how did the pack behave at a defined SOC, temperature and current, and how did the voltage spread recover afterward?`.

## Tooltip wording policy

The tooltip must **not** claim that a specific number such as 30 mV, 50 mV or 100 mV is universally good or bad.

For the current project specification, the user-facing explanation should communicate the following meaning precisely:

### Cell ΔV — Klid

`Nízký rozdíl napětí mezi články po ustálení je obecně příznivý. Vyšší rozdíl může souviset s rozdílným SOC, kapacitou nebo odporem článků. Hodnotu posuzuj spolu se SOC a teplotou.`

### Cell ΔV — Akcelerace / zátěž

`Při vysokém proudu může rozdíl napětí mezi články nebo moduly krátkodobě výrazně vzrůst kvůli rozdílům v odporu. Špičková hodnota sama o sobě neznamená závadu.`

### Cell ΔV — Recovery

`Po uvolnění zátěže sleduj, zda se rozdíl napětí vrátí blízko hodnotě před zatížením. Trvale vysoká nebo zhoršující se hodnota po ustálení je diagnosticky významnější než krátká špička při akceleraci.`

### Cell ΔV — Nabíjení

`Při nabíjení sleduj průběh jednotlivých článků v čase. Článek nebo skupina, která se opakovaně odchyluje od ostatních při podobném SOC a teplotě, může vyžadovat další diagnostiku.`

These texts are the **current canonical meaning**, not manufacturer thresholds. They must be kept synchronized with the diagnostic engine and vehicle profile.

## Individual-cell charging view

If the vehicle exposes individual-cell or cell-group voltages, the application must record and display them during charging, including both AC and DC charging where the data are available.

Required capabilities:

- live cell/module grid,
- min/max cell and ΔV,
- cell ranking,
- temperature association where available,
- time-series recording,
- replay synchronized with charge current, pack voltage, SOC and temperature,
- selection of an individual cell/module to inspect its complete trajectory.

If only module-level data are available, the UI must explicitly say `module-level` rather than pretending to show individual cells.

## Threshold policy

Thresholds are profile- and context-dependent. They may depend on:

- vehicle model and battery generation,
- battery chemistry,
- SOC,
- temperature,
- current/load,
- charging mode,
- measurement quality,
- settling/recovery time.

A future `thresholds.json` must therefore contain phase/context information rather than one global ΔV limit.

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
