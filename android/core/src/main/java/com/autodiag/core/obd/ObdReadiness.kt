package com.autodiag.core.obd

/** SAE J1979 PID 01 — MIL, DTC count, continuous/non-continuous monitors. */
data class ReadinessReport(
    val milOn: Boolean,
    val storedDtcCount: Int,
    val sparkIgnition: Boolean?,
    val monitors: List<MonitorStatus>,
)

data class MonitorStatus(
    val id: String,
    val nameCs: String,
    val supported: Boolean,
    val complete: Boolean?,
)

object ObdReadinessDecoder {
    fun decodePid01(data: List<Int>): ReadinessReport? {
        if (data.size < 4) return null
        val a = data[0]
        val b = data[1]
        val c = data[2]
        val d = data[3]
        val mil = (a and 0x80) != 0
        val count = a and 0x7F
        val spark = (b and 0x08) == 0
        val monitors = mutableListOf<MonitorStatus>()
        fun bit(v: Int, supportedBit: Int, readyBit: Int, id: String, name: String) {
            val supported = (v and supportedBit) != 0
            val incomplete = (v and readyBit) != 0
            monitors += MonitorStatus(id, name, supported, if (supported) !incomplete else null)
        }
        bit(b, 0x01, 0x10, "misfire", "Výpadky zapalování")
        bit(b, 0x02, 0x20, "fuel", "Palivový systém")
        bit(b, 0x04, 0x40, "ccm", "Komplexní součásti")
        if (spark) {
            bit(c, 0x01, d and 0x01, "cat", "Katalyzátor")
            bit(c, 0x02, d and 0x02, "hcat", "Vyhřívaný katalyzátor")
            bit(c, 0x04, d and 0x04, "evap", "EVAP")
            bit(c, 0x08, d and 0x08, "sair", "Sekundární vzduch")
            bit(c, 0x10, d and 0x10, "ac", "A/C chladivo")
            bit(c, 0x20, d and 0x20, "o2", "Kyslíkový senzor")
            bit(c, 0x40, d and 0x40, "o2h", "Ohřev O2")
            bit(c, 0x80, d and 0x80, "egr", "EGR/VVT")
        } else {
            bit(c, 0x01, d and 0x01, "nmhc", "NMHC katalyzátor")
            bit(c, 0x02, d and 0x02, "nox", "NOx/SCR")
            bit(c, 0x04, d and 0x04, "boost", "Boost pressure")
            bit(c, 0x08, d and 0x08, "egs", "Výfukový senzor")
            bit(c, 0x20, d and 0x20, "pf", "Filtr pevných částic")
            bit(c, 0x80, d and 0x80, "egr", "EGR/VVT")
        }
        return ReadinessReport(mil, count, spark, monitors)
    }

    private fun bit(c: Int, supportedMask: Int, readyMask: Int, id: String, name: String): MonitorStatus {
        val supported = (c and supportedMask) != 0
        val incomplete = (readyMask) != 0
        return MonitorStatus(id, name, supported, if (supported) !incomplete else null)
    }
}
