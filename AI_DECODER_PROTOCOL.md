# AI Decoder Selection & Evidence Protocol

Povinné čtení před implementací nebo úpravou jakéhokoli decoderu v tomto repozitáři. Doplňuje `docs/AI_APK_EXTRACTION_GUIDE.md` a `docs/AI_EXTRACTION_DEPTH_BY_CAPABILITY.md` o poslední krok: výběr decoderu pouze tehdy, když jeho evidence a struktura odpovídají skutečné odpovědi vozidla.

## Základní pravidla

- Decoder, který pouze produkuje rozumné číslo, není automaticky validní.
- READ/decode governance nenahrazuje přísnější bezpečnostní gate pro WRITE operace.
- Vrstvy se nesmí míchat: `TRANSPORT → CAN/ISO-TP → DIAGNOSTIC RESPONSE → APPLICATION DECODER`.
- Request `≠` response layout.
- ISO-TP musí být kompletně rekonstruováno před dekódováním.
- Deklarovaná délka ISO-TP je tvrdá podmínka.
- Pokud decoder čte `bytes[78..79]`, musí platit `payload.size >= 80`.
- `internal resistance` ≠ `HV isolation resistance` ≠ `resistance difference`.
- Při strukturálním nebo sémantickém nesouladu je výsledek `NO_VERIFIED_DECODER_MATCH`, nikoli odhad.

## Hierarchie evidence

| Úroveň | Evidence |
|---|---|
| A | přímá exekuovatelná/dekompilovaná evidence, request/response, offsety, scaling |
| B | stejný decoder nezávisle nalezený ve více appek |
| C | runtime CAN/WiCAN/ISO-TP capture ze skutečného vozidla |
| D | dokumentace a komunitní výzkum |
| E | AI inference — vždy `INFERRED` |

B posiluje důvěru, ale samo o sobě neprokazuje aplikovatelnost na konkrétní vozidlo. D nesmí tiše přebít A. Runtime evidence musí být porovnána byte-za-byte s očekávaným vstupem decoderu.

## Povinný postup před implementací

1. Přečti architekturu a evidence dokumentaci.
2. Najdi VŠECHNY relevantní extrahované appky.
3. Z každé vytáhni request, CAN ID, addressing, ISO-TP handling, offsety, délku, typ, byte order, scaling, jednotku, variantu a chyby.
4. Vytvoř matici všech kandidátů.
5. Porovnej ji se skutečným CAN/ISO-TP capture.
6. Teprve potom implementuj vlastní decoder jako samostatnou implementaci varianty, nikoli jako slepou kopii zdroje.

### Kandidátní matice

| Candidate | App | Request | CAN | Payload | Offset | Type | Scale | Unit | Variant | Evidence |
|---|---|---|---|---|---|---|---|---|---|---|
| A | Watchdog | 21 01 | 761→762 | 80+ | 78–79 | UInt16 BE | 1 | kΩ | Lz3a | Direct |
| B | App 2 | 21 01 | 761→762 | 72+ | 71 | UInt8 | 0.02 | MΩ | Ld4a | Direct |
| C | App 3 | 21 01 | ? | 40+ | 38 | UInt8 | 0.1 | MΩ | Le4a | Direct |

Při jiné signalizaci musí být tabulka samozřejmě sestavena z reálných kandidátů daného signálu; příklad výše je pouze pracovní ilustrace známých kandidátů.

## Tvrdé veto

Kandidát je okamžitě vyřazen, pokud:
- nesedí délka payloadu;
- nesedí CAN addressing;
- nesedí vozidlo/ECU varianta;
- nesedí sémantika pole;
- decoder čte bajty, které payload neobsahuje.

Mezi přeživšími kandidáty se použije síla evidence A > B > C > D > E. Pokud není jednoznačný výsledek, použij `UNKNOWN_VARIANT` / `UNKNOWN_LAYOUT`.

## ISO-TP a diagnostika

```text
CAN rámce → ISO-TP parser → First/Consecutive Frames → kompletní payload
→ kontrola deklarované délky → diagnostická response → signal decoder
```

