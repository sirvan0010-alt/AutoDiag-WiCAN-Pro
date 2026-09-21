# Outlander 2101 Monitor

Read-only live monitor for the Mitsubishi Outlander PHEV `21 01` diagnostic response over WiCAN ELM327.

Transport: CAN 11-bit, 500 kbit/s, request header `0x761`, response header `0x762`.

The app reassembles ISO-TP before applying the evidence-gated PHEV Watchdog decoder. No write/coding/security operations are implemented.
