# AI Decoder Selection & Evidence Protocol

Povinné čtení před implementací nebo úpravou jakéhokoli decoderu v tomto repozitáři. Doplňuje `docs/AI_APK_EXTRACTION_GUIDE.md` (jak extrahovat) a `docs/AI_EXTRACTION_DEPTH_BY_CAPABILITY.md` (do jaké hloubky) o poslední krok: jak z několika nalezených kandidátů vybrat ten, který skutečně sedí na to, co vozidlo doopravdy posílá.

**Vznik tohoto dokumentu:** Mitsubishi Outlander PHEV, `21 01`. Watchdog decoder pro variantu `Lz3/a` byl extrahovaný ze zdrojové appky naprosto správně — a přesto byl špatný, protože skutečná odpověď vozidla měla 55 bajtů, zatímco decoder vyžadoval `bytes[78..79]` (minimálně 80). Chyba nebyla ve extrakci. Chyba byla v tom, že se decoder nasadil bez ověření, že strukturálně vůbec může sedět na to, co přišlo z vozidla.

## ⚠️ Vztah k WRITE operacím

Tento protokol řeší READ/decode cestu (kategorie A/B v `AI_EXTRACTION_DEPTH_BY_CAPABILITY.md`). **Nenahrazuje**, jen doplňuje přísnější bezpečnostní hradlo pro WRITE operace (long coding, servisní resety, aktuátor testy — kategorie C/D, `DtcClearPolicy` a obdobné gate mechanismy). Špatně vybraný READ decoder zobrazí špatné číslo. Špatně vybraný WRITE příkaz může fyzicky poškodit modul. WRITE cesta vyžaduje navíc explicitní potvrzení a security-access gate, i když projde vším níže.

---

## 1. Účel

Repozitář obsahuje nezávisle extrahované implementace z více appek pro stejný diagnostický požadavek/PID/DID/signál. AI nesmí vybrat decoder jen proto, že vypadá věrohodně. Cíl je decoder, který je zároveň:

1. technicky správně extrahovaný,
2. podložený nejsilnější dostupnou evidencí,
3. strukturálně kompatibilní se skutečnou transportní/protokolovou odpovědí,
4. kompatibilní se správnou vozidlo/ECU variantou,
5. deterministický a reprodukovatelný,
6. bezpečně odmítnutelný, když evidence nesedí.

Decoder, co vyprodukuje rozumně vypadající číslo, **není** tím pádem validní.

## 2. Než se cokoliv implementuje

1. Přečti architekturu a evidence dokumentaci.
2. Najdi VŠECHNY extrahované appky relevantní k požadovanému signálu, ne jen první, co ho obsahuje.
3. Z každé vytáhni request, CAN ID, ISO-TP handling, byte offsety, délkové podmínky, scaling, jednotku, variant detection, error/timeout handling.
4. Postav srovnávací matici VŠECH kandidátů (sekce 8) **předtím**, než se napíše produkční kód.

## 3. Hierarchie evidence

| Úroveň | Co to je | Poznámka |
|---|---|---|
| **A — přímá exekuovatelná evidence** | dekompilovaný kód appky, skutečné request/response konstrukce, offsety, scaling | nejsilnější |
| **B — opakovaná nezávislá evidence** | stejný decoder nalezený nezávisle ve 2+ appkách | posiluje důvěru, **neprokazuje** aplikovatelnost na konkrétní vozidlo |
| **C — runtime evidence** | zachycené CAN rámce, WiCAN odpovědi, ISO-TP trace ze skutečného vozidla | musí se porovnat byte-za-byte s očekávaným vstupem decoderu |
| **D — dokumentace/komunita** | servisní manuály, komunitní capture | podpůrné, **nesmí tiše přebít** úroveň A |
| **E — AI inference** | odhad AI | musí být označeno `INFERRED`, nikdy prezentováno jako ověřené |

## 4. Čtyři vrstvy se nikdy nemíchají

```text
TRANSPORT → CAN/ISO-TP → DIAGNOSTIC RESPONSE → APPLICATION DECODER
```

