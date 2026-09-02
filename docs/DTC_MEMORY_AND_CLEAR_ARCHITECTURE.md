# DTC memory: read, history and clear

## Goal

AutoDiag-WiCAN-Pro must be able to work with diagnostic trouble codes stored in an ECU, not only with live sensor values. The intended workflow is:

1. identify the vehicle and ECU;
2. discover the ECU's supported diagnostic capability;
3. read stored DTC memory;
4. optionally read pending and permanent memories where supported;
5. preserve the raw response as diagnostic evidence;
6. show the code, ECU, memory type and verification status;
7. offer **Clear DTCs** only as an explicit state-changing operation;
8. after a successful clear, read DTC memory again to verify the result;
9. update local DTC history from the complete timestamped scan.

## OBD-II foundation

The core models represent:

- Mode `03` — stored/emission-related DTCs;
- Mode `07` — pending DTCs;
- Mode `0A` — permanent DTCs;
- Mode `04` — clear/reset diagnostic information.

The decoder accepts the positive response service IDs (`43`, `47`, `4A`) and converts standard two-byte OBD DTC encoding to `Pxxxx`, `Cxxxx`, `Bxxxx` or `Uxxxx` codes. Zero-filled two-byte slots are ignored.

The implementation is transport-neutral: it creates diagnostic operations, while the existing WiCAN/ELM/CAN/ISO-TP transport layer remains responsible for actually sending and receiving frames.

## ECU-native UDS memory

OBD Mode 03/04 is not sufficient for every ECU. The UDS foundation adds service `0x19 ReadDTCInformation` and `0x14 ClearDiagnosticInformation`, including status masks and ECU-native three-byte DTC identifiers. Snapshot and extended-data decoding remain vehicle/ECU-specific extensions.

UDS DTC identifiers are retained as `DTC` + six hexadecimal digits in the generic model. They must not be falsely converted to the four-character OBD P/C/B/U representation.

Do not translate a UDS ECU into a generic OBD operation unless the vehicle/ECU profile proves that mapping is valid.

## Local DTC history

The ECU reports its current diagnostic state; it does not provide a universal application-level timestamp for when AutoDiag first saw or last saw a DTC. AutoDiag therefore maintains a separate local history keyed by:

`(ECU address, DTC memory, DTC code)`

`DtcHistoryStore` tracks:

- `ACTIVE` vs `RESOLVED`;
- `firstSeenAt`;
- `lastSeenAt`;
- `resolvedAt`;
- `timesObserved`;
- `reoccurrenceCount` for ACTIVE → RESOLVED → ACTIVE returns;
- `ABSENT_ON_RESCAN` vs `CLEARED_BY_USER` resolution reason.

A resolved record is retained in history rather than removed. `STORED`, `PENDING` and `PERMANENT` memories are tracked independently, so clearing one memory does not implicitly resolve another.

**Ingestion safety:** `ingestScan()` must receive a complete scan for exactly one `(ECU, memory)` scope. A partial, filtered or failed scan must not be interpreted as absence, because that would create a false "resolved" event.

The core history store intentionally has no destructive `clearHistory()` operation. If storage retention/deletion is introduced later, it belongs to an explicit persistence/privacy policy layer.

## Clear safety

Clearing DTC memory is a **write/state-changing operation**. It can erase diagnostic evidence and may reset monitor/history information. The UI must therefore:

- require an exact vehicle + ECU scope;
- require explicit support evidence for the ECU/procedure;
- handle OEM security access when required;
- require deliberate user confirmation;
- show that stored diagnostic evidence may be lost;
- never silently clear as part of a scan;
- perform a post-clear read-back;
- call local history `recordExplicitClear()` only after the protocol layer confirms a successful clear;
- record request, response and result in diagnostic evidence/audit data.

A failed or unsupported clear operation must not be reported as successful.

## Long-term behavior

The feature remains part of the permanent architecture even when a specific vehicle/ECU cannot currently be cleared. The capability state should distinguish `AVAILABLE`, `AVAILABLE_WITH_PREREQUISITES`, `REQUIRES_OEM_SECURITY`, `NOT_SUPPORTED` and `UNKNOWN` rather than hiding the function.
