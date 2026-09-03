# Ingestion reference service

Toto není produkční služba. Je to referenční implementace hranice mezi Android aplikací a datasetovým repozitářem.

## Kontrakt

`POST /v1/contributions`

- `Content-Type: application/json`
- tělo: `{"records": [ContributionRecord, ...]}`
- `202` při validaci a stagingu
- `400` při neplatném batchi
- `422` při porušení minimálního schématu

Worker nesmí mít GitHub write credential. Samostatná dávková úloha má načíst schválené záznamy, vytvořit PR do datasetového repozitáře a čekat na lidskou kontrolu.

Před produkcí je nutné doplnit autentizaci/anti-abuse ochranu, persistentní staging, rate limiting, monitoring a retenční politiku.
