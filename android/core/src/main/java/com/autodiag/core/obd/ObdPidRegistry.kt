package com.autodiag.core.obd

/**
 * Registry of standardized SAE J1979 Mode 01 PIDs that AutoDiag can decode.
 *
 * Discovery may find additional PIDs. They are not shown as decoded values
 * until an explicit decoder is present; this prevents invented measurements.
 */
object ObdPidRegistry {
    private fun u16(d: List<Int>): Int? =
        if (d.size >= 2) (d[0] * 256) + d[1] else null

    val definitions: Map<Int, ObdPidDefinition> = listOf(
        ObdPidDefinition(0x04, 1, "%", "Zatížení motoru") { d -> d.getOrNull(0)?.let { it * 100.0 / 255.0 } },
        ObdPidDefinition(0x05, 1, "°C", "Teplota chladicí kapaliny") { d -> d.getOrNull(0)?.let { (it - 40).toDouble() } },
        ObdPidDefinition(0x06, 1, "%", "Krátkodobé přizpůsobení paliva – banka 1") { d -> d.getOrNull(0)?.let { it * 100.0 / 128.0 - 100.0 } },
        ObdPidDefinition(0x07, 1, "%", "Dlouhodobé přizpůsobení paliva – banka 1") { d -> d.getOrNull(0)?.let { it * 100.0 / 128.0 - 100.0 } },
        ObdPidDefinition(0x08, 1, "%", "Krátkodobé přizpůsobení paliva – banka 2") { d -> d.getOrNull(0)?.let { it * 100.0 / 128.0 - 100.0 } },
        ObdPidDefinition(0x09, 1, "%", "Dlouhodobé přizpůsobení paliva – banka 2") { d -> d.getOrNull(0)?.let { it * 100.0 / 128.0 - 100.0 } },
        ObdPidDefinition(0x0A, 1, "kPa", "Tlak paliva") { d -> d.getOrNull(0)?.let { it * 3.0 } },
        ObdPidDefinition(0x0B, 1, "kPa", "Absolutní tlak v sacím potrubí") { d -> d.getOrNull(0)?.toDouble() },
        ObdPidDefinition(0x0C, 2, "rpm", "Otáčky motoru") { d -> u16(d)?.div(4.0) },
        ObdPidDefinition(0x0D, 1, "km/h", "Rychlost vozidla") { d -> d.getOrNull(0)?.toDouble() },
        ObdPidDefinition(0x0E, 1, "°", "Předstih zapalování") { d -> d.getOrNull(0)?.let { it / 2.0 - 64.0 } },
        ObdPidDefinition(0x0F, 1, "°C", "Teplota nasávaného vzduchu") { d -> d.getOrNull(0)?.let { (it - 40).toDouble() } },
        ObdPidDefinition(0x10, 2, "g/s", "Hmotnostní průtok vzduchu (MAF)") { d -> u16(d)?.div(100.0) },
        ObdPidDefinition(0x11, 1, "%", "Poloha škrticí klapky") { d -> d.getOrNull(0)?.let { it * 100.0 / 255.0 } },
        ObdPidDefinition(0x1F, 2, "s", "Doba běhu motoru od startu") { d -> u16(d)?.toDouble() },
        ObdPidDefinition(0x21, 2, "km", "Ujetá vzdálenost s aktivní kontrolkou MIL") { d -> u16(d)?.toDouble() },
        ObdPidDefinition(0x2F, 1, "%", "Úroveň paliva v nádrži") { d -> d.getOrNull(0)?.let { it * 100.0 / 255.0 } },
        ObdPidDefinition(0x33, 1, "kPa", "Barometrický tlak") { d -> d.getOrNull(0)?.toDouble() },
        ObdPidDefinition(0x42, 2, "V", "Napětí řídicí jednotky") { d -> u16(d)?.div(1000.0) },
        ObdPidDefinition(0x43, 2, "%", "Absolutní hodnota zatížení") { d -> u16(d)?.let { it * 100.0 / (255.0 * 256.0) } },
        ObdPidDefinition(0x46, 1, "°C", "Okolní teplota vzduchu") { d -> d.getOrNull(0)?.let { (it - 40).toDouble() } },
        ObdPidDefinition(0x4D, 2, "min", "Doba se zapnutou kontrolkou MIL") { d -> u16(d)?.toDouble() },
        ObdPidDefinition(0x4E, 2, "min", "Doba od vymazání DTC") { d -> u16(d)?.toDouble() },
        ObdPidDefinition(0x5C, 1, "°C", "Teplota motorového oleje") { d -> d.getOrNull(0)?.let { (it - 40).toDouble() } },
        ObdPidDefinition(0x5E, 2, "L/h", "Průtok paliva motorem") { d -> u16(d)?.div(20.0) },
        ObdPidDefinition(0x5F, 1, "%", "Požadovaný točivý moment") { d -> d.getOrNull(0)?.let { it - 125.0 } },
        ObdPidDefinition(0x60, 1, "%", "Aktuální točivý moment") { d -> d.getOrNull(0)?.let { it - 125.0 } }
    ).associateBy { it.pid }

    fun get(pid: Int): ObdPidDefinition? = definitions[pid]
    fun isSupported(pid: Int): Boolean = definitions.containsKey(pid)
}
