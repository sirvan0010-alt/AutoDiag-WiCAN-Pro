package com.autodiag.core.obd

/**
 * Short titles for well-known **generic** SAE J2012 / ISO 15031-6 codes.
 * Manufacturer P1/P2xxx stay UNKNOWN until a verified profile exists.
 * Not copied from Torque faultcodes.dat.
 */
object GenericDtcCatalog {
    data class Entry(val code: String, val titleCs: String, val system: String)

    private val map: Map<String, Entry> = listOf(
        e("P0001", "Regulace paliva — obvod otevřený", "fuel"),
        e("P0100", "Hmotnostní průtok vzduchu — závada obvodu", "air"),
        e("P0101", "MAF — rozsah / výkon", "air"),
        e("P0102", "MAF — nízký vstup", "air"),
        e("P0103", "MAF — vysoký vstup", "air"),
        e("P0105", "MAP — závada obvodu", "air"),
        e("P0106", "MAP — rozsah / výkon", "air"),
        e("P0107", "MAP — nízký vstup", "air"),
        e("P0108", "MAP — vysoký vstup", "air"),
        e("P0110", "IAT — závada obvodu", "temp"),
        e("P0115", "ECT — závada obvodu", "temp"),
        e("P0117", "ECT — nízký vstup", "temp"),
        e("P0118", "ECT — vysoký vstup", "temp"),
        e("P0120", "TPS A — závada obvodu", "throttle"),
        e("P0121", "TPS A — rozsah / výkon", "throttle"),
        e("P0122", "TPS A — nízký vstup", "throttle"),
        e("P0123", "TPS A — vysoký vstup", "throttle"),
        e("P0125", "Nedostatečná teplota chladicí kapaliny pro řízení směsi", "temp"),
        e("P0128", "Termostat — teplota pod regulačním rozsahem", "temp"),
        e("P0130", "O2 senzor banka 1 senzor 1 — obvod", "o2"),
        e("P0131", "O2 B1S1 — nízké napětí", "o2"),
        e("P0132", "O2 B1S1 — vysoké napětí", "o2"),
        e("P0133", "O2 B1S1 — pomalá odezva", "o2"),
        e("P0134", "O2 B1S1 — žádná aktivita", "o2"),
        e("P0135", "O2 B1S1 ohřev — obvod", "o2"),
        e("P0171", "Směs příliš chudá — banka 1", "fuel"),
        e("P0172", "Směs příliš bohatá — banka 1", "fuel"),
        e("P0174", "Směs příliš chudá — banka 2", "fuel"),
        e("P0175", "Směs příliš bohatá — banka 2", "fuel"),
        e("P0201", "Vstřikovač 1 — obvod", "injector"),
        e("P0202", "Vstřikovač 2 — obvod", "injector"),
        e("P0203", "Vstřikovač 3 — obvod", "injector"),
        e("P0204", "Vstřikovač 4 — obvod", "injector"),
        e("P0300", "Výpadky zapalování — náhodné / více válců", "misfire"),
        e("P0301", "Výpadek zapalování válec 1", "misfire"),
        e("P0302", "Výpadek zapalování válec 2", "misfire"),
        e("P0303", "Výpadek zapalování válec 3", "misfire"),
        e("P0304", "Výpadek zapalování válec 4", "misfire"),
        e("P0305", "Výpadek zapalování válec 5", "misfire"),
        e("P0306", "Výpadek zapalování válec 6", "misfire"),
        e("P0325", "Klepání — senzor 1 obvod", "knock"),
        e("P0335", "Snímač klikové hřídele — obvod A", "ckp"),
        e("P0340", "Snímač vačkové hřídele — obvod A", "cmp"),
        e("P0400", "EGR — průtok", "egr"),
        e("P0401", "EGR — nedostatečný průtok", "egr"),
        e("P0402", "EGR — nadměrný průtok", "egr"),
        e("P0420", "Účinnost katalyzátoru pod prahem — banka 1", "cat"),
        e("P0430", "Účinnost katalyzátoru pod prahem — banka 2", "cat"),
        e("P0440", "EVAP — závada systému", "evap"),
        e("P0442", "EVAP — malý únik", "evap"),
        e("P0455", "EVAP — velký únik", "evap"),
        e("P0456", "EVAP — velmi malý únik", "evap"),
        e("P0500", "Snímač rychlosti vozidla A", "vss"),
        e("P0505", "Regulace volnoběhu", "idle"),
        e("P0506", "Volnoběh — otáčky pod očekáváním", "idle"),
        e("P0507", "Volnoběh — otáčky nad očekáváním", "idle"),
        e("P0560", "Systémové napětí", "electrical"),
        e("P0562", "Systémové napětí nízké", "electrical"),
        e("P0563", "Systémové napětí vysoké", "electrical"),
        e("P0600", "Sériová komunikace", "ecu"),
        e("P0606", "ECM/PCM procesor", "ecu"),
        e("P0700", "Převodovka — řídicí systém (MIL požadavek)", "tcm"),
        e("C0035", "ABS snímač kola — levé přední", "abs"),
        e("B0001", "ISO/SAE vyhrazeno — SRS", "srs"),
        e("U0100", "Ztráta komunikace s ECM/PCM A", "network"),
        e("U0101", "Ztráta komunikace s TCM", "network"),
        e("U0121", "Ztráta komunikace s ABS", "network"),
        e("U0140", "Ztráta komunikace s BCM", "network"),
    ).associateBy { it.code }

    fun lookup(code: String): Entry? = map[code.uppercase()]

    fun titleOrUnknown(code: String): String =
        lookup(code)?.titleCs ?: if (code.startsWith("P1") || code.startsWith("P2") || code.startsWith("P3"))
            "Výrobně specifický kód — bez ověřeného profilu"
        else "Neznámý / není v generic katalogu"

    private fun e(code: String, titleCs: String, system: String) = Entry(code, titleCs, system)
}
