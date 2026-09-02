# Integrated Automotive Oscilloscope Architecture

## Goal

AutoDiag-WiCAN-Pro is intended to grow beyond OBD/UDS scanning into an open automotive measurement platform. A future oscilloscope capability is therefore a first-class feature, not a disposable experiment.

The oscilloscope layer is hardware-neutral: Android owns capture configuration, visualization, measurement, evidence, and replay; a compatible WiCAN extension or dedicated measurement hardware supplies the sampled electrical signal.

## Capability states

The application must distinguish:

- `AVAILABLE` — the connected hardware exposes a verified oscilloscope input.
- `AVAILABLE_WITH_ADDITIONAL_HARDWARE` — the software supports it, but the connected WiCAN setup needs a measurement module/probe/interface.
- `NOT_SUPPORTED` — the active hardware cannot provide the required signal path.
- `UNKNOWN` — capability has not been verified.

The current WiCAN PRO CAN interface must **not** be marketed as a built-in analog oscilloscope unless the required analog sampling hardware and firmware path are actually verified.

## Planned measurement stack

1. **Signal source** — analog input / future dedicated automotive measurement module.
2. **Sampler** — timestamped voltage samples with a known sample rate and input limits.
3. **Capture engine** — rolling buffer, single-shot capture, trigger and pre/post-trigger windows.
4. **Measurement engine** — min, max, peak-to-peak, mean, frequency, period, duty cycle and RMS where sampling permits.
5. **Viewer** — zoom, pan, time/div, volts/div, cursors and frozen captures.
6. **Evidence** — persist captures with vehicle/ECU scope, timestamps, hardware capability and provenance.
7. **Correlation** — align analog signals with CAN frames, DTCs and diagnostic events.
8. **Replay/export** — deterministic replay and CSV export for analysis and regression tests.

## Trigger model

The first generic trigger implementation supports rising/falling threshold crossings with optional hysteresis. Future versions can add pulse-width, window, runt-pulse, external/event trigger and CAN/UDS-correlated triggers.

## Automotive use cases

The long-term UI should support measurements such as:

- crankshaft/camshaft sensor signals,
- injector and actuator control signals where the hardware is electrically rated for them,
- throttle/position sensor signals,
- pressure transducer outputs,
- PWM control and feedback,
- charging and battery-related low-voltage signals,
- CAN activity correlated with an analog sensor.

High-voltage traction-battery measurements are a separate safety-critical hardware domain. The software must never imply that a normal oscilloscope input is safe for direct HV connection.

## Safety

Probe/input ratings, grounding, isolation, category rating, maximum voltage, current limits and protection must come from the actual measurement hardware. Software cannot make an electrically unsafe probe safe.

Before capture, the UI should show the selected channel's verified electrical limits and refuse configurations outside those limits.

## Relationship to diagnostics

Oscilloscope captures are diagnostic evidence. A useful future workflow is:

`Vehicle → ECU/DTC → suspected signal → oscilloscope channel → capture → measurement → CAN/UDS correlation → evidence → report`

This deliberately complements, rather than replaces, the existing CAN/OBD/ISO-TP/UDS pipeline.

## Current implementation

The repository now contains hardware-neutral Kotlin models for:

- oscilloscope capability and channel limits,
- timestamped samples,
- captures and basic measurements,
- rising/falling edge trigger detection.

This is the software foundation only. A real integrated oscilloscope requires a verified sampling front end, firmware/data transport, electrical protection and corresponding hardware capability advertisement.

## Roadmap

- [x] Capability model
- [x] Timestamped sample/capture model
- [x] Basic measurements
- [x] Basic trigger detector
- [ ] Streaming capture engine
- [ ] Ring buffer + pre/post-trigger capture
- [ ] Frequency/duty/RMS measurement algorithms with sampling validation
- [ ] Android waveform viewer
- [ ] CAN/UDS event correlation
- [ ] CSV/replay format
- [ ] Verified WiCAN-compatible measurement hardware path
- [ ] Multi-channel synchronization
- [ ] Automated probe/input safety checks
