# WiCAN fork evidence register

Status: **REFERENCE / PROVENANCE REGISTER**

Purpose: record what AutoDiag may legitimately import from the public WiCAN firmware ecosystem without treating a fork as an automatic specification.

## Upstream baseline

- Upstream: `meatpiHQ/wican-fw`
- Upstream default branch: `main`
- License reported by GitHub: GPL-3.0
- Upstream repository is the primary firmware reference.

The upstream repository documents WiCAN PRO capabilities including WiFi/BLE/USB connectivity, SLCAN/socketCAN, multiple CAN/OBD protocol families, ELM327/ELM329/STN/VT command sets, and additional reverse-engineering tooling. These are **firmware capabilities**, not automatically Android API guarantees.

## Public forks inspected

### `davidsmyers/wican-fw`

Observed public README confirms the same WiCAN PRO platform and explicitly documents:

- WiFi / BLE / USB interfaces
- SLCAN / socketCAN
- additional reverse-engineering tools
- WiCAN PRO-specific extended protocol support
- ELM327 v2.3, ELM329 v2.2, STN and VT instruction sets

Classification: **DOCUMENTED in a public fork README**.

### `Elmardus/wican-fw-kona`

Observed public README tracks the upstream WiCAN platform and supported interfaces. No unique Kona-specific diagnostic semantics are imported into AutoDiag from the README alone.

Classification: **DOCUMENTED platform reference; no unique decoder evidence**.

### `adam-weber/wican-fw-mqtt-broker`

Observed public README documents the WiCAN PRO platform and MQTT-oriented use. It does not by itself prove any new vehicle diagnostic layout or decoder.

Classification: **DOCUMENTED integration reference; no unique decoder evidence**.

## AutoDiag import rule

A WiCAN fork can provide:

1. implementation ideas for the transport/acquisition layer;
2. examples of device management and telemetry integration;
3. evidence that a capability exists in a real WiCAN firmware lineage;
4. candidate protocol handling to investigate further.

A WiCAN fork must **not** automatically provide:

- verified vehicle signal meanings;
- ECU-specific response layouts;
- CAN IDs for a specific vehicle;
- PID byte offsets or formulas;
- safety-critical write commands;
- vehicle-specific thresholds.

Those require independent evidence and the AutoDiag evidence gate.

## Target architecture

```text
WiCAN firmware ecosystem
        |
        +--> upstream firmware
        +--> public forks
        +--> implementation differences
        |
        v
  provenance register
        |
        v
  transport capability model
        |
        v
  AutoDiag transport/session layer
        |
        +--> raw CAN
        +--> SLCAN
        +--> ELM327 framing
        +--> device status / management
        |
        v
  CAN / ISO-TP / UDS
        |
        v
  diagnostic candidates
        |
        v
  evidence + verification gate
```

## Separation of concerns

`AutoDiag-WiCAN-Pro` owns the Android/application implementation.

`AutoDiag-WiCAN-Diagnostic-Data` owns the evidence-backed candidate registry and provenance.

The WiCAN firmware repositories remain external references. We do not fork firmware source into the Android application and we do not copy GPL firmware code into the Android repository.

## License boundary

The upstream WiCAN firmware repository reports GPL-3.0. AutoDiag therefore uses the firmware repositories as external reference sources and reimplements required client-side protocols from observed/documented interfaces rather than copying firmware implementation code into the Android application.

Any future source-code reuse must be reviewed separately for license compatibility before import.

## Verification labels

- `PROVEN`: directly demonstrated by executable/runtime/decompiled evidence.
- `DOCUMENTED`: explicitly stated by upstream/fork documentation.
- `OBSERVED`: observed in a public implementation but not independently verified on target hardware.
- `INFERRED`: engineering interpretation; never treated as vehicle proof.
- `UNKNOWN`: insufficient evidence.

## Hard rule

A fork changes the **evidence pool**, not the evidence standard.

No decoder becomes verified merely because it exists in a WiCAN fork.
