# AI Decoder Selection & Evidence Protocol

Povinné čtení před implementací nebo úpravou jakéhokoli decoderu v tomto
repozitáři. Doplňuje `docs/AI_APK_EXTRACTION_GUIDE.md` (jak extrahovat) a
`docs/AI_EXTRACTION_DEPTH_BY_CAPABILITY.md` (do jaké hloubky) o poslední
krok: jak z několika nalezených kandidátů vybrat ten, který skutečně sedí
na to, co vozidlo doopravdy posílá.

**Vznik tohoto dokumentu:** Mitsubishi Outlander PHEV, `21 01`. Watchdog
decoder pro variantu `Lz3/a` byl extrahovaný ze zdrojové appky naprosto
správně — a přesto byl špatný, protože skutečná odpověď vozidla měla 55
bajtů, zatímco decoder vyžadoval `bytes[78..79]` (minimálně 80). Chyba
nebyla ve extrakci. Chyba byla v tom, že se decoder nasadil bez ověření,
že strukturálně vůbec může sedět na to, co přišlo z vozidla.

## ⚠️ Vztah k WRITE operacím

Tento protokol řeší READ/decode cestu (kategorie A/B v
`AI_EXTRACTION_DEPTH_BY_CAPABILITY.md`). **Nenahrazuje**, jen doplňuje
přísnější bezpečnostní hradlo pro WRITE operace (long coding, servisní
resety, aktuátor testy — kategorie C/D, `DtcClearPolicy` a obdobné gate
mechanismy). Špatně vybraný READ decoder zobrazí špatné číslo. Špatně
vybraný WRITE příkaz může fyzicky poškodit modul. WRITE cesta vyžaduje
navíc explicitní potvrzení a security-access gate, i když projde vším
níže.

---

## 1. Účel

Repozitář obsahuje nezávisle extrahované implementace z více appek pro
stejný diagnostický požadavek/PID/DID/signál. AI nesmí vybrat decoder jen
proto, že vypadá věrohodně. Cíl je decoder, který je zároveň:

1. technicky správně extrahovaný,
2. podložený nejsilnější dostupnou evidencí,
3. strukturálně kompatibilní se skutečnou transportní/protokolovou odpovědí,
4. kompatibilní se správnou vozidlo/ECU variantou,
5. deterministický a reprodukovatelný,
6. bezpečně odmítnutelný, když evidence nesedí.

Decoder, co vyprodukuje rozumně vypadající číslo, **není** tím pádem
validní.

## 2. Než se cokoliv implementuje

1. Přečti architekturu a evidence dokumentaci repozitáře.
2. Najdi VŠECHNY extrahované appky relevantní k požadovanému signálu, ne
   jen tu první, co ho obsahuje.
3. Z každé vytáhni: request, CAN ID, ISO-TP handling, byte offsety, délkové
   podmínky, scaling, jednotku, variant detection, error/timeout handling.
4. Postav srovnávací matici VŠECH kandidátů (sekce 7) **předtím**, než se
   napíše produkční kód.

## 3. Hierarchie evidence

| Úroveň | Co to je | Poznámka |
|---|---|---|
| **A — přímá exekuovatelná evidence** | dekompilovaný kód appky, skutečné request/response konstrukce, offsety, scaling | nejsilnější |
| **B — opakovaná nezávislá evidence** | stejný decoder nalezený nezávisle ve 2+ appkách | posiluje důvěru, **neprokazuje** aplikovatelnost na konkrétní vozidlo |
| **C — runtime evidence** | zachycené CAN rámce, WiCAN odpovědi, ISO-TP trace ze skutečného vozidla | musí se porovnat byte-za-byte s očekávaným vstupem decoderu |
| **D — dokumentace/komunita** | servisní manuály, komunitní capture | podpůrné, **nesmí tiše přebít** úroveň A |
| **E — AI inference** | odhad AI | musí být označeno `INFERRED`, nikdy prezentováno jako ověřené |

## 4. Čtyři vrstvy se nikdy nemíchají