Správně extrahovaný decoder je k ničemu, pokud transportní/ISO-TP vrstva dodá špatný nebo neúplný payload.

## 5. Request ≠ response layout

Že dvě appky posílají stejný `21 01`, **neprokazuje**, že používají stejný decoder odpovědi. Vždy porovnávej request CAN ID, response CAN ID, addressing mode, ISO-TP mód, očekávanou délku payloadu, byte offsety, byte order, scaling, jednotku, generaci vozidla a ECU variantu.

## 6. Délka payloadu je tvrdá podmínka

Pokud decoder čte `bytes[78]`/`bytes[79]`, **nesmí se spustit**, dokud `payload.size >= 80`. To samo o sobě nestačí — musí se ověřit i skutečná struktura payloadu, ne jen jeho délka.

```text
Očekáváno (Lz3/a): payload >= 80 B, pole = bytes[78..79]
Pozorováno (skutečné vozidlo): payload = 55 B

Výsledek: DECODER NENÍ APLIKOVATELNÝ
```

**Nikdy:** zkusit jiný offset, hádat pole, použít blízkou hodnotu, nebo reinterpretovat jiné pole jako hledaný signál.

## 7. Nikdy nezaměňuj příbuzná pole

`internal resistance` ≠ `HV isolation resistance` ≠ `max/min internal resistance` ≠ `resistance difference`. Pokud repozitář obsahuje víc z těchto polí, zůstávají oddělené signály, dokud evidence explicitně neprokáže, že jde o totéž.

## 8. Matice kandidátů (povinná před implementací)

| Candidate | App | Request | CAN | Payload | Offset | Type | Scale | Unit | Variant | Evidence |
|---|---|---|---|---|---|---|---|---|---|---|
| A | Watchdog | 21 01 | 761→762 | 80+ | 78–79 | UInt16 BE | 1 | kΩ | Lz3a | Direct |
| B | App 2 | 21 01 | 761→762 | 72+ | 71 | UInt8 | 0.02 | MΩ | Ld4a | Direct |
| C | App 3 | 21 01 | ? | 40+ | 38 | UInt8 | 0.1 | MΩ | Le4a | Direct |

AI nesmí vybrat kandidáta, dokud tahle tabulka neexistuje.

## 9. Porovnání se skutečnou odpovědí vozidla

Po získání reálné odpovědi zjisti: CAN response ID, ISO-TP typ rámce, deklarovanou ISO-TP délku, skutečnou poskládanou délku, diagnostické response bajty, a se kterým kandidátem strukturálně sedí. Výběr decoderu se řídí **skutečnou** odpovědí, ne tou, co appka teoreticky umí.

## 10. ISO-TP se rekonstruuje před dekódováním, vždy

```text
CAN rámce → ISO-TP parser → First/Consecutive Frames → kompletní payload
→ kontrola délky → kontrola diagnostické odpovědi → signal decoder
```

Decoder nikdy nedostane první CAN rámec, ELM řádek, nebo částečně poskládanou odpověď jako by to byl kompletní payload.

## 11. Deklarovaná délka se bere jako fakt

ISO-TP First Frame `10 LL ...` — `LL` je závazná deklarovaná délka. `declared length = 55` a kandidát vyžaduje `80` → `NO_VERIFIED_DECODER_MATCH`, ne pokus o dekódování zkrácené verze.

## 12. Selekce kandidáta — tvrdá veta, ne aritmetika

Nejdřív tvrdé veto podmínky — kterákoli platí → kandidát je **okamžitě vyřazený**, bez ohledu na jinak silnou evidenci:

- délka payloadu nesedí
- CAN addressing nesedí
- vozidlo/ECU varianta nesedí
- sémantický nesoulad pole (viz sekce 7)
- decoder čte bajty, co v payloadu neexistují

Teprve mezi kandidáty, co veto podmínky přežijí, se řadí kvalitativně podle síly evidence (sekce 3, A > B > C > D > E). Pokud po vyřazení zůstane víc než jeden kandidát se stejnou úrovní evidence a žádný jednoznačně nevede, výsledek je `UNKNOWN_VARIANT` — ne vynucená volba.

