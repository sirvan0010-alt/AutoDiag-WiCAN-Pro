# Data Storage Map — co a kam ukládat

Jediné definitivní místo pro otázku „kam tenhle soubor/záznam patří?“. Pokud jiný dokument nebo README říká něco jiného, opravuje se tento dokument nebo jeho rozporující dokument; pravidla se neobcházejí.

## Značení ověření

- `[OVĚŘENO]` — přímo přečteno v dostupném repozitáři v této session.
- `[NEOVĚŘENO — sestaveno z konverzace]` — informace o struktuře, kterou zde nemáme přímo přečtenou.
- Candidate evidence se nikdy automaticky nestává produkčním datem.

## Dva repozitáře, dva druhy obsahu

```text
AutoDiag-WiCAN-Pro                 AutoDiag-WiCAN-Diagnostic-Data
veřejná implementace + docs        privátní evidence / staging
schválené runtime balíčky          provenance + candidate data
```

### `AutoDiag-WiCAN-Pro` — veřejný repozitář

| Cesta | Účel | Nová data? |
|---|---|---|
| `android/core/`, `android/app/`, `android/simulator/` | `[OVĚŘENO]` implementační Kotlin/Android moduly | ano, kód |
| `android/.../diagnostics/` | `[OVĚŘENO]` offline diagnostické balíčky bundlované do APK | jen schválené balíčky |
| `diagnostic-data/` | `[OVĚŘENO]` legacy staging; respektuj jeho vlastní README | ne pro nové candidate evidence |
| `docs/` | `[OVĚŘENO]` architektura, governance a stavové dokumenty | ano, dokumentace |
| `tools/` | `[OVĚŘENO]` nástroje a validační skripty | ano, kód nástrojů |
| `core/`, `src/` | `[OVĚŘENO]` staré duplicitní stromy mimo aktuální Android build | ne |

### `AutoDiag-WiCAN-Diagnostic-Data` — privátní repozitář

Repo je nyní `[OVĚŘENO]` dostupné přes autorizované GitHub připojení. Konkrétní Tesla signal matrix je skutečně uložena v `provenance/apk-extraction/tesla/tesla-model3y-can-signal-source-matrix-2026-09-06.json`. Ostatní níže uvedené lokace mají být před změnou struktury znovu ověřeny přímo v repu.

| Cesta | Účel | Nová data? |
|---|---|---|
| `incoming/` | `[NEOVĚŘENO — sestaveno z konverzace]` syrový vstupní materiál | ano, syrové zdroje |
| `local-inventory/` | `[NEOVĚŘENO — sestaveno z konverzace]` inventář/manifest zdrojů | podle skutečného workflow |
| `schema/` | `[NEOVĚŘENO — sestaveno z konverzace]` schémata datasetu | pouze při změně schématu |
| `provenance/apk-extraction/<app>/` | `[OVĚŘENO pro Tesla matrix]` provenance a evidence APK extrakce | ano, extrakční evidence |
| `data/candidates/` | `[NEOVĚŘENO — sestaveno z konverzace]` candidate signály/DTC čekající na ověření | ano, candidate data |
| `data/` mimo `candidates/` | `[NEOVĚŘENO — sestaveno z konverzace]` schválená normalizovaná data | pouze po promotion gate |
| `manifest.json` | `[NEOVĚŘENO — sestaveno z konverzace]` souhrnný manifest | aktualizovat pouze podle skutečného datasetu |

## Tesla: kam patří současná CAN signal matrix

Tesla Model 3/Y CAN signal-source matrix patří do privátního repozitáře do:

`provenance/apk-extraction/tesla/tesla-model3y-can-signal-source-matrix-2026-09-06.json`

Její stav je `CANDIDATE_ONLY`; obsahuje transportní větev CAN/DoIP/BLE a kandidátní signály SOC, HV_V, HV_A, BATT_TEMP, charging a drive. SOH zůstává bez CAN mapování. Soubor výslovně říká `NO_PROMOTION` a nemění produkční počty.

Veřejné `docs/` proto obsahuje pouze orientační stav a pravidla; samotné neověřené CAN mapování se do veřejné runtime vrstvy nekopíruje.

## Rozhodovací strom pro nový artefakt

```text
Mám nový soubor/záznam. Kam patří?

├─ Je to Kotlin/Android kód?
│    └─ android/core/ nebo android/app/ nebo android/simulator/
│
├─ Je to architektura/governance/dokumentace?
│    └─ docs/
│
├─ Je to syrový, needitovaný vstupní materiál?
│    └─ Diagnostic-Data/incoming/                 [ověřit strukturu]
│
├─ Je to evidence z APK extrakce?
│    └─ Diagnostic-Data/provenance/apk-extraction/<app>/
│
├─ Je to candidate signál/DTC čekající na vehicle capture/replay?
│    └─ Diagnostic-Data/data/candidates/           [ověřit strukturu]
│
├─ Je to schválené normalizované runtime data?
│    └─ Diagnostic-Data/data/                     [ověřit strukturu]
│
├─ Je to offline balíček bundlovaný do APK?
│    └─ android/.../diagnostics/                   [po schválení]
│
└─ Nejsi si jistý?
     └─ NEZAPISUJ NIKAM. Nejdřív ověř umístění.
```

## Promotion gate

`provenance` a `data/candidates` jsou evidence, ne automaticky použitelné runtime definice. Promotion vyžaduje odpovídající provenance, vozidlovou/ECU vazbu, přesný request/response nebo raw capture, byte/bit mapování, škálování a jednotku, replay a opakovatelnost. U generací vozidla musí být doložena použitelnost.

Nejisté CAN ID, význam polí, bezpečnostní sekvence, klíče, immobilizer/component-protection, coding a flashing se nesmí povýšit pouze na základě názvu symbolu nebo statické extrakce.

## Závazné pravidlo

**Evidence nejprve → candidate → ověření → promotion → runtime.**

Nikdy opačně.
