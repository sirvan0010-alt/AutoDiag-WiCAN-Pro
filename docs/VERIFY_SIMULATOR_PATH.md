# Verify simulator path (bez vozidla)

## Co musí projít

```text
SimulatorWiCanTransport
  → connect
  → Elm327Session.initialize (ATZ, ATE0, ATL0, ATH1, ATSP0)
  → CapabilityDiscovery.run (ATI, ATDP, 0902, 03, 010C)
  → snapshot: COMMUNICATION/PROTOCOL/VIN/MODE01/MODE03 AVAILABLE
  → Mode01Decoder.decode("41 0C …") → RPM (nebo null při NO DATA)
```

## Příkaz

```bash
cd android
./gradlew :core:test --tests com.autodiag.core.SimulatorPathTest
```

Vyžaduje Gradle wrapper + JDK 17. Android SDK pro **unit** testy core není nutný
(unit test běží na JVM). `assembleDebug` vyžaduje Android SDK.

## Očekávaný VIN simulátoru

`SIMTEST0AUTODIAG01` (syntetický, ne reálné vozidlo).

## Co test **neověřuje**

- TCP k reálnému WiCAN
- TEC/REC (FW dependent)
- Adaptive sampling / calibration engine
- Fake cell/SOC/Riso — ty se do simulátoru záměrně nepřidávají
