# OPEN QUESTIONS

## Car Scanner 2.1.50

1. **Mitsubishi isolation / `21 01`** — static symbol/string evidence found Mitsubishi CAN ECU and MUT-2 compatibility support, but no verified `21 01` request/response mapping yet. Need request-construction/IL evidence and preferably runtime capture.
2. **Exact CAN arbitration IDs / ECU addressing** — protocol decoders and manufacturer ECU classes are present, but this block did not reconstruct every request/response header from method bodies. Do not promote guessed IDs.
3. **ISO-TP exact implementation parameters** — manual flow-control and STN segmentation are clearly present, but PCI parsing, block size, STmin and timeout values still need code-level reconstruction.
4. **ISO 9141-2 / SAE J1850 implementation boundary** — both are explicitly named in profile/documentation evidence, but a uniquely named low-level decoder was not isolated in the first string pass.
5. **Embedded Mitsubishi database schema** — `CarScannerMaui.EmbeddedFiles.dtcdb.Mitsubishi.db` is present by managed-resource name; extraction of the actual SQLite/resource bytes and schema is still pending.
6. **Consult 3 wire format** — Consult 3 profile/session support is directly evidenced, but exact frame/request construction remains to be reconstructed.

All questions are non-blocking for continued extraction.
