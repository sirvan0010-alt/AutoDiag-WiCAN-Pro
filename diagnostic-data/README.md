# AutoDiag-WiCAN diagnostic-data directory

## IMPORTANT: not the production data repository

This directory is **legacy seed/staging/compatibility material only**. It is **not** a second source of truth for diagnostic candidates, decoder definitions or APK provenance.

The sole production source of truth is:

`AutoDiag-WiCAN-Diagnostic-Data`

Canonical production locations:

```text
AutoDiag-WiCAN-Diagnostic-Data/manifest.json
AutoDiag-WiCAN-Diagnostic-Data/data/candidates/*
AutoDiag-WiCAN-Diagnostic-Data/provenance/*
```

`GitHubDiagnosticDataProvider` reads the external repository directly. New candidate or provenance data must never be added to this local directory.

The existing JSON files here are retained only as legacy seed material during migration. They must not be edited to represent newer production state. If a future migration removes these legacy files, it must also update any tests or tooling that explicitly depend on them.

When documenting a path from the external repository, always name the repository explicitly; do not write `provenance/...` as though it were local.
