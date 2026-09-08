# Bus Health and Auto-Electrician Diagnostics

AutoDiag has a separate physical-bus observation layer. It does not confuse a CAN-protocol error with an ECU fault.

## Measurements

Where the adapter/driver exposes them, record:

- nominal bitrate and evidence for selected configuration
- frame rate
- bus load estimate
- standard/extended frame ratio
- error-frame observations
- error-passive / bus-off state
- dropped/overrun frames
- receive queue saturation
- timestamp jitter
- active CAN IDs and response topology

## Bus-load calculation

For a raw capture, AutoDiag may calculate an **observed bus-load estimate** from frame length, bitrate and time window. The result must be labelled as an estimate unless the hardware provides an authoritative bus-load counter.

## Physical termination test

A resistance measurement such as approximately 60 ohms across a correctly powered-off, dual-terminated CAN network is a **physical test**, not a software observation. AutoDiag may store the technician's measured value, meter/source and conditions, but must not pretend that software CAN traffic proves termination resistance.

## Diagnostic interpretation

```text
Bus health
  Frames/s          1,842
  Observed load     41 % (calculated)
  Error frames      0 observed
  Bus-off            no
  Dropped frames    12

Interpretation
  ⚠ Capture loss detected
  Cause not established
```

Possible causes include adapter throughput, Wi-Fi loss, buffer overflow or an actual vehicle-bus issue. The app must not jump directly from dropped frames to "bad wiring".

## Scope

K-Line and J1850 are represented as separate protocol transports. Their timing and error semantics must not be forced into the CAN model.