First Frame `10 LL ...` určuje závaznou deklarovanou délku. Decoder nikdy nesmí dostat první ELM řádek nebo částečný payload jako kompletní odpověď.

## Testy

Každý produkční decoder musí mít:

**Pozitivní test:** validní kompletní payload přesně odpovídající variantě → `Success` s očekávanou hodnotou.

**Negativní testy minimálně:**
- krátký payload → `PAYLOAD_TOO_SHORT`;
- špatné CAN ID → `WRONG_CAN_ID`;
- špatná diagnostická odpověď → `WRONG_RESPONSE`;
- zkrácená ISO-TP sekvence → `INVALID_ISOTP`;
- neznámá varianta → `WRONG_VARIANT` / `UNKNOWN_LAYOUT`;
- poškozené pole → `INVALID_FIELD`.

Zakázán je tichý fallback typu „zkus jiný offset“ nebo „ukaž příbuzné pole“.

## Syrová evidence

Při odmítnutí se zachová request, CAN ID, raw CAN/ISO-TP data, deklarovaná délka, kompletní payload, kandidáti a důvod odmítnutí. RAW/DIAGNOSTIC EVIDENCE MODE je preferovaný výsledek před odhadnutou hodnotou.

## Provenance

Decoder musí nést důvod své existence přímo v kódu, například zdroj, variantu, request, addressing, field, offset, délku, endian, scale, unit a evidence level. Decoder musí být testovatelný bez vozidla a oddělený od WiCAN/TCP/ELM327 transportu.

```text
Transport → CanFrame → IsoTpSession → DiagnosticPayload → Decoder → DecodedSignal → UI
```

## Požadované API

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

## Decoder Evidence Registry

Všechny kandidáty stejného konkrétního signálu se normalizují do perzistentního registry před vytvořením vlastního decoderu. Registry je mezivrstva mezi extrakcí a produkčním kódem a obsahuje kandidáty i skutečné vehicle captures.

Nové evidence se nepřepisují přes starší. Kandidát z jedné appky není automaticky pravda pro všechny varianty. `NO_VERIFIED_DECODER_MATCH` je legitimní výsledek.

Registry patří do externího repo `AutoDiag-WiCAN-Diagnostic-Data` pod `data/candidates/<signal_id>.json`. `diagnostic-data/` v tomto repozitáři je legacy/deprecated pro nová data.

## AI decision record

Před commitem decoderu musí být dohledatelné:

```text
TARGET: <signal>
CANDIDATES: <count>
SELECTED: <decoder or none>
WHY: <evidence>
VEHICLE/VARIANT: <variant>
REQUEST: <request>
ADDRESSING: <request → response>
EXPECTED PAYLOAD: <length>
FIELD: <offset/type>
SCALE: <scale>
UNIT: <unit>
REJECTED CANDIDATES: <candidate + reason>
REMAINING UNCERTAINTY: <items>
```

## Hard stop

Číselný decoder se nesmí implementovat, pokud chybí přímá evidence, nesedí délka, addressing nebo varianta, kandidáti si protiřečí bez možnosti rozlišení, význam pole je nejistý, nebo skutečná runtime odpověď kandidátovi odporuje. Místo toho RAW/DIAGNOSTIC EVIDENCE MODE.

## CI Evidence Gate

CI gate má kontrolovat přítomnost odpovídajícího testovacího artefaktu pro každý `DiagnosticDecoder`, alespoň jeden pozitivní `Success` test a alespoň jeden negativní rejection reason. Gate se připojí na `check`.

Gate je pouze mechanická kontrola testovacího/evidence artefaktu. **Zelené CI nikdy neznamená vehicle verification.** Existence testu neprokazuje CAN addressing ani původ payloadu ze skutečného vozidla.

Správné výsledky evidence jsou například `VERIFIED`, `PARTIALLY_VERIFIED`, `NO_VERIFIED_DECODER_MATCH` nebo `UNKNOWN_VARIANT`.
