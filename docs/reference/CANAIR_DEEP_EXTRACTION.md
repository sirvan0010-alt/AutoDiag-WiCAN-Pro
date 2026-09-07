# canair deep extraction — transport/diagnostic core

Source: `philipkocanda/canair`
Commit/tree inspected: `92f742d9deaa69be77af1d16d08cf6a3436e1627`

## Verified source structure

The current canair tree contains dedicated transport components including:

- `canlib/transport/config.py`
- `canlib/transport/channel.py`
- `canlib/transport/isotp_params.py`
- `canlib/transport/isotp_stack.py`
- `canlib/transport/protocol.py`
- `canlib/transport/raw_terminal.py`
- `canlib/transport/uds_raw.py`
- `canlib/transport/elm327_session.py`
- `canlib/transport/elm327_terminal.py`
- `canlib/transport/elm327_tcp.py`
- `canlib/transport/elm327_pipe.py`
- `canlib/transport/fallback.py`
- `canlib/transport/elm327_frame_count.py`
- `canlib/uds_parse.py`
- `canlib/captures.py`
- `canlib/modes/monitor_raw.py`
- `canlib/modes/multi_batch.py`

## Highest-value concept: raw CAN → client-side ISO-TP → UDS

`uds_raw.py` explicitly models raw CAN transports as single-frame links where multi-frame UDS requires ISO-TP in the client. It creates an ISO-TP stack per ECU over a shared CAN bus/notifier and supports pipelined polling across different ECUs.

Important behaviour to reproduce independently in Kotlin:

1. one ISO-TP context per ECU/addressing configuration;
2. explicit 11-bit/29-bit addressing and flow-control address handling;
3. multi-frame reassembly and flow control outside the ELM terminal;
4. per-request deadlines;
5. UDS ResponsePending (`7F xx 78`) handling;
6. stale/late-response detection;
7. transport timing statistics;
8. incremental result delivery;
9. cancellation/interruption of an in-flight polling cycle;
10. transport choice hints based on measured link latency.

These are architectural requirements, not copied Python implementation.

## Critical reliability insight

The source explicitly tracks requests that were abandoned because of timeouts. A late response can otherwise be consumed by the next repeated request and look valid if it echoes the same PID/DID. The source therefore combines:

- an outstanding-response ledger;
- draining already queued ISO-TP messages before a request;
- UDS echo validation;
- explicit stale-response accounting;
- bounded debt so permanently lost responses do not poison the session forever.

This is highly relevant to AutoDiag's future adaptive polling engine because a repeated diagnostic request can otherwise produce a plausible but temporally wrong value.

## UDS pending handling

The source treats `7F <service> 78` as ResponsePending and continues waiting for the final response, with a per-follow-up timeout and an overall cap. AutoDiag should implement the same semantic distinction:

```text
positive response      → success
negative response      → diagnostic negative response
ResponsePending (78)   → still executing; continue waiting
no response            → transport/timeout failure
stale response         → evidence-quality failure, never a fresh value
```

## Pipelining boundary

The source does not blindly parallelize requests within one ECU. It pipelines **across ECUs**, while maintaining one outstanding ISO-TP exchange per ECU. This is the correct model for a shared CAN bus and should be reflected in an AutoDiag scheduler.

## Timing/provenance

Transport timing is treated as diagnostic context. A measured network round-trip allowance is added to ECU processing budgets for raw SLCAN, because ISO-TP flow-control traffic crosses the TCP link. This means a timeout is not purely an ECU property; it depends on transport mode and link conditions.

AutoDiag should preserve at least:

- request timestamp;
- first-response timestamp;
- completion timestamp;
- transport type;
- measured link latency if available;
- ECU logical identity/address scope;
- timeout budget;
- stale/drop/error counters.

## UDS parser boundary

`canlib/uds_parse.py` derives expected positive-response service IDs and validates echoed identifiers. AutoDiag should keep this parser independent from vehicle-specific signal decoding.

That separation gives:

```text
UDS protocol validity
        ≠
vehicle signal meaning
        ≠
vehicle verification
```

A syntactically valid `62 xx xx ...` response is not proof of what the bytes physically represent.

## Capture / investigation pipeline

`canlib/captures.py` and the monitor/decode/correlate/hunt/investigate command family establish a useful evidence workflow:

```text
capture → normalize → decode → correlate → hunt → investigate
```

For AutoDiag the equivalent should become a reusable core pipeline rather than a CLI-only feature:

```text
CaptureSession
  → FrameRecord
  → ProtocolMessage
  → DecoderCandidate
  → SignalObservation
  → CorrelationEvidence
  → VerificationState
```

The raw capture must remain immutable/replayable. Derived values must reference the originating capture/message and decoder provenance.

## Safety finding

The canair contribution guidance explicitly blocks UDS programming/write/upload operations. AutoDiag's existing WRITE_COMMAND default-off gate is therefore consistent with this reference and should remain stricter rather than weaker.

## Implementation order for AutoDiag

1. normalized `CanFrame` model;
2. SLCAN parser/encoder and TCP stream framing;
3. CAN channel abstraction;
4. ISO-TP configuration/addressing;
5. ISO-TP reassembly/transmit state machine;
6. UDS request/response model;
7. UDS negative/positive/pending parser;
8. stale-response ledger and timing statistics;
9. deterministic replay transport;
10. capture store;
11. diagnostic polling scheduler;
12. decoder/evidence bridge;
13. passive monitor/sniffer UI;
14. vehicle-specific decoders only after evidence gates.

## Do not implement yet

- ECU-specific addresses copied from canair;
- vehicle-specific DIDs as production capabilities;
- thresholds copied from profiles;
- write/programming sessions;
- Mazda NC flash functionality.

Those require their own provenance, scope and verification records.
