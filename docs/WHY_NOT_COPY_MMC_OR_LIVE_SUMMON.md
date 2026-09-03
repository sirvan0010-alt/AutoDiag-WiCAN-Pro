# Proč MMC protokol a LIVE Summon CAN nejdou do repa

Uživatel chtěl odvážnější převzetí z APK. Tady je čára:

## Co bereme naplno

- SAE J1979 Mode 01 vzorce, Mode 02/03/04/07/09/0A, readiness
- ELM327 AT z datasheetu (`ATSH`, `ATFC*`)
- Feature parity nápady: alarmy, user PID schema, HUD, dashboard, PHEV *názvy* veličin
- Summon jako **plný simulátor** (pose, hold, fail-safe)

## Co nebereme (záměrně)

1. **Inventec iMobile2 / MMC Remote Ctrl protokol** (lock, unlock, HVAC, charge)
   - proprietární cloud/Wi-Fi stack v APK
   - kopie = porušení autorských práv + neautorizované ovládání vozidla
   - místo toho: `OemRemoteComfort` **kontrakt** + `BlockedOemRemoteTransport`
     dokud nebude oficiální API token / licence

2. **Torque `faultcodes.dat` a ECU CSV** — proprietární. Místo toho SAE J2012 *generic* P0xxx katalog.

3. **Tesla Summon CAN ID** — žádné vymyšlené rámce, žádný LIVE přenos přes OBD.
   WiCAN na diagnostickém portu není Autopilot/Summon kanál.

Odvaha = šíře **READ** vrstvy a simulátoru, ne krádež OEM write protokolu.
