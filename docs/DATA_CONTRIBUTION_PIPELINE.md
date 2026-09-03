# Data Contribution Pipeline — sdílený dataset pro PID/DTC přesnost

Status: návrh + klientská implementace (Android core). Server-side část je referenční, ne produkční.

## Cíl

Shromažďovat data od uživatelů s výslovným souhlasem pro ověřování PID vzorců, rozšiřování DTC pokrytí a zlepšení rozpoznávání vozidel.

## Architektura

```text
Android app (explicitní opt-in)
   │ HTTPS, anonymizovaná/agregovaná data
   ▼
Ingestion endpoint
   │ validace + rate limiting + staging
   ▼
Pull request do dataset repo
   │ lidská kontrola
   ▼
Merge → dataset
```

Appka záměrně nepoužívá GitHub write token. Klient posílá pouze minimální agregovaná data na vlastní HTTPS ingestion endpoint; server následně připravuje PR do datasetového repozitáře.

## Co se smí odeslat

- verzovaný souhlas,
- prvních 10 znaků VIN jako `wmiVdsModelYear`,
- agregované PID min/max/mean/počet vzorků,
- agregované počty DTC,
- software/firmware hinty, pokud jsou dostupné,
- měsíc místo přesného času.

## Co se neposílá

- plný ani hashovaný VIN,
- GPS/poloha,
- volný text,
- syrové CAN rámce,
- vysokofrekvenční časové řady,
- žádná data bez aktivního souhlasu.

## Bezpečnostní hranice

`ContributionConsentManager` znovu ověřuje souhlas při každém flush. `revoke` i `decline` mažou lokální frontu. Server odmítá záznamy mimo minimální schéma a nikdy nemá obsahovat GitHub write credential v klientovi.

Referenční Worker v `services/ingestion-reference/` není produkční endpoint. Před produkčním použitím je nutné doplnit autentizaci/ochranu proti zneužití, persistentní staging, rate limiting, monitoring a retenční politiku.

## Otevřené kroky

- napojit `ContributionRecordBuilder` na skutečné výsledky relace,
- Room/DataStore pro consent a queue,
- skutečný ingestion backend,
- UI pro změnu/odvolání souhlasu,
- privacy notice/DPIA před produkčním zapnutím.
