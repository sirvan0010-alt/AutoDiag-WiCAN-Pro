# PUSH_GUIDE.md — AutoDiag-WiCAN-Pro

Historický návod k nahrání dokumentů. Stav po docs committech:

- `AI_HANDOFF.md` — v `main` (audit HEAD 94f1409+)
- `FEATURE_PROPOSALS.md`, `REFERENCES.md`, `help_content_schema.md` — v kořeni
- `docs/` existuje (CAPABILITY_DISCOVERY, PRE_PURCHASE, AUTOMATION, …)
- `SAFETY.md` jako samostatný soubor **zatím ne** (pravidla v `AI_CONTEXT.md`)
- Otevřené PR: zkontroluj https://github.com/sirvan0010-alt/AutoDiag-WiCAN-Pro/pulls

## Otevřené PR (před merge ověř diff proti main)

| # | Title |
|---|-------|
| 3 | feat: connect WiCAN to capability discovery |
| 2 | Add evidence-based pre-purchase forensics and Czech tooltips |
| 1 | feat: expand diagnostic architecture and pre-purchase inspection |

## Soubory

| Soubor | Umístění |
|--------|----------|
| AI_HANDOFF.md | kořen |
| FEATURE_PROPOSALS.md | kořen |
| REFERENCES.md | kořen |
| help_content_schema.md | kořen |
| PUSH_GUIDE.md | docs/ |

## Architektonický princip (vytesáno)

Nebudeme optimalizovat na „co nejjednodušší implementaci“.
Stavíme rozáhlý diagnostický systém:

```text
hardware → transport → evidence → diagnostika → automatizace → analýza → UI
```

AUTO TEST / PRE-PURCHASE včetně HV baterie (REST → CHARGE → LOAD → RECOVERY)
s adaptive sampling je cíl. `NOT_AVAILABLE` je platný výsledek, ne chyba.

## Další krok po docs

1. `./gradlew :app:assembleDebug`
2. End-to-end **Simulátor** (CONNECTING → INITIALIZING_ELM → DISCOVERING → READY)
3. Teprve pak PID value parser / DTC / HV orchestration
