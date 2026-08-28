package com.autodiag.wican.core.ui

/** Central Czech tooltip catalogue. UI components should use these texts instead of ad-hoc explanations. */
object DiagnosticTooltips {
    const val CAPABILITY = "Zjišťuje, zda vozidlo a připojené rozhraní skutečně poskytují tento údaj. Výsledek je vázán na konkrétní VIN a verzi firmwaru/softwaru."
    const val REPORTED = "Hodnota byla přímo nahlášena řídicí jednotkou. AutoDiag ji nevypočítal."
    const val ESTIMATED = "Hodnota je odhad vypočítaný z dostupných měření. Nejde o údaj přímo nahlášený vozidlem."
    const val NOT_AVAILABLE = "Vozidlo nebo aktuální rozhraní tento údaj neposkytlo. AutoDiag hodnotu nedoplňuje odhadem."
    const val US_MARKET = "⚠️ Zjištěna pravděpodobná americká specifikace. Diagnostické a servisní postupy se mohou lišit od evropské verze."
    const val RISO = "Izolační údaj musí být označen podle zdroje: hodnota nahlášená vozidlem není totéž co fyzické měření izolačního odporu."
    const val CELL_DELTA = "Rozdíl mezi nejvyšším a nejnižším dostupným napětím článku/modulu. Samotná hodnota bez kontextu zátěže, teploty a stavu nabití není důkaz závady."
    const val REPLAY = "Při přehrávání lze posouvat čas a procházet dostupná data článků, modulů a packu v daném okamžiku."
    const val DRY_RUN = "Simulace pravidla nad záznamem. Nic se neposílá do vozidla. Zobrazí se pouze okamžiky, kdy by pravidlo reagovalo."
    const val TRANSPORT_SECURITY = "Zobrazuje, zda je přenos chráněn. Nešifrované rozhraní může umožnit neoprávněný přístup k diagnostickému kanálu."
}
