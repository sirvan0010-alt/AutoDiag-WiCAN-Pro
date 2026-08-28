# UI — ČESKÉ TOOLTIPY A CHYBOVÉ STAVY

## Pravidlo

Každá diagnostická metrika, výsledek automatického testu, bezpečnostní upozornění, neznámá hodnota a akce s technickým významem musí mít vedle názvu ikonu `?`.

Tooltip je součástí diagnostického produktu, ne dekorace. Text musí vysvětlit:

1. **Co se měří** — jednoduchou češtinou.
2. **Proč je to důležité** — praktický význam pro uživatele.
3. **Odkud údaj pochází** — ECU, CAN, UDS, fyzický test nebo výpočet.
4. **Jak se výsledek interpretuje** — včetně omezení.
5. **Stav ověření** — `OVĚŘENO`, `ČÁSTEČNĚ OVĚŘENO`, `NEOVĚŘENO`.

## Povinné tooltipy pro PRE-PURCHASE TEST

### Pyropojistka / HV odpojovač
> Pyropojistka je bezpečnostní prvek vysokonapěťové baterie, který může při havárii nebo jiné kritické události fyzicky odpojit HV obvod. AutoDiag zde zobrazuje pouze stav, který skutečně poskytuje podporovaná řídicí jednotka. Stav „nelze zjistit“ neznamená, že je pyropojistka vadná ani že je v pořádku.

### HV izolace (Riso)
> Riso je izolační odpor mezi vysokonapěťovým systémem a karoserií. Nízká hodnota může souviset například s poškozením izolace, vlhkostí nebo závadou některého HV komponentu. Prahové hodnoty nejsou univerzální pro všechna vozidla; AutoDiag vždy uvádí zdroj a rozsah platnosti použitého limitu.

### Havárie / Crash evidence
> Tato kontrola hledá dostupné záznamy a stavy související s havárií, aktivací bezpečnostních systémů a pyrotechnickým odpojením HV. Čistý výsledek neprokazuje, že vozidlo nikdy nebylo havarované, pokud daná jednotka historii neposkytuje.

### VIN ECU / Gateway
> Porovnání VIN pomáhá odhalit nesoulad mezi řídicí jednotkou a vozidlem. Nesoulad může znamenat výměnu jednotky, chybu konfigurace nebo jiný servisní zásah. Samotný nesoulad není automaticky důkaz podvodu.

### Odometr
> AutoDiag porovnává hodnoty kilometrů pouze z jednotek, které je skutečně poskytují. Rozdíl je nejprve označen jako nesrovnalost. Samotná latence CAN komunikace není důkazem stočeného tachometru.

### Stav „Není k dispozici“
> Vozidlo nebo připojené rozhraní neposkytuje potřebný údaj. Tento stav se nesmí převést na „OK“ ani „Chyba“.

### Stav „Neověřeno“
> AutoDiag zná možný signál nebo interpretaci, ale nemá dostatečné důkazy pro potvrzení jeho významu pro tuto konkrétní verzi vozidla, ECU nebo firmwaru.

### Omezené hodnocení
> Test proběhl, ale chybí některá data potřebná pro spolehlivé posouzení. Výsledek proto není verdikt o celkovém stavu vozidla.

### Kritické upozornění
> Byla nalezena informace, která vyžaduje další odbornou kontrolu. AutoDiag záměrně neoznačuje nález jako definitivní příčinu závady, pokud ji nelze prokázat dostupnými daty.

## Chybové zprávy

Technické chyby se uživateli zobrazují česky. Interní kód zůstává zachován pro diagnostiku a logy.

Příklad:

```text
Nepodařilo se přečíst diagnostický údaj.
Kód: UDS_TIMEOUT
Jednotka: BMS

Co to znamená?
Řídicí jednotka v nastaveném časovém limitu neodpověděla.
Neznamená to automaticky, že je jednotka vadná.

[ ? ] Podrobnosti
```

## Jednotný formát

V UI používat komponentu `InfoTooltip` s centrálním textem z knowledge base. Texty se nemají kopírovat do jednotlivých obrazovek, aby se technické vysvětlení nerozcházelo.

Ikona `?` má být dostupná i u:

- každého řádku výsledku;
- každého diagnostického parametru;
- každého barevného stavu;
- `NOT_AVAILABLE`, `NOT_TESTED`, `UNASSESSED`, `ERROR`;
- hodnoty s použitým prahem;
- confidence / důvěryhodnosti;
- zdroje a verification state;
- tlačítka `AUTO TEST`, pokud jeho rozsah není z obrazovky zřejmý.