```
TRANSPORT → CAN/ISO-TP → DIAGNOSTIC RESPONSE → APPLICATION DECODER
```

Správně extrahovaný decoder je k ničemu, pokud transportní/ISO-TP vrstva
dodá špatný nebo neúplný payload.

## 5. Request ≠ response layout

Že dvě appky posílají stejný `21 01`, **neprokazuje**, že používají stejný
decoder odpovědi. Vždy porovnávej: request CAN ID, response CAN ID,
addressing mode, ISO-TP mód, očekávanou délku payloadu, byte offsety,
byte order, scaling, jednotku, generaci vozidla, ECU variantu.

## 6. Délka payloadu je tvrdá podmínka

Pokud decoder čte `bytes[78]`/`bytes[79]`, **nesmí se spustit**, dokud
`payload.size >= 80`. To samo o sobě nestačí — musí se ověřit i skutečná
struktura payloadu, ne jen jeho délka.

```
Očekáváno (Lz3/a): payload >= 80 B, pole = bytes[78..79]
Pozorováno (skutečné vozidlo): payload = 55 B

Výsledek: DECODER NENÍ APLIKOVATELNÝ
```

**Nikdy:** zkusit jiný offset, hádat pole, použít blízkou hodnotu, nebo
reinterpretovat jiné pole jako hledaný signál.

## 7. Nikdy nezaměňuj příbuzná pole

`internal resistance` ≠ `HV isolation resistance` ≠ `max/min internal
resistance` ≠ `resistance difference`. Pokud repozitář obsahuje víc z
těchto polí, zůstávají oddělené signály, dokud evidence explicitně
neprokáže, že jde o totéž.

## 8. Matice kandidátů (povinná před implementací)

| Candidate | App | Request | CAN | Payload | Offset | Type | Scale | Unit | Variant | Evidence |
|---|---|---|---|---|---|---|---|---|---|---|
| A | Watchdog | 21 01 | 761→762 | 80+ | 78–79 | UInt16 BE | 1 | kΩ | Lz3a | Direct |
| B | App 2 | 21 01 | 761→762 | 72+ | 71 | UInt8 | 0.02 | MΩ | Ld4a | Direct |
| C | App 3 | 21 01 | ? | 40+ | 38 | UInt8 | 0.1 | MΩ | Le4a | Direct |

AI nesmí vybrat kandidáta, dokud tahle tabulka neexistuje.

## 9. Porovnání se skutečnou odpovědí vozidla

Po získání reálné odpovědi zjisti: CAN response ID, ISO-TP typ rámce,
deklarovanou ISO-TP délku, skutečnou poskládanou délku, diagnostické
response bajty, a se kterým kandidátem strukturálně sedí. Výběr decoderu
se řídí **skutečnou** odpovědí, ne tou, co appka teoreticky umí.

## 10. ISO-TP se rekonstruuje před dekódováním, vždy

```
CAN rámce → ISO-TP parser → First/Consecutive Frames → kompletní payload
→ kontrola délky → kontrola diagnostické odpovědi → signal decoder
```

Decoder nikdy nedostane první CAN rámec, ELM řádek, nebo částečně
poskládanou odpověď jako by to byl kompletní payload.

## 11. Deklarovaná délka se bere jako fakt

ISO-TP First Frame `10 LL ...` — `LL` je závazná deklarovaná délka.
`declared length = 55` a kandidát vyžaduje `80` → `NO_VERIFIED_DECODER_MATCH`,
ne pokus o dekódování zkrácené verze.

## 12. Selekce kandidáta — tvrdá veta, ne aritmetika

*(Zpřesnění oproti návrhu s bodovým skóre — číselné sčítání svádí k
falešné přesnosti, kterou i původní návrh sám zpochybňoval.)*

Nejdřív tvrdé veto podmínky — kterákoli platí → kandidát je **okamžitě
vyřazený**, bez ohledu na jinak silnou evidenci:

- délka payloadu nesedí
- CAN addressing nesedí
- vozidlo/ECU varianta nesedí
- sémantický nesoulad pole (viz sekce 7)
- decoder čte bajty, co v payloadu neexistují

