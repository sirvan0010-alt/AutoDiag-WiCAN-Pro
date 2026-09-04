# AI implementation notes — Mitsubishi Outlander PHEV

Tento dokument je doplněk k `AI_HANDOFF.md` a je určený pro další AI i lidské vývojáře. Obsahuje atypická rozhodnutí, která nesmí být při refaktoringu zjednodušena nebo odstraněna.

## 1. Atypický diagnostický tok: zdroj → evidence → živé měření

Outlander PHEV není v první fázi implementován jako běžný OBD PID seznam.

Preferovaný tok je:

```text
WiCAN PRO
  → ELM327 TCP session
  → konkrétní read-only request
  → raw response
  → evidence / provenance
  → source-derived decoder
  → typed measurement
  → session history
  → live graph / replay
```

UI nesmí být zdrojem pravdy. UI pouze zobrazuje hodnoty, které prošly diagnostickou vrstvou.

## 2. `21 01` není běžný OBD PID

PHEV Watchdog používá pro bateriový datový blok příkaz `21 01`.

V projektu se s ním zachází jako se source-derived proprietárním lokálním datovým requestem. Nesmí se automaticky přejmenovat na standardní OBD PID.

Technicky může jít o KWP/ISO-14230-style ReadDataByLocalIdentifier, ale přesný Mitsubishi transport/protokol a ECU adresace musí být potvrzeny request/response zachycením.

Dlouhá odpověď je očekávatelná: diagnostická transportní vrstva musí zvládnout ISO-TP/multi-frame payload tam, kde jej skutečný adapter/ECU používá.

## 3. Tři odporové hodnoty jsou záměrně odlišné

Aplikace zachovává tři hodnoty, protože zdroj je skutečně rozlišuje:

1. `HV_ISOLATION_RESISTANCE` — zdrojově dekódovaný izolační odpor v kΩ.
2. `INTERNAL_RESISTANCE_MAX_UNVERIFIED` — zdroj jej označuje jako internal resistance, jednotka MΩ, ale fyzický význam není potvrzen.
3. `INTERNAL_RESISTANCE_MIN_UNVERIFIED` — stejná situace pro MIN.

### Kritická zásada

MAX/MIN v MΩ **NESMÍ být prezentovány jako potvrzený ESR/vnitřní odpor trakční baterie**.

Důvod: běžný sériový vnitřní odpor trakčního packu je řádově mΩ až stovky mΩ, nikoli MΩ. Rozdíl mezi těmito interpretacemi je obrovský. Proto se zdrojové označení zachovává kvůli reverse engineeringu, ale fyzický význam zůstává `UNVERIFIED`.

Pokud další AI narazí na tento údaj, nesmí jej „opravit“ na mΩ ani jej vyhodit. Správné chování je zachovat hodnotu + viditelně uvést `význam neověřen`.

## 4. Izolační odpor — přesnost terminologie

Zdrojový dekodér používá dva bajty jako unsigned 16-bit hodnotu a jednotku kΩ.

Rozsah kódování je tedy 0–65 535 kΩ (~0–65,5 MΩ).

Tento rozsah je pouze vlastnost kódování. Není to automaticky bezpečný provozní rozsah ani limit výrobce.

Vysvětlení pro uživatele může uvést, že jde o elektrickou izolaci HV systému vůči referenci vozidla/karoserie. Přesná fyzická topologie měření a Mitsubishi servisní limit však nejsou z tohoto zdroje potvrzené.

Nikdy neodvozovat výrobní limit z jednoho komunitního příspěvku nebo z rozsahu 16bitového pole.

## 5. Důležitá zvláštnost indexů bajtů

Při reverse engineeringu Watchdog parseru je nutné zachovat skutečnou reprezentaci jeho parseru.

Parser může v odpovědi zachovat také CAN header jako token délky 3. Dekodér proto pracuje s indexy své normalizované response reprezentace, nikoli automaticky s „byte 0 prvního ISO-TP payloadu“.

Proto dokumentace typu „bytes 78–79“ znamená především:

> zdrojový Watchdog dekodér používá indexy 78–79 ve své response reprezentaci.

Další AI nesmí tento index bez zachyceného raw request/response přemapovat na jinou CAN/ISO-TP strukturu.

## 6. Živý graf není jen UI dekorace

Historie měření je diagnostický artefakt.

Každý vzorek má zachovat minimálně:

- timestamp,
- hodnotu,
- measurement kind,
- verification state,
- pokud je dostupné také raw request/response a zdroj/evidence.

Graf musí být schopen zobrazit:

- aktuální hodnotu,
- časový průběh,
- session MIN/MAX,
- počet vzorků,
- stáří posledního validního vzorku,
- stav komunikace,
- verification state.

Lokální UI historie je pouze vizualizace. Dlouhodobě má být zdrojem pravdy VM/session time-series store, aby navigace/rekompozice neztratila měření.

## 7. Vzorkovací interval ≠ rychlost ECU

Volba 100/250/500/1000/2000/5000 ms určuje požadovanou hustotu ukládání/odběru v AutoDiag.

