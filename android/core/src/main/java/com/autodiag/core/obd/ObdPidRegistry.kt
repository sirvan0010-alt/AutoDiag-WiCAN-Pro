package com.autodiag.core.obd

/** Registry of standardized Mode 01 PIDs. Vehicle-specific signals belong in vehicle profiles. */
object ObdPidRegistry {
    val definitions: Map<Int, ObdPidDefinition> = listOf(
        ObdPidDefinition(0x04, 1, "%", "Zatížení motoru") { d -> d.getOrNull(0)?.let { it * 100.0 / 255.0 } },
        ObdPidDefinition(0x05, 1, "°C", "Teplota chladicí kapaliny") { d -> d.getOrNull(0)?.let { (it - 40).toDouble() } },
        ObdPidDefinition(0x06, 1, "%", "Krátkodobé přizpůsobení paliva – banka 1") { d -> d.getOrNull(0)?.let { (it - 128) * 100.0 / 128.0 } },
        ObdPidDefinition(0x07, 1, "%", "Dlouhodobé přizpůsobení paliva – banka 1") { d -> d.getOrNull(0)?.let { (it - 128) * 100.0 / 128.0 } },
        ObdPidDefinition(0x0A, 1, "kPa", "Tlak paliva") { d -> d.getOrNull(0)?.let { it * 3.0 } },
        ObdPidDefinition(0x0B, 1, "kPa", "Absolutní tlak v sacím potrubí") { d -> d.getOrNull(0)?.toDouble() },
        ObdPidDefinition(0x0C, 2, "RPM", "Otáčky motoru") { d -> if (d.size < 2) null else ((d[0] * 256) + d[1]) / 4.0 },
        ObdPidDefinition(0x0D, 1, "km/h", "Rychlost") { d -> d.getOrNull(0)?.toDouble() },
        ObdPidDefinition(0x0E, 1, "°", "Předstih zapalování") { d -> d.getOrNull(0)?.let { it / 2.0 - 64.0 } },
        ObdPidDefinition(0x0F, 1, "°C", "Teplota nasávaného vzduchu") { d -> d.getOrNull(0)?.let { (it - 40).toDouble() } },
        ObdPidDefinition(0x10, 2, "g/s", "Hmotnostní průtok vzduchu (MAF)") { d -> if (d.size < 2) null else ((d[0] * 256) + d[1]) / 100.0 },
        ObdPidDefinition(0x11, 1, "%", "Poloha škrticí klapky") { d -> d.getOrNull(0)?.let { it * 100.0 / 255.0 } },
        ObdPidDefinition(0x1F, 2, "s", "Doba běhu motoru od startu") { d -> if (d.size < 2) null else ((d[0] * 256) + d[1]).toDouble() },
        ObdPidDefinition(0x21, 2, "km", "Ujetá vzdálenost s aktivní kontrolkou MIL") { d -> if (d.size < 2) null else ((d[0] * 256) + d[1]).toDouble() },
        ObdPidDefinition(0x2F, 1, "%", "Úroveň paliva v nádrži") { d -> d.getOrNull(0)?.let { it * 100.0 / 255.0 } },
        ObdPidDefinition(0x33, 1, "kPa", "Barometrický tlak") { d -> d.getOrNull(0)?.toDouble() },
        ObdPidDefinition(0x42, 2, "V", "Napětí řídicí jednotky") { d -> if (d.size < 2) null else ((d[0] * 256) + d[1]) / 1000.0 },
        ObdPidDefinition(0x43, 2, "%", "Absolutní hodnota zatížení") { d -> if (d.size < 2) null else ((d[0] * 256) + d[1]) * 100.0 / 255.0 },
        ObdPidDefinition(0x46, 1, "°C", "Okolní teplota vzduchu") { d -> d.getOrNull(0)?.let { (it - 40).toDouble() } },
        ObdPidDefinition(0x5C, 1, "°C", "Teplota motorového oleje") { d -> d.getOrNull(0)?.let { (it - 40).toDouble() } }
    ).associateBy { it.pid }

    fun get(pid: Int): ObdPidDefinition? = definitions[pid]
    fun isSupported(pid: Int): Boolean = definitions.containsKey(pid)
}