## 13. Variant selection

Pokud appky obsahují varianty (`Lz3a`, `Ld4a`, `Le4a`...), jejich decodery se **nikdy neslučují**. Každá zůstává samostatně reprezentovaná (request/addressing/offsety/typ/scaling/confidence zvlášť). Pokud detekce varianty není možná: `UNKNOWN_VARIANT` nebo `NO_VERIFIED_LAYOUT`, nikdy předstíraná jistota.

## 14. Testy — pozitivní A negativní, obojí povinné

**Pozitivní test (povinný):** validní, kompletní payload odpovídající přesně dané variantě → decoder MUSÍ vrátit `Success` s očekávanou hodnotou. Bez tohohle testu projde i decoder, co vždy vrací `null`/`Rejected`.

**Negativní testy (povinné, alespoň):**
- kratší payload, než varianta vyžaduje → `PAYLOAD_TOO_SHORT`
- špatné CAN ID → `WRONG_CAN_ID`
- špatná diagnostická odpověď → `WRONG_RESPONSE`
- zkrácená ISO-TP sekvence → `INVALID_ISOTP`
- neznámá varianta → `WRONG_VARIANT` / `UNKNOWN_LAYOUT`
- poškozený rámec → `INVALID_FIELD`

Decoder se nepovažuje za produkčně připravený bez obojího druhu testu.

## 15. Nikdy tichý fallback

Zakázáno: "když offset 78 nejde, zkus 38"; "když isolation není dostupná, ukaž internal resistance"; "když decoder selže, odhadni hodnotu". Vždy místo toho: `NO_VERIFIED_DECODER_MATCH` + zachování syrové evidence.

## 16. Syrová data zůstávají dostupná vždy

Když dekódování selže, appka uchová: request, CAN ID, syrové CAN/ISO-TP rámce, deklarovanou délku, kompletní payload, zvažované kandidáty, důvod zamítnutí. Příklad:

```text
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

Kód musí umět odpovědět "proč tenhle decoder existuje" bez nutnosti reverzovat vlastní historii commitů.

## 18. Decoder odděleně od transportu

```text
Transport → CanFrame → IsoTpSession → DiagnosticPayload → Decoder → DecodedSignal → UI
```

Nikdy nemíchat WiCAN/TCP/ELM327/ISO-TP/UDS/decoder/UI do jedné funkce — decoder musí jít testovat bez připojeného vozidla.

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

Decoder musí umět říct "tenhle payload neznám" jako plnohodnotný, očekávaný výsledek — ne výjimku, ne null bez důvodu.

## 20. AI musí zdokumentovat své rozhodnutí

Před commitnutím decoderu, v implementačních poznámkách:

```text
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

AI musí zastavit implementaci číselného decoderu, pokud: neexistuje přímá evidence; délka kandidátního payloadu nesedí; CAN addressing je neznámý nebo nekompatibilní; vozidlo/ECU varianta nesedí; víc kandidátů si protiřečí a nejde je rozlišit; sémantický význam pole je nejistý; skutečná runtime odpověď kandidátovi odporuje. Namísto dekódování: **RAW/DIAGNOSTIC EVIDENCE MODE**.

## 22. Zlatá otázka

Neptej se: *"Který decoder vypadá nejpravděpodobněji?"*

Ptej se: *"Který decoder má nejsilnější evidenci A je strukturálně kompatibilní se skutečnou odpovědí?"* a nakonec: *"Umím dokázat, že přesně tenhle payload dekóduje přesně tenhle decoder?"*

Pokud ne: **nedekóduj. Zachovej syrová data. Nahlaš nesoulad.**

---

## 23. CI Decoder Evidence Gate — mechanická pojistka, ne jen instrukce

Rozdíl mezi "AI si to má přečíst" a "build spadne, když se to poruší". Bez tohohle je celý dokument jen doporučení, co se dá pod tlakem obejít — přesně jak se stalo u `21 01`.

**Mechanika:** samostatný Gradle task/test, co běží v CI a:

