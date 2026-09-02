# Implementační poznámka: registry-driven Mode 01

Datum: 2026-09-02

## Cíl

Převést generický OBD-II Mode 01 z rostoucího `when` bloku na datově řízený registr. Tím vzniká základ, na který lze bezpečně napojit budoucí Live Data Engine, supported-PID bitmapu, grafy a adaptivní polling.

## Co je nyní v repu

- `ObdPidDefinition.kt` — typovaná definice PID a explicitní stav `AVAILABLE` / `UNAVAILABLE` / `UNKNOWN_PID`.
- `ObdPidRegistry.kt` — 20 standardních Mode 01 PID definic.
- `Mode01Decoder.kt` — registry-driven dekodér; původní `decode()` API zůstává zachováno.
- `ObdPidRegistryTest.kt` — pin-testy původních RPM fixtur a nové hraniční případy.

Registry obsahuje např. zatížení motoru, teploty, palivové trimy, tlak, RPM, rychlost, MAF, škrticí klapku, dobu běhu, hladinu paliva, barometrický tlak a napětí řídicí jednotky.

## Architektonické pravidlo

Generický registr je určen pouze pro standardizované OBD-II významy. Výrobce-specifické PID/signály patří do samostatných profilů vozidel (Tesla, VAG, BMW atd.). Neznámý PID se nesmí převést na smyšlenou hodnotu.

## Další krok

- Mode 01 supported-PID bitmapa (`0x00` a další bloky)
- scheduler, který se nejdříve naučí dostupné PID a potom plánuje polling podle priority a požadované frekvence
- Mode 02 freeze frame
- Mode 03/07/0A DTC
- Mode 06 test results
- Mode 09 VIN/CALID/CVN

Tato poznámka je doplňková; roadmapa a audit zůstávají autoritativními dokumenty pro pořadí práce.
