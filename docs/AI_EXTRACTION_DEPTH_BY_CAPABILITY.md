# Extraction Depth by Capability Type (v2 — kompletní)

Doplněk k `docs/AI_APK_EXTRACTION_GUIDE.md`. Ten guide řeší JAK extrahovat.
Tenhle dokument řeší CO a JAK HLUBOKO, podle typu funkce v referenční appce.

Kategorie jsou seřazené podle rizika: **A = čisté čtení** (nejnižší riziko),
**B = appka si něco dopočítává sama** (jiná kategorie faktů), **C = trvalý
zápis do ECU**, **D = okamžitá akce na skutečné komponentě**, **E = appka to
možná umí, ale u nás to nikdy nejde do produkce**. Pořadí není náhodné — je
to i pořadí, ve kterém stoupá potřebná opatrnost.

---

## A. Čisté čtení (nejnižší riziko)

### A1. Živá hodnota (live data / PID)
Request → byte offset v `d[]` → vzorec (scale/offset/endianita/signed) →
jednotka → rozsah. Bez UI/grafů. **Hotovo, když:** unit test na syntetickou
hodnotu sedí. *(Tohle už zvládáte — vzor pro zbytek kategorie A.)*

### A2. DTC kódy + status bity
Service pro čtení (Mode 03/07/0A, nebo UDS `0x19` subfunkce) → parsing
raw kódu na `P/C/B/U####` → status byte (pending/confirmed/permanent/MIL)
zvlášť pro každou appku, neber převzatě odjinud.

### A3. Interpretace DTC (text k kódu)
**Samostatná kategorie od A2** — je to slovníková vrstva, ne protokol.
Ukládej text vždy s tagem zdroje appky, nikdy jako jednu "pravdu":
```json
{"code": "P1704", "interpretations": [
  {"source": "PHEV Watchdog 1.9.1", "text": "..."},
  {"source": "generic_sae", "text": "..."}
]}
```
`resolved_description` vznikne, jen když se shodnou dva nezávislé zdroje.

### A4. Freeze frame / snapshot data
Které PID appka uloží společně s DTC v okamžiku poruchy. Je to vazba mezi
A1 a A2, ne nová hodnota — extrahuj jako referenci na existující PID
definice + samotný request pro freeze frame (často jiný command než živá
hodnota stejného PID).

### A5. Readiness monitory / emisní připravenost
Bitové pole (Mode 01 PID 0x01 a příbuzné) — které bity znamenají který
monitor (catalyst, O2 sensor, EVAP...) a co znamená "ready" vs "not ready"
vs "not supported". Tři stavy na bit, ne dva — časté místo, kde appky chybují.

### A6. VIN a identifikace per řídící jednotka
Discovery smyčka (seznam adres, ne jeden request) → service+DID (typicky
UDS `0x22 0xF190`, ověř, appka může mít vlastní) → přesný byte offset ASCII
VIN v odpovědi včetně případného prefixu k odseknutí.

### A7. Konfigurace/výbava vozidla (option/PR kódy)
Appky typu VCDS/Carly umí přečíst "jaké má auto vybavení" z kódovaných DID
(ne z VIN dekodéru třetí strany, ale přímo z ECU). Extrahuj mapování
kód→funkce, protože tohle přímo řídí, jestli má smysl v appce nabízet
signál, který dané konkrétní auto vůbec nemá (capability gating).

### A8. Topologie sítě / gateway mapping
Které ECU appka umí najít, na jaké sběrnici/segmentu, přes jaký gateway
routing, jaké adresování (11-bit/29-bit, extended addressing). Tohle je "mapa",
na které stojí A6 i A7 — extrahuj ji jako samostatný artefakt, ne jen jako
vedlejší produkt jiné extrakce.

### A9. Řízení diagnostické session
Jak appka přepíná default→extended→programming session (UDS `0x10`), jaký
interval tester-present keep-alive (`0x3E`) používá, aby jí spojení nespadlo.
Tohle je "instalatérská" vrstva, kterou appka řeší potichu a většina extrakcí
ji přeskočí — bez ní ale ostatní kategorie nebudou spolehlivě fungovat v praxi,
jen v laboratorních podmínkách.

---

## B. Appka si něco dopočítává sama (jiná kategorie faktů)

### B1. Historie / trip statistiky / agregace
Spotřeba za jízdu, skóre jízdního stylu, grafy v čase — appka tohle počítá
sama z primárních dat, není to fakt o vozidle. Extrahuj vzorec/agregační
logiku jako "appka X to počítá takhle", ne jako ověřený fakt o autě.

