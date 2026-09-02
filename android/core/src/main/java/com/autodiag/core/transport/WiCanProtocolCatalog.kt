package com.autodiag.core.transport

/** User-facing description of the communication modes available through WiCAN PRO. */
data class WiCanProtocolInfo(
    val id: TransportMode,
    val titleCs: String,
    val shortDescriptionCs: String,
    val bestForCs: String,
    val limitationsCs: String,
    val recommendedForAutoDiag: Boolean,
    val tooltipCs: String
)

object WiCanProtocolCatalog {
    val modes: List<WiCanProtocolInfo> = listOf(
        WiCanProtocolInfo(
            TransportMode.ELM327,
            "ELM327 / OBD-II",
            "Diagnostický režim pro standardní OBD-II komunikaci.",
            "Nejlepší volba pro automatickou diagnostiku, PIDy, DTC, VIN, readiness a živá data.",
            "Nezpřístupňuje automaticky veškerá proprietární CAN data výrobce.",
            true,
            "AutoDiag tento režim preferuje pro běžnou diagnostiku. Nejprve použije standardní OBD služby a podle odpovědí vozidla zjistí jeho skutečné schopnosti."
        ),
        WiCanProtocolInfo(
            TransportMode.SLCAN_RAW,
            "SLCAN / RAW CAN",
            "Surová CAN komunikace přes SLCAN.",
            "Pokročilé CAN monitorování, pasivní snímání a budoucí OEM/UDS diagnostiku.",
            "Samotné připojení SLCAN neznamená dostupnost standardních OBD příkazů, VIN ani DTC.",
            false,
            "SLCAN je vhodný pro přímou práci s CAN rámci. Je určen pro pokročilou analýzu a vyžaduje znalost CAN, bitrate, adresace a diagnostického protokolu. AutoDiag ho použije jako pokročilou cestu, ne jako výchozí režim."
        ),
        WiCanProtocolInfo(
            TransportMode.SIMULATOR,
            "Simulátor",
            "Lokální syntetický ELM327 endpoint bez vozidla.",
            "Vývoj, automatické testy a ověřování UI bez hardwaru.",
            "Data nejsou z vozidla a nesmí být prezentována jako skutečná diagnostika.",
            false,
            "Simulátor je určen pouze pro vývoj a CI. Odpovědi jsou deterministické a syntetické."
        )
    )

    val recommended: WiCanProtocolInfo
        get() = modes.first { it.recommendedForAutoDiag }

    fun forMode(mode: TransportMode?): WiCanProtocolInfo? = modes.firstOrNull { it.id == mode }
}
