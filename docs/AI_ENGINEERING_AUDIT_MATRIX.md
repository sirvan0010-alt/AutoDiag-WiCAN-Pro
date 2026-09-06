# AI Engineering Audit Matrix — AutoDiag-WiCAN-Pro & AutoDiag-WiCAN-Diagnostic-Data

Nahrazuje ad-hoc audity (`AI_LEADERSHIP_REVIEW_2026-09-05.md` v1/v2) jako
systematický rámec. Ty dokumenty byly první spontánně nalezené problémy,
ne kompletní seznam oblastí. Tohle je ten kompletní seznam — a je to
**otevřený seznam**, ne uzavřený na 17 položek (viz sekce 17 a poznámka
na konci).

## Proč 17 oblastí, ne jen "pořádek v repu"

Projekt nestojí na tom, jestli je Kotlin build čistý. Stojí na celém
životním cyklu diagnostické informace:

```text
zdroj → důkaz → candidate → validace → runtime → UI → test → CI → release
```

Audit, který kontroluje jen strukturu repozitáře, tenhle cyklus nepokrývá.
Proto je matice rozdělená tak, aby každý článek toho řetězce měl vlastní
auditní oblast.

---

## 1. Repo struktura a mrtvý kód
`core/`, `src/` duplicity; nepoužívané moduly; orphaned resources; staré
experimenty.

## 2. Build systém
Gradle source sets; dependencies; pluginy; duplicity konfigurace; které
moduly se skutečně sestavují (ne jen existují na disku).

## 3. CI/CD
Auto-commity botů; workflow závislosti; pořadí build/test kroků; cache;
artefakty; **CI stav vždy svázaný s přesným commit SHA, nikdy "poslední běh"**.

## 4. Testovací infrastruktura
Unit testy; coroutine testy; decoder testy; provider testy; integration
testy; pokrytí kritických cest (ne jen počet testů celkem).

## 5. Diagnostic-data governance
Co smí být v lokálním `diagnostic-data/`; co patří do externího repa;
`reconstruction/`/`extraction/` — jejich vznik a oprávněnost; provenance;
zákaz tichého přenosu candidate → production bez gate.

## 6. Evidence / provenance
Odkud každý údaj pochází — APK/static evidence, capture evidence,
community evidence, inferred data — a kdo/co ho povýšilo na aktuální stav.

## 7. Candidate → verified pipeline
Explicitní stavy `UNKNOWN → CANDIDATE → CAPTURE-VALIDATED → VERIFIED`;
žádné implicitní povyšování; žádné hádané CAN ID/DID.

## 8. Decoder engine
Byte indexing; endianita; signed/unsigned; scaling; offset; bitová pole;
ISO-TP; multi-frame payloady; non-contiguous indexy.

## 9. Protocol / transport layer
CAN; ISO-TP; ELM/STN; WiCAN transport; init sekvence; timeouty; retry;
response matching.

## 10. Runtime diagnostická architektura
Request → response → decoder → measurement; error isolation (all-or-nothing
bugy přesně tady); polling; caching; lifecycle; concurrency.

## 11. Vehicle/ECU topology
Request ID; response ID; ECU; gateway; multiplexing; model/generation
applicability; oddělení skutečného důkazu od inference.

## 12. UI / canonical data contract
Canonical signal names; jednotky; precision; missing/error state; raw vs.
decoded value. **UI nesmí obsahovat vlastní skryté dekódování** — dekódovat
se smí jen v core vrstvě, UI jen zobrazuje hotovou hodnotu.

## 13. Capability coverage
Rozšiřuje `AI_EXTRACTION_DEPTH_BY_CAPABILITY.md` (aktuálně 16 kategorií:
live data, battery, cell data, motor, inverter, thermal, charging, SOC/SOH,
isolation resistance, DTC, VIN, ECU discovery, coding/adaptation, service
functions, passive CAN analysis, atd.). Při každé další APK/app analýze
musí vzniknout návrh nové kategorie, pokud funkce nikam nezapadá — viz
poznámka na konci dokumentu.

## 14. Safety / write-operation boundary
Read-only vs. write; coding; adaptation; actuator tests; security access;
**žádné produkční write operace jen na základě statické extrakce**.

## 15. Release / APK integrity
Package identity; versioning; signing; debug/release rozlišení; stale APK;
duplicate package; upgrade/install konflikty; reproducibilita buildu.

## 16. AI engineering governance
- `[OVĚŘENO]` / `[ODVOZENO]` / `[TVRZENÍ DRUHÉ AI, NEOVĚŘENO]` u každého zjištění
- žádné tvrzení o stavu CI bez commit SHA
- žádné mazání bez dependency checku
- žádné "AI si to nějak domyslí"
- WIP limit (nerozjíždět další frontu, dokud předchozí není zmergovaná/otestovaná/uklizená)
- branch discipline
- lokální compile před pushem
- žádné autonomní opravování repa CI botem bez revize

## 17. Audit auditu

**Každý audit musí být sám auditovatelný.** Zjištění bez důkazu a bez
navazující akce je jen dojem, ne audit. Formát:

| Zjištění | Stav | Důkaz | Akce |
|---|---|---|---|
| `core/` je mrtvé | `[NEOVĚŘENO]` | `settings.gradle.kts` | nejdřív dependency scan (grep CI/skripty/alt. build config) |
| bot commituje opravy | `[OVĚŘENO]` | workflow soubor + commit SHA | workflow odstranit/upravit |
| `21_04` decoder | `[OVĚŘENO]` | APK evidence + unit test | ponechat jako candidate |
| fyzické přiřazení buněk k modulům | `[NEOVĚŘENO]` | chybí vehicle capture | nepovyšovat |

Žádné zjištění nejde do akce (smazání, merge, force-push, promotion na
`VERIFIED`) bez řádku v téhle tabulce se sloupcem *Důkaz* vyplněným něčím
konkrétnějším než "druhá AI to řekla".

---

## Jak se s maticí pracuje

1. Při každém průchodu repem projeď oblasti 1–17 postupně, ne namátkově.
2. Každé zjištění zapiš rovnou ve formátu ze sekce 17 (Zjištění/Stav/Důkaz/Akce).
3. Nic se neaplikuje (smazání, merge, promotion) bez vyplněného Důkazu.
4. Matice se nerozšiřuje jen "když se hodí" — rozšiřuje se **vždy**, když
audit narazí na něco, co do žádné z existujících 17 oblastí nepatří.
Číslo 17 není strop, je to aktuální stav.

**Pokud tě při procházení nějaké appky napadne funkce, co nikam nezapadá,
klidně mi ji popiš a doplníme kategorii — lepší doplnit teď, než ať to zase
řeší "AI si to nějak vyřeší samo".**