### B2. Prahové hodnoty a upozornění
"Appka varuje při teplotě > 110 °C" — to je appkou zvolený práh, ne
specifikace výrobce. Označ explicitně jako "vendor threshold", ať se to
nesmíchá s A1 daty.

---

## C. Trvalý zápis konfigurace (WRITE — persistent)

### C1. Long coding / adaptace
Bitová mapa konfiguračního řetězce (který bit/nibble = která funkce),
security access mechanismus (`0x27` seed-key — appka ho zná, nebo je modul
pro ni zamčený?), a **validní rozsah hodnot, který appka dovoluje nastavit**
(ne teoretické maximum bitového pole).

### C2. Servisní/údržbové resety (state machine, ne jeden zápis)
Reset servisního intervalu, EPB servisní režim na výměnu destiček,
registrace nové baterie, regenerace DPF, adaptace škrticí klapky. Tohle není
jeden write — je to **sekvence kroků s podmínkami** (motor vypnutý,
zapalování zapnuté, brzda uvolněná...). Extrahuj celou sekvenci a
předpoklady, ne jen finální routine-control command, jinak to v reálu selže
i s "správným" příkazem.

### C3. Bidirekční test aktuátorů (okamžitá akce na komponentě)
Sepnutí ventilátoru, cyklování solenoidu, test vstřikovače — na rozdíl od
C1/C2 je to **okamžitá, ne trvalá** akce, ale hýbe to skutečnou součástkou.
Extrahuj přesný command + jak appka akci ukončí/timeoutuje (bezpečnostní
mechanismus appky samotné je cenná informace — kopíruj i tohle, ne jen
"jak se to zapne").

---

## D. Odposlech (metoda, ne cíl — použitelná na C1–C3)

### D1. Pasivní CAN capture korelovaná s akcí
Baseline bez akce → capture během akce → diff → ověření opakovatelnosti
(2–3×, stejný payload pokaždé, nebo se mění čítač/checksum). Výstup je vždy
jen hypotéza o příkazu (`CANDIDATE`, "observed not documented"), nikdy přímo
spustitelná akce.

---

## E. Appka to možná umí — u nás explicitně mimo scope

### E1. Flashování / přeprogramování ECU
I kdyby appka tohle uměla, neextrahujte to k implementaci. Riziko zacihlení
modulu je příliš vysoké na cokoliv jiného než čistě informativní poznámku
"appka X tohle umí, my ne".

### E2. Imobilizér, párování klíčů, component protection
Nejvyšší riziková kategorie ze všech — bezpečnostně (fyzicky) i z hlediska
zneužitelnosti (krádež vozidla). Extrahuj maximálně existenci funkce pro
přehled trhu, nikdy mechanismus.

---

## Souhrnná tabulka

| # | Kategorie | Tier | Hotovo, když... | Kam patří |
|---|---|---|---|---|
| A1 | Živá hodnota | Read | request→offset→vzorec→test | candidate |
| A2 | DTC kód+status | Read | kód i status zvlášť, appka-specifické | candidate |
| A3 | DTC interpretace | Read | text má `source`, resolved jen při shodě 2+ | knowledge base |
| A4 | Freeze frame | Read | vazba na existující PID definice | candidate (reference) |
| A5 | Readiness monitory | Read | 3 stavy na bit rozlišené | candidate |
| A6 | VIN per ECU | Read | seznam adres + formát z každé | candidate |
| A7 | Konfigurace/výbava | Read | mapování kód→funkce | candidate |
| A8 | Topologie sítě | Read | mapa adres/segmentů/gateway | candidate (infra) |
| A9 | Session management | Read | přepínání + keep-alive interval | candidate (infra) |
| B1 | Historie/statistiky | Derived | označeno jako appka-dopočet, ne fakt | knowledge base, tag "derived" |
| B2 | Prahy/alerty | Derived | označeno jako "vendor threshold" | knowledge base, tag "derived" |
| C1 | Long coding | **Write** | bitmapa+rozsah+security stav | candidate, gate před spuštěním |
| C2 | Servisní resety | **Write** | celá sekvence + předpoklady | candidate, gate před spuštěním |
| C3 | Aktuátor testy | **Write (okamžité)** | command + appčin timeout mechanismus | candidate, gate před spuštěním |
| D1 | Odposlech | **Write (hypotéza)** | diff + opakovatelnost 2-3× | candidate, "observed not documented" |
| E1 | Flashování | Mimo scope | — | jen poznámka do přehledu |
| E2 | Imobilizér/klíče | Mimo scope | — | jen poznámka do přehledu |

**První otázka AI u každé appky, kterou nahraješ, zůstává stejná:** "do
které z těchto ~16 kategorií tahle konkrétní funkce patří?" — teď má ale
mnohem menší šanci, že narazí na něco, co nikam nezapadá.
