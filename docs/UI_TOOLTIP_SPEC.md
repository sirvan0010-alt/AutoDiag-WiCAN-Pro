# AutoDiag UI: české tooltipy a vysvětlení

## Povinné pravidlo
Každá diagnostická metrika, capability, stav transportu, výsledek testu a automatizační akce má v UI ikonu `?`. Tooltip musí být česky a vysvětlit:

1. co údaj znamená,
2. odkud pochází,
3. zda je Reported / Estimated / Not Available,
4. jaké má omezení,
5. případně proč byl výsledek označen jako varování.

## Chyby a DTC
Chybová karta se zobrazí česky a pokud existuje ověřený záznam znalostní báze, nabídne tlačítko **Vysvětlení a postup opravy**. Odkaz je uložen v `KnowledgeSource` a nikdy se negeneruje podle domněnky.

Doporučená struktura:

- **Co to znamená**
- **Co může být příčinou**
- **Co zkontrolovat**
- **Doporučený servisní postup**
- **Zdroj / ověření**

Pokud OEM postup není dostupný, UI musí výslovně napsat: `Ověřený servisní postup není v databázi.`

## Tržní varianta
Pokud je US trh spolehlivě identifikován, zobrazí se `⚠️ Zjištěna americká specifikace` s tooltipem. Pokud identifikace není spolehlivá, zobrazí se `Trh: Neznámý` bez tvrzení.

## Replay
U každého vybraného článku/modulu je dostupný tooltip vysvětlující časový kurzor. Uživatel může zpětně procházet přesnou hodnotu v daném timestampu; UI nesmí nahradit chybějící vzorek interpolací bez jasného označení.