Teprve mezi kandidáty, co veto podmínky přežijí, se řadí kvalitativně
podle síly evidence (sekce 3, A > B > C > D > E). Pokud po vyřazení
zůstane víc než jeden kandidát se stejnou úrovní evidence a žádný
jednoznačně nevede, výsledek je `UNKNOWN_VARIANT` — ne vynucená volba.

## 13. Variant selection

Pokud appky obsahují varianty (`Lz3a`, `Ld4a`, `Le4a`...), jejich decodery
se **nikdy neslučují**. Každá zůstává samostatně reprezentovaná
(request/addressing/offsety/typ/scaling/confidence zvlášť). Pokud detekce
varianty není možná: `UNKNOWN_VARIANT` nebo `NO_VERIFIED_LAYOUT`, nikdy
předstíraná jistota.

## 14. Testy — pozitivní A negativní, obojí povinné

*(Zpřesnění: původní návrh měl pozitivní příklad schovaný pod nadpisem
"negative tests", což přesně náš bug s `IsoTpDecoder.accept()` — funkce,
co nikdy nevrátí hodnotu ani při validním vstupu — by nezachytilo.)*

**Pozitivní test (povinný):** validní, kompletní payload odpovídající
přesně dané variantě → decoder MUSÍ vrátit `Success` s očekávanou hodnotou.
Bez tohohle testu projde i decoder, co vždy vrací `null`/`Rejected`.

**Negativní testy (povinné, alespoň):**
- kratší payload, než varianta vyžaduje → `PAYLOAD_TOO_SHORT`
- špatné CAN ID → `WRONG_CAN_ID`
- špatná diagnostická odpověď → `WRONG_RESPONSE`
- zkrácená ISO-TP sekvence → `INVALID_ISOTP`
- neznámá varianta → `WRONG_VARIANT` / `UNKNOWN_LAYOUT`
- poškozený rámec → `INVALID_FIELD`

Decoder se nepovažuje za produkčně připravený bez obojího druhu testu.

## 15. Nikdy tichý fallback

Zakázáno: "když offset 78 nejde, zkus 38"; "když isolation není dostupná,
ukaž internal resistance"; "když decoder selže, odhadni hodnotu". Vždy
místo toho: `NO_VERIFIED_DECODER_MATCH` + zachování syrové evidence.

## 16. Syrová data zůstávají dostupná vždy

Když dekódování selže, appka uchová: request, CAN ID, syrové CAN/ISO-TP
rámce, deklarovanou délku, kompletní payload, zvažované kandidáty, důvod
zamítnutí. Příklad:

```
21 01
response: 0x762
ISO-TP payload: 55 bytes
candidate: Lz3a
required: >=80 bytes
result: REJECTED
reason: PAYLOAD_TOO_SHORT
```

Tohle je cennější než zobrazené odhadnuté číslo.

## 17. Provenance přímo v kódu

```kotlin
DecoderEvidence(
    source = "phev-watchdog",
    variant = "watchdog.lz3a.21_01",
    request = "21 01",
    requestCanId = 0x761,
    responseCanId = 0x762,
    field = "battery.isolation_resistance",
    offset = 78,
    length = 2,
    endian = Endian.BIG,
    scale = 1.0,
    unit = "kΩ",
    evidenceLevel = EvidenceLevel.DIRECT_EXTRACTION
)
```

Kód musí umět odpovědět "proč tenhle decoder existuje" bez nutnosti
reverzovat vlastní historii commitů.

## 18. Decoder odděleně od transportu

```
Transport → CanFrame → IsoTpSession → DiagnosticPayload → Decoder → DecodedSignal → UI
```

Nikdy nemíchat WiCAN/TCP/ELM327/ISO-TP/UDS/decoder/UI do jedné funkce —
decoder musí jít testovat bez připojeného vozidla.

## 19. Požadované decoder API

