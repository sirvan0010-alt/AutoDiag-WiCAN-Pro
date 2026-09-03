# Doplněk: P1 — Data contribution pipeline

Hotová základní vrstva:

- [x] verzovaný explicitní souhlas grant/decline/revoke
- [x] klientská anonymizace VIN na 10 znaků
- [x] žádná poloha ani volný text
- [x] agregované PID statistiky a DTC počty
- [x] lokální fronta + uploader s opakovanou kontrolou souhlasu
- [x] revoke/decline promaže lokální frontu
- [x] Compose obrazovka explicitního souhlasu
- [x] referenční HTTPS ingestion endpoint
- [x] PII screening volného textu před GitHub community publikací

Další kroky:

- [ ] `ContributionRecordBuilder` napojit na reálnou diagnostickou relaci
- [ ] Room/DataStore místo in-memory referencí
- [ ] produkční ingestion endpoint s rate limitingem a ochranou proti zneužití
- [ ] nastavení v appce pro změnu/odvolání souhlasu kdykoliv
- [ ] privacy notice a DPIA před produkčním zapnutím
- [ ] navázat ingestion backend na `AutoDiag-WiCAN-Diagnostic-Data`
