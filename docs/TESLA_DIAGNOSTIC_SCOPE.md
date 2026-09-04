# Tesla diagnostic scope

The deferred Tesla diagnostic area is now represented as an explicit capability catalog in `core/capability/TeslaDiagnosticCapabilities.kt`.

## Included domains

- powertrain / drive control
- brake electronics (ABS/ESP)
- body control (BCM)
- airbag / restraint controller
- electronic parking brake (EPB)
- instrument cluster
- parking assistance
- door electronics
- steering / electric power steering
- infotainment
- battery management system (BMS)

## Functional scope

The catalog covers the requested diagnostic functions at the capability level:

- DTC readout
- live/measured data
- actuator/output tests
- service procedures
- calibration/adaptation
- configuration where supported
- battery cell, temperature, SOC/SOH and power/charging diagnostics

The catalog intentionally does **not** contain guessed Tesla CAN IDs, undocumented payloads, security access sequences, or write commands. Those must be supplied by a vehicle-specific definition and verified against the discovered ECU/protocol before execution.

Safety-sensitive operations remain explicitly classified as service/configuration/control operations. Presence in the catalog is therefore not permission to execute them.

## Integration path

```text
Tesla vehicle discovery
        -> ECU identity/topology
        -> protocol + vehicle-specific definition
        -> capability discovery
        -> DiagnosticEvidence
        -> live data / DTC / service operation
        -> capture + replay + verification
```

This keeps the transport layer independent of Tesla business meaning and allows the same diagnostic engine to serve ICE, hybrid and EV vehicles.