```kotlin
interface DiagnosticDecoder<T> {
    fun canDecode(context: DecoderContext, payload: ByteArray): DecoderMatch
    fun decode(context: DecoderContext, payload: ByteArray): DecodeResult<T>
}

sealed interface DecodeResult<out T> {
    data class Success<T>(val value: T, val evidence: DecoderEvidence) : DecodeResult<T>
    data class Rejected(val reason: RejectReason) : DecodeResult<Nothing>
}

enum class RejectReason {
    WRONG_CAN_ID, WRONG_SERVICE, WRONG_RESPONSE, WRONG_VARIANT,
    PAYLOAD_TOO_SHORT, PAYLOAD_TOO_LONG, INVALID_ISOTP, INVALID_LENGTH,
    INVALID_FIELD, SEMANTIC_MISMATCH, INSUFFICIENT_EVIDENCE, UNKNOWN_LAYOUT
}
```

Decoder musí umět říct "tenhle payload neznám" jako plnohodnotný,
očekávaný výsledek — ne výjimku, ne null bez důvodu.

## 20. AI musí zdokumentovat své rozhodnutí

Před commitnutím decoderu, v implementačních poznámkách:

```
TARGET: <signál>
CANDIDATES: <počet>
SELECTED: <decoder>
WHY: <evidence>
VEHICLE/VARIANT: <varianta>
REQUEST: <request>
ADDRESSING: <request → response>
EXPECTED PAYLOAD: <délka>
FIELD: <offset/typ>
SCALE: <scale>
UNIT: <jednotka>
REJECTED CANDIDATES: <seznam + důvod>
REMAINING UNCERTAINTY: <seznam>
```

## 21. Hard stop podmínky

AI musí zastavit implementaci číselného decoderu, pokud: neexistuje přímá
evidence; délka kandidátního payloadu nesedí; CAN addressing je neznámý
nebo nekompatibilní; vozidlo/ECU varianta nesedí; víc kandidátů si
protiřečí a nejde je rozlišit; sémantický význam pole je nejistý; skutečná
runtime odpověď kandidátovi odporuje. Namísto dekódování: **RAW/DIAGNOSTIC
EVIDENCE MODE**.

## 22. Zlatá otázka

Neptej se: *"Který decoder vypadá nejpravděpodobněji?"*

Ptej se: *"Který decoder má nejsilnější evidenci A je strukturálně
kompatibilní se skutečnou odpovědí?"* a nakonec: *"Umím dokázat, že přesně
tenhle payload dekóduje přesně tenhle decoder?"*

Pokud ne: **nedekóduj. Zachovej syrová data. Nahlaš nesoulad.**

---

## 22b. Decoder Evidence Registry — sjednocené kandidáty PŘED vlastním decoderem

**Nevytváříme vlastní decoder tím, že zkopírujeme jeden zdroj (Watchdog).**
Vlastní decoder je výsledek analýzy VŠECH nalezených zdrojů pro daný signál,
ne náhrada za první z nich. Pokud existují 4 extrahované appky, dávají nám
4 nezávislé kandidáty a důkazy — ne jednoho "vítěze" k okopírování.

```
4 extrahované appky
        ↓
všechny nalezené implementace pro daný signál
        ↓
normalizace kandidátů (request/CAN/ISO-TP/offset/typ/scale/unit/variant)
        ↓
Decoder Evidence Registry (perzistentní, verzovaný artefakt — ne jen
                            analytický krok v hlavě AI)
        ↓
porovnání se skutečným CAN/ISO-TP capture z vozidla
        ↓
vyřazení nekompatibilních kandidátů (sekce 12 — tvrdá veta)
        ↓
NÁŠ decoder — samostatná implementace na variantu, ne kopie zdroje
```

Registry je autoritativní mezivrstva mezi extrakcí a produkčním decoderem.
Jeden soubor registru reprezentuje jeden konkrétní signál a obsahuje
všechny známé kandidáty i skutečné vehicle captures, které je mohou
potvrdit nebo vyvrátit.

Důležitá pravidla:
- nové kandidáty se nepřepisují přes starší evidenci;
- kandidát z jedné appky se nepovažuje automaticky za pravdu pro všechny
  varianty vozidla;