1. Najde všechny třídy implementující `DiagnosticDecoder<T>` v `android/core/`.
2. Pro každou ověří, že existuje odpovídající test soubor (`<DecoderName>Test.kt`).
3. V testu vyžaduje oba typy pokrytí:
   - alespoň jeden validní, kompletní payload dané varianty a `DecodeResult.Success` s očekávanou hodnotou;
   - alespoň jeden payload kratší, než decoder vyžaduje, a `DecodeResult.Rejected` s `RejectReason.PAYLOAD_TOO_SHORT` (nebo odpovídajícím důvodem).
4. Pokud podmínka chybí, CI job selže s jasnou zprávou.

**Aktuální stav:** v tomto okamžiku repository neobsahuje `DiagnosticDecoder<T>` implementaci v `android/core/`; gate je proto připraven jako guard pro budoucí produkční decodery. Pokud nejsou žádné decodery, gate projde. Jakmile první decoder vznikne, povinnost testu se aktivuje.

Příklad implementace je uveden níže jako referenční návrh; konkrétní cesty musí odpovídat aktuální modulové struktuře.

```kotlin
tasks.register("decoderEvidenceGate") {
    group = "verification"
    description = "Fails if any DiagnosticDecoder<T> lacks a positive+negative test pair."
    doLast {
        val decoderSourceDir = file("src/main/java/com/autodiag/core")
        val testSourceDir = file("src/test/java/com/autodiag/core")

        if (!decoderSourceDir.exists()) return@doLast

        val decoders = decoderSourceDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { it.readText().contains(": DiagnosticDecoder<") }
            .map { it.nameWithoutExtension }
            .toList()

        val failures = mutableListOf<String>()
        for (decoderName in decoders) {
            val testFile = if (testSourceDir.exists()) {
                testSourceDir.walkTopDown().firstOrNull { it.name == "${decoderName}Test.kt" }
            } else null

            if (testFile == null) {
                failures += "$decoderName: chybí test soubor ${decoderName}Test.kt"
                continue
            }

            val testContent = testFile.readText()
            val hasPositive = testContent.contains("DecodeResult.Success")
            val hasNegative = testContent.contains("DecodeResult.Rejected") &&
                testContent.contains("RejectReason.")

            if (!hasPositive) failures += "$decoderName: chybí pozitivní test (DecodeResult.Success)"
            if (!hasNegative) failures += "$decoderName: chybí negativní test (DecodeResult.Rejected + RejectReason)"
        }

        if (failures.isNotEmpty()) {
            throw GradleException(
                "Decoder Evidence Gate selhal:\n" + failures.joinToString("\n") { "  - $it" }
            )
        }
    }
}

tasks.named("check") { dependsOn("decoderEvidenceGate") }
```

**Poznámka:** textové hledání je záměrně jednoduché a rychlé, ale není to plnohodnotná statická analýza. Proto je gate první mechanickou pojistkou, ne náhradou za skutečné unit testy. Budoucí rozšíření může přejít na AST/Kotlin symbol analysis, pokud bude potřeba odolnost proti obcházení.

## 24. Umístění a návaznosti

Tento dokument je v kořeni repozitáře jako `AI_DECODER_PROTOCOL.md`. Musí být odkazován z `AI_HANDOFF.md` a používán společně s `AI_CONTEXT.md`, `docs/AI_APK_EXTRACTION_GUIDE.md`, `docs/AI_EXTRACTION_DEPTH_BY_CAPABILITY.md` a `DATA_STORAGE_MAP.md`.

## 25. Finální pravidlo pro AI

Před tvrzením `decoder implemented` musí být dohledatelné:

```text
SOURCE + REQUEST + ADDRESSING + TRANSPORT + ISO-TP + PAYLOAD LENGTH
+ VARIANT + FIELD OFFSET + TYPE + SCALING + UNIT + VALIDATION
+ POSITIVE TEST + NEGATIVE TEST
```

Pokud chybí kritický prvek, stav je **UNVERIFIED** a AI nesmí vyrobit číselný výsledek.

**A decoder can be perfectly extracted and still be the wrong decoder for the vehicle currently connected.**
