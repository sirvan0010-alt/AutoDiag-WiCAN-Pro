# Implementační poznámky: EV/ICE PPI a automatizace

Tato revize převádí schválené požadavky do bezpečného základu v Kotlin core.

## Implementováno v této vrstvě

- granularita Capability Discovery: jednotlivé signály mohou být `SUPPORTED`, `UNSUPPORTED`, `UNKNOWN` nebo `PARTIAL`
- cache scope: VIN + WiCAN firmware + ECU software fingerprint
- tri-state evidence pro diagnostické výsledky
- český centrální katalog tooltipů
- auditovatelná automatizační pravidla jako data
- `NOTIFY` je odděleno od READ/ANALYZE
- deterministic dry-run evaluace pravidel
- replay index s binárním vyhledáváním timestampu
- source-linked DTC knowledge model
- základ PPI reportu pro EV/ICE

## Záměrně neimplementováno jako potvrzená funkce

- konkrétní Tesla/VAG CAN ID bez ověřeného zdroje
- automatické SGW/SFD obcházení
- neověřené CAN frame injection makra
- tvrzení, že konkrétní WiCAN firmware poskytuje CAN FD, MS-CAN, SW-CAN nebo K-Line
- detekce CAN stopperu pouze podle libovolně zvoleného prahu latence

Tyto oblasti zůstávají capability-/evidence-driven. Jakmile bude k dispozici ověřený HW/FW protokol a reprodukovatelný test, může být přidán decoder nebo transport adapter.

## PPI workflow

1. Capability Discovery
2. VIN / firmware / software scope
3. dostupnost jednotlivých metrik
4. DTC + freeze-frame, pokud je podporováno
5. EV: battery static/load/recovery/trend/confidence, cell/module replay, Riso podle zdroje
6. ICE: odometer cross-check, DPF/OPF, misfire, injector, boost/rail podle dostupných PID/UDS dat
7. vysvětlení každého nálezu v češtině
8. zdroj a ověření u každého servisního doporučení
9. export protokolu až po dokončení testu

## Automatizace

Pravidla jsou serializovatelná data a musí mít dry-run režim. WRITE/COMMAND není možné spustit automatizačním pravidlem. NOTIFY/MQTT má vlastní cooldown/rate-limit politiku.
