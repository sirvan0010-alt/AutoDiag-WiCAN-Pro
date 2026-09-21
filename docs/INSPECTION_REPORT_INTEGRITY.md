# Inspection Report Integrity

## Goal

A pre-purchase report should be reproducible and tamper-evident without pretending that cryptography proves the vehicle condition.

## Dataset

The inspection dataset contains raw frames and normalized observations used by the report. It should include:

- report ID
- VIN or privacy-preserving vehicle identifier where appropriate
- capture start/end timestamps
- adapter/transport identity
- firmware/software versions
- capability snapshot
- raw capture references
- normalized findings
- source/provenance metadata

## SHA-256

Before report publication, serialize the canonical inspection dataset deterministically and calculate SHA-256.

```text
canonical dataset
      |
      v
    SHA-256
      |
      +--> report header
      +--> QR payload
      +--> exported metadata
```

The digest proves that a later copy matches the hashed dataset. It does not prove that the adapter was genuine, that the VIN belongs to the presented vehicle, or that an interpretation is correct.

## QR payload

Use a compact versioned payload, for example:

```json
{
  "schema": "autodiag-report-v1",
  "report_id": "...",
  "dataset_sha256": "..."
}
```

Do not put secrets, credentials, full VINs or raw private telemetry into the QR code.

## Report sections

1. Vehicle identity / market evidence
2. Connection and transport security
3. Capability Discovery
4. DTC / alerts with source links
5. Battery / HV / Riso
6. Charging
7. Drive unit / thermal
8. ICE/hybrid sections when applicable
9. Bus health
10. Missing/unknown data
11. Confidence and evidence
12. Dataset hash

## Reproducibility

A report must be regeneratable from the same canonical dataset. Any later manual edit creates a new report revision and a new digest.