Nesmí být prezentována jako garance, že ECU odpovídá v tomto intervalu.

Skutečná rychlost je omezená minimálně:

```text
ECU response time
+ diagnostic transport
+ ISO-TP
+ WiCAN firmware
+ TCP latency/buffering
+ Android scheduling
```

Pro pozdější adaptive sampling platí:

- pomalé veličiny → nižší frekvence,
- dynamické napětí/proud → vyšší frekvence,
- detekovaný přechod stavu → dočasně hustší vzorkování,
- stabilní stav → frekvenci lze snížit.

Adaptive sampling nesmí vytvářet falešnou přesnost.

## 8. Read-only evidence gate

Nový Outlander request nesmí být považován za podporovaný jen proto, že jej najdeme v APK.

Minimální cesta:

```text
source candidate
  → concrete request
  → observed matching request
  → non-empty response
  → PARTIALLY_VERIFIED
  → decoder/value validation
  → VERIFIED
```

`PARTIALLY_VERIFIED` znamená, že request/response pár byl pozorován, ale význam/škálování ještě není plně nezávisle potvrzen.

Aktuální evidence gate záměrně nepřeskakuje rovnou do `VERIFIED`.

## 9. Co další AI nesmí udělat

- nevymýšlet CAN ID,
- nevymýšlet ECU adresu,
- nevymýšlet DID/PID,
- nezměnit `21 01` na standardní OBD PID bez důkazu,
- nepřepsat MΩ na mΩ jen kvůli fyzikální intuici,
- nevymazat MAX/MIN proto, že jejich význam není známý,
- nevytvořit z neověřeného čísla bezpečnostní limit,
- neoznačit decoder jako `VERIFIED` pouze proto, že unit test prošel,
- nezaměnit session MIN/MAX za výrobní limit,
- nezvyšovat polling jen kvůli tomu, aby UI graf vypadal plynuleji,
- neprovádět aktivní HV testovací rutiny během prvotního reverse engineeringu.

## 10. Jak hodnoty identifikovat při jízdě

Experimentální jízda může být použita jako evidence, nikoli jako automatický důkaz významu.

Při současném logování je vhodné korelovat:

```text
čas
├─ SOC
├─ pack voltage
├─ pack current
├─ power
├─ cell min/max/delta
├─ module temperature
├─ HV isolation
├─ source MAX resistance
├─ source MIN resistance
├─ charging state
├─ accelerator/load context
└─ raw diagnostic response
```

Pokud se neověřená hodnota systematicky mění při určitém stavu, je to kandidátní evidence pro další analýzu. Samotná korelace ještě není definitivní identifikace.

## 11. Priorita dvoudenního MVP

Pro první reálný Outlander build má přednost funkční read-only řetězec před rozšiřováním UI:

1. připojení WiCAN,
2. bezpečný ELM request path,
3. raw response,
4. source-derived decoder,
5. tři měřené položky se správným verification state,
6. session history,
7. živý graf,
8. export/replay raw evidence,
9. teprve potom rozšíření dalších battery bloků.

Pokud některá část není ověřitelná, označit ji `BLOCKED` nebo `UNVERIFIED` a pokračovat jinou částí. Funkci nemaž.

## 12. Source / provenance

Primárním zdrojem současných Outlander candidate decoderů je reverse engineering PHEV Watchdog APK uložený v diagnostic-data workflow. Zdrojové názvy a formule se zachovávají jako provenance; proprietární APK/binary se do AutoDiag nekopíruje.

Cílem je nezávislá implementace kompatibilního diagnostického chování s explicitním původem a verification state.

## 13. Implementovaná hranice aktivního `21 01` měření

Projekt nyní obsahuje explicitní `OutlanderPhev21LiveMeasurementRunner`.

Runner:

- je pouze **read-only**,
- spouští se explicitně,
- používá přesně request `21 01`,
- respektuje pouze předdefinované intervaly 100/250/500/1000/2000/5000 ms,
- čeká na dokončení předchozího ELM příkazu, takže nevytváří paralelní requesty,
- zachovává raw request a raw response,
- při `NO DATA`, timeoutu nebo jiné chybě nevyrábí nulovou hodnotu,
- při krátké/neplatné odpovědi vrací chybu místo falešného měření,
- nepřidává `ATSH`, ECU adresu ani CAN ID.

### Důležitá hranice

Runner **nepředstírá**, že zná správnou Mitsubishi ECU adresaci. Jeho úkolem je použít již nakonfigurovaný ELM/WiCAN diagnostický kanál a provést source-derived request. Konfigurace transportu/adresace zůstává samostatnou evidence vrstvou.

`OutlanderPhev21ResponseParser` navíc zachovává tříznakový CAN header jako jeden token, pokud jej ELM vrátí. Tím se zachovává kompatibilita s indexy analyzovaného Watchdog parseru. Parser proto není obecný „CAN payload decoder“.

Toto je záměrná atypická hranice architektury: **request path může být implementován dříve než definitivní fyzická identifikace ECU**, ale výsledek nesmí být povýšen na `VERIFIED`, dokud není pozorován a potvrzen na skutečném vozidle.
