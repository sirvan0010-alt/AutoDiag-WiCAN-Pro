package com.autodiag.core.obd

/** Metadata and decoder for a standard SAE J1979 Mode 01 PID. */
data class ObdPid<T>(
    val pid: Int,
    val nameCs: String,
    val unit: String,
    val descriptionCs: String,
    val decode: (ByteArray) -> T?
)

object ObdPids {
    private fun u8(b: Byte) = b.toInt() and 0xFF
    private fun u16(b: ByteArray) = (u8(b[0]) shl 8) or u8(b[1])
    private fun requireSize(b: ByteArray, n: Int): Boolean = b.size >= n

    val standard: List<ObdPid<*>> = listOf(
        ObdPid(0x04, "Vypočtené zatížení motoru", "%", "Relativní zatížení motoru.") { b -> if (requireSize(b, 1)) u8(b[0]) * 100.0 / 255.0 else null },
        ObdPid(0x05, "Teplota chladicí kapaliny", "°C", "Teplota chladicí kapaliny motoru.") { b -> if (requireSize(b, 1)) u8(b[0]) - 40.0 else null },
        ObdPid(0x06, "Krátkodobá korekce paliva – banka 1", "%", "Krátkodobá korekce směsi pro banku 1.") { b -> if (requireSize(b, 1)) u8(b[0]) * 100.0 / 128.0 - 100.0 else null },
        ObdPid(0x07, "Dlouhodobá korekce paliva – banka 1", "%", "Dlouhodobá korekce směsi pro banku 1.") { b -> if (requireSize(b, 1)) u8(b[0]) * 100.0 / 128.0 - 100.0 else null },
        ObdPid(0x0B, "Absolutní tlak v sání", "kPa", "Absolutní tlak v sacím potrubí.") { b -> if (requireSize(b, 1)) u8(b[0]).toDouble() else null },
        ObdPid(0x0C, "Otáčky motoru", "rpm", "Otáčky motoru.") { b -> if (requireSize(b, 2)) u16(b) / 4.0 else null },
        ObdPid(0x0D, "Rychlost vozidla", "km/h", "Rychlost vozidla.") { b -> if (requireSize(b, 1)) u8(b[0]).toDouble() else null },
        ObdPid(0x0F, "Teplota nasávaného vzduchu", "°C", "Teplota vzduchu na sání.") { b -> if (requireSize(b, 1)) u8(b[0]) - 40.0 else null },
        ObdPid(0x10, "Průtok vzduchu MAF", "g/s", "Hmotnostní průtok vzduchu.") { b -> if (requireSize(b, 2)) u16(b) / 100.0 else null },
        ObdPid(0x11, "Poloha škrticí klapky", "%", "Relativní poloha škrticí klapky.") { b -> if (requireSize(b, 1)) u8(b[0]) * 100.0 / 255.0 else null },
        ObdPid(0x1C, "OBD standard vozidla", "", "Standard OBD, ke kterému se ECU hlásí.") { b -> if (requireSize(b, 1)) u8(b[0]) else null },
        ObdPid(0x1F, "Čas od nastartování", "s", "Doba od nastartování motoru.") { b -> if (requireSize(b, 2)) u16(b) else null },
        ObdPid(0x2F, "Úroveň paliva", "%", "Jmenovitá úroveň paliva v nádrži.") { b -> if (requireSize(b, 1)) u8(b[0]) * 100.0 / 255.0 else null },
        ObdPid(0x42, "Napětí řídicí jednotky", "V", "Napětí napájení řídicí jednotky.") { b -> if (requireSize(b, 2)) u16(b) / 1000.0 else null },
        ObdPid(0x46, "Okolní teplota", "°C", "Teplota okolního vzduchu.") { b -> if (requireSize(b, 1)) u8(b[0]) - 40.0 else null },
        ObdPid(0x4D, "Čas se zapnutou kontrolkou MIL", "min", "Čas od aktivace MIL.") { b -> if (requireSize(b, 2)) u16(b) else null },
        ObdPid(0x4E, "Čas od vymazání DTC", "min", "Čas od posledního vymazání diagnostických kódů.") { b -> if (requireSize(b, 2)) u16(b) else null },
        ObdPid(0x51, "Typ paliva", "", "Kód typu paliva podle OBD.") { b -> if (requireSize(b, 1)) u8(b[0]) else null },
        ObdPid(0x5C, "Teplota motorového oleje", "°C", "Teplota motorového oleje.") { b -> if (requireSize(b, 1)) u8(b[0]) - 40.0 else null },
        ObdPid(0x5E, "Průtok paliva motorem", "L/h", "Průtok paliva, pokud jej vozidlo poskytuje.") { b -> if (requireSize(b, 2)) u16(b) / 20.0 else null },
        ObdPid(0x5F, "Požadovaný točivý moment", "%", "Požadovaný moment vyjádřený standardizovaným procentem.") { b -> if (requireSize(b, 1)) u8(b[0]) - 125.0 else null },
        ObdPid(0x60, "Aktuální točivý moment", "%", "Aktuální moment vyjádřený standardizovaným procentem.") { b -> if (requireSize(b, 1)) u8(b[0]) - 125.0 else null }
    )

    private val byPid = standard.associateBy { it.pid }
    fun forPid(pid: Int): ObdPid<*>? = byPid[pid]
}

/** Result of parsing one ELM327 Mode 01 response. */
data class ObdPidResponse(val pid: Int, val data: ByteArray)

object ObdResponseParser {
    /** Extracts positive Mode 01 responses such as 41 0C 1A F8, tolerating ELM spaces. */
    fun parseMode01(body: String): List<ObdPidResponse> {
        val tokens = body.uppercase()
            .replace(Regex("[^0-9A-F\\s]"), " ")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.length == 2 && it.all { c -> c in '0'..'9' || c in 'A'..'F' } }
            .map { it.toInt(16) }
        val out = mutableListOf<ObdPidResponse>()
        var i = 0
        while (i + 1 < tokens.size) {
            if (tokens[i] == 0x41) {
                val pid = tokens[i + 1]
                val next = when {
                    pid == 0x00 || pid == 0x20 || pid == 0x40 || pid == 0x60 || pid == 0x80 || pid == 0xA0 || pid == 0xC0 -> 4
                    else -> 2
                }
                if (i + 2 + next <= tokens.size) out += ObdPidResponse(pid, tokens.subList(i + 2, i + 2 + next).map { it.toByte() }.toByteArray())
                i += 2 + next
            } else i++
        }
        return out
    }

    fun supportedPids(body: String): Set<Int> {
        return parseMode01(body).filter { it.pid in setOf(0x00,0x20,0x40,0x60,0x80,0xA0,0xC0) }
            .flatMap { response ->
                response.data.take(4).flatMapIndexed { index, byte ->
                    (0..7).filter { bit -> (byte.toInt() and 0xFF and (1 shl (7 - bit))) != 0 }
                        .map { response.pid + index * 8 + it + 1 }
                }
            }.toSet()
    }
}