- runtime capture je veden jako samostatná evidence a musí být dohledatelný;
- `NO_VERIFIED_DECODER_MATCH` je legitimní výsledek;
- registry patří do externího repo `AutoDiag-WiCAN-Diagnostic-Data` pod
  `data/candidates/<signal_id>.json`;
- `diagnostic-data/` v tomto repozitáři je legacy/deprecated a pro nová data
  se nepoužívá.

---

## 23. CI Evidence Gate

Cílem gate je zabránit tomu, aby nový produkční decoder vznikl bez
odpovídajícího testu a bez explicitní evidence.

Do `android/core/build.gradle.kts` lze přidat task `decoderEvidenceGate`,
který:

1. projde `src/main` a najde implementace `DiagnosticDecoder<...>`;
2. pro každý decoder vyžaduje odpovídající `<DecoderName>Test.kt` v test
   source setu;
3. ověří přítomnost alespoň jednoho pozitivního `Success` testu;
4. ověří přítomnost alespoň jednoho negativního testu a důvodu odmítnutí;
5. při nesplnění skončí chybou s názvem decoderu a konkrétním chybějícím
   artefaktem;
6. task se připojí na `check`, takže běžný CI build gate automaticky
   zahrne.

Gate je kontrola přítomnosti testovacího/evidence artefaktu, nikoli náhrada
za skutečné vehicle verification. Passing CI neznamená, že decoder byl
ověřen na konkrétním vozidle.

Doporučený minimální tvar tasku:

```kotlin
tasks.register("decoderEvidenceGate") {
    group = "verification"
    description = "Ensures every DiagnosticDecoder has positive and negative evidence tests."

    doLast {
        val mainRoot = file("src/main")
        val testRoot = file("src/test")
        val decoderFiles = mainRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { it.readText().contains("DiagnosticDecoder<") }
            .toList()

        val failures = mutableListOf<String>()
        decoderFiles.forEach { decoderFile ->
            val source = decoderFile.readText()
            val className = Regex("class\\s+(\\w+)\\s*[:(]")
                .find(source)?.groupValues?.getOrNull(1)
                ?: decoderFile.nameWithoutExtension
            val test = testRoot.walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .firstOrNull { it.nameWithoutExtension == "${className}Test" }

            if (test == null) {
                failures += "$className: missing ${className}Test.kt"
                return@forEach
            }

            val testSource = test.readText()
            if (!testSource.contains("Success")) {
                failures += "$className: missing positive Success test"
            }
            val hasNegative = listOf(
                "PAYLOAD_TOO_SHORT", "WRONG_CAN_ID", "WRONG_RESPONSE",
                "INVALID_ISOTP", "WRONG_VARIANT", "UNKNOWN_LAYOUT",
                "INVALID_FIELD"
            ).any(testSource::contains)
            if (!hasNegative) {
                failures += "$className: missing negative rejection test"
            }
        }

        if (failures.isNotEmpty()) {
            throw GradleException(
                "Decoder evidence gate failed:\n" + failures.joinToString("\n")
            )
        }
    }
}

tasks.named("check") {
    dependsOn("decoderEvidenceGate")
}
```

Poznámka: tento minimální gate je záměrně jednoduchý a má být dále
zpřesňován podle skutečného build systému. Nesmí být interpretován jako
plná sémantická kontrola evidence.

## 24. Co CI gate NESMÍ tvrdit

- zelené CI ≠ vehicle verified;
- existence testu ≠ důkaz správnosti CAN addressing;
- existence `Success` testu ≠ důkaz, že testovaný payload pochází ze
  skutečného vozidla;
- registry bez runtime capture ≠ potvrzená aplikovatelnost na konkrétní
  vozidlo.

Jediný správný závěr je vždy odvozený z evidence: `VERIFIED`,
`PARTIALLY_VERIFIED`, `NO_VERIFIED_DECODER_MATCH` nebo `UNKNOWN_VARIANT`.
